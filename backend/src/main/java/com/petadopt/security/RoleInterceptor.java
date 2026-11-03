package com.petadopt.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 角色权限拦截器：校验 {@link RequireRole} 注解。
 *
 * <p>登录拦截器只解决了"你有没有登录"；本拦截器解决"你有没有权限"。
 * 只有登录还不够，普通用户不该能访问只有管理员能用的接口，
 * 所以需要按角色做二次拦截。</p>
 */
@Component
public class RoleInterceptor implements HandlerInterceptor {

    /**
     * 请求处理前执行：判断当前用户角色是否满足接口上声明的 {@link RequireRole}。
     *
     * @return true 放行；false 拦截并返回 403（无权限）
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 只有映射到 Controller 方法的请求才有"角色注解"可查；
        // 静态资源、错误页等不是 HandlerMethod，直接放行。
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        // 先看方法上的注解，没有再看类上的注解（方法级优先、类级兜底）
        RequireRole requireRole = handlerMethod.getMethodAnnotation(RequireRole.class);
        if (requireRole == null) {
            requireRole = handlerMethod.getBeanType().getAnnotation(RequireRole.class);
        }

        // 既没方法注解也没类注解，说明该接口不需要角色限制，放行
        if (requireRole == null) {
            return true;
        }

        // 从 ThreadLocal 取当前用户角色（由登录拦截器写入）
        String currentRole = UserContext.role();
        // 拿不到角色说明根本没登录，返回 403
        if (currentRole == null) {
            response.setStatus(403);
            return false;
        }

        // 遍历注解里允许的角色列表，命中任意一个即放行
        for (String role : requireRole.value()) {
            if (role.equals(currentRole)) {
                return true;
            }
        }

        // 角色都不匹配 -> 无权限，返回 403
        response.setStatus(403);
        return false;
    }
}
