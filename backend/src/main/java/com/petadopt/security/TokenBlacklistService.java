package com.petadopt.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * JWT 黑名单：把已吊销令牌的 jti 写入 Redis，TTL 对齐令牌剩余有效期。
 *
 * <p>为什么要黑名单：JWT 是无状态的，签发后服务端无法主动"作废"它。
 * 当用户点击"退出登录"时，令牌其实还没过期，仍能被继续使用，这是安全隐患。
 * 解决办法：退出时把该令牌的 jti 存进 Redis 黑名单，
 * 后续每次请求都查一下黑名单，命中就直接拒绝。</p>
 *
 * <p>TTL（存活时间）设为令牌剩余有效期，这样令牌过期后黑名单记录也自动清理，
 * 不会无限堆积占用内存。</p>
 */
@Service
public class TokenBlacklistService {

    // Redis key 前缀，用于区分黑名单和其他缓存数据
    private static final String PREFIX = "jwt:blacklist:";

    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 把令牌加入黑名单。
     *
     * @param jti         令牌唯一 id
     * @param expireMillis 该令牌剩余有效毫秒数（作为黑名单记录的存活时间）
     */
    public void blacklist(String jti, long expireMillis) {
        // 无效参数直接忽略，避免写脏数据
        if (jti == null || expireMillis <= 0) {
            return;
        }
        // 写入 Redis，值随便给个 "1"，关键是设置过期时间
        redisTemplate.opsForValue().set(PREFIX + jti, "1", Duration.ofMillis(expireMillis));
    }

    /**
     * 判断令牌是否在黑名单中。
     *
     * @param jti 令牌唯一 id
     * @return true 表示已拉黑（应拒绝访问）；false 表示正常
     */
    public boolean isBlacklisted(String jti) {
        if (jti == null) {
            return false;
        }
        // 只需判断 key 是否存在；过期后 key 自动消失，自然返回 false
        return Boolean.TRUE.equals(redisTemplate.hasKey(PREFIX + jti));
    }
}
