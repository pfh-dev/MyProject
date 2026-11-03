package com.petadopt.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 登录认证拦截器：在请求进入 Controller 之前校验身份。
 *
 * <p>它负责三件事：</p>
 * <ol>
 *   <li>校验 JWT 是否合法（签名、过期）；</li>
 *   <li>校验令牌是否在黑名单（已登出/已吊销）；</li>
 *   <li>把解析出的用户信息写入 ThreadLocal，供本次请求后续使用。</li>
 * </ol>
 *
 * <p>拦截器像一道门卫：请求进来先过这道门，没带"通行证"（合法令牌）就直接挡在门外（返回 401），
 * 门都进不去，自然不会触碰到业务代码。</p>
 */
@Component
public class LoginInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(LoginInterceptor.class);

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private TokenBlacklistService tokenBlacklistService;

    /**
     * 请求处理前执行：校验令牌合法性。
     *
     * @return true 表示放行（继续往下走）；false 表示拦截（直接返回，不进 Controller）
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 浏览器跨域时会先发一个 OPTIONS "预检请求"，它不携带令牌，
        // 必须直接放行，否则跨域请求全部失败。
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        // 从请求头取出令牌。约定前端把 JWT 放在 Authorization 头里。
        String token = request.getHeader("Authorization");
        // 没带令牌 -> 未登录，返回 401（未授权）并拦截
        if (token == null || token.isBlank()) {
            response.setStatus(401);
            return false;
        }

        try {
            // 解析令牌。签名不对 / 过期会抛异常，走下面 catch 返回 401。
            Claims claims = jwtUtil.parseToken(token);

            // 取出令牌的唯一 id（jti），用于黑名单校验
            String jti = claims.getId();
            // 黑名单校验：Redis 异常时放行（避免 Redis 故障导致全员 401）
            if (jti != null) {
                boolean blacklisted;
                try {
                    blacklisted = tokenBlacklistService.isBlacklisted(jti);
                } catch (Exception e) {
                    // Redis 挂了不能把所有人都拒之门外，这里选择"降级放行"，
                    // 记录一条警告日志方便排查。
                    log.warn("JWT 黑名单校验失败，放行请求: {}", e.getMessage());
                    blacklisted = false;
                }
                // 令牌已被拉黑（用户已登出），拒绝访问
                if (blacklisted) {
                    response.setStatus(401);
                    return false;
                }
            }

            // 令牌校验通过，把用户信息装进 LoginUser，再放入 ThreadLocal
            // 这样本次请求内的任何代码（Controller、Service）都能通过 UserContext 拿到当前用户。
            LoginUser user = new LoginUser();
            user.setId(Long.valueOf(String.valueOf(claims.get("id"))));
            user.setUsername(String.valueOf(claims.get("username")));
            user.setRole(String.valueOf(claims.get("role")));
            user.setJti(jti);
            user.setExpiresAt(claims.getExpiration().getTime());
            UserContext.set(user);
        } catch (Exception e) {
            // 解析失败（篡改、过期、格式错）统一按未授权处理
            response.setStatus(401);
            return false;
        }
        return true;
    }

    /**
     * 请求处理完成后（无论成功还是异常）执行：清理 ThreadLocal。
     *
     * <p>为什么必须清理：Tomcat 用线程池复用线程，如果不清理，
     * 下一个复用该线程的请求会读到上一个用户的残留数据，造成"串号"安全事故。</p>
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.remove();
    }
}
