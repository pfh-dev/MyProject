package com.petadopt.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

/**
 * JWT 工具类：负责"签发令牌"和"解析令牌"。
 *
 * <p>JWT（JSON Web Token）是登录后发给前端的一张"电子身份证"。
 * 前端后续每次请求都把它放在请求头里，后端用签名校验它是否被篡改、
 * 是否过期，从而识别"你是谁"，无需每次查数据库维持会话。</p>
 *
 * <p>结构：JWT 由"头部.载荷.签名"三部分组成，载荷里塞了用户信息（id、用户名、角色等），
 * 签名用服务端密钥生成，任何人改了载荷都会导致签名对不上、解析失败。</p>
 */
@Component
public class JwtUtil {

    // 签名密钥，从配置文件读取（jwt.secret）。密钥必须保密，
    // 谁拿到密钥谁就能伪造令牌，所以绝不外泄、也不写死在代码里。
    @Value("${jwt.secret}")
    private String secret;

    // 令牌有效期（小时），默认 24 小时，可通过配置覆盖
    @Value("${jwt.expire-hours:24}")
    private long expireHours;

    /**
     * 把配置的密钥字符串转成 HMAC-SHA 签名所需的密钥对象。
     */
    private SecretKey key() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成令牌。
     *
     * @param claims 要放进令牌的自定义信息（例如 id、username、role）
     * @return 签好名的 JWT 字符串
     */
    public String generateToken(Map<String, Object> claims) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                // 给每个令牌分配一个全局唯一的 id（jti），
                // 用于"登出后拉黑"时精确标识某一个令牌。
                .id(UUID.randomUUID().toString())
                // 写入自定义载荷（用户信息）
                .claims(claims)
                // 签发时间
                .issuedAt(new Date(now))
                // 过期时间：当前时间 + 有效期，到期后令牌自动失效
                .expiration(new Date(now + expireHours * 3600_000L))
                // 用密钥签名，保证令牌不可伪造、不可篡改
                .signWith(key())
                // 打包成最终的 JWT 字符串
                .compact();
    }

    /**
     * 解析并校验令牌。
     *
     * @param token 前端传来的 JWT 字符串
     * @return 解析出的载荷 Claims（里面含用户信息）；若签名不对、令牌过期或格式错误，
     *         会抛出异常，由调用方捕获处理
     */
    public Claims parseToken(String token) {
        return Jwts.parser()
                // 用同一个密钥验签，签名不合法会直接抛异常
                .verifyWith(key())
                .build()
                // 解析并校验签名，拿到令牌主体
                .parseSignedClaims(token)
                // 返回载荷部分（用户信息）
                .getPayload();
    }
}
