package com.petadopt.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 密码加密配置：向 Spring 容器注册一个 BCrypt 密码编码器。
 *
 * <p>为什么要用 BCrypt：</p>
 * <ul>
 *   <li>它是单向哈希算法，只能加密不能解密，即使数据库泄露，攻击者也无法还原明文密码；</li>
 *   <li>每次加密会自动掺入随机"盐"，同一个密码两次加密结果不同，无法用彩虹表破解；</li>
 *   <li>登录校验时用 {@code matches()} 方法比对，而不是解密后比较。</li>
 * </ul>
 *
 * <p>注册成 Bean 后，系统里任何地方注入 {@link PasswordEncoder} 都能用它来
 * 加密和校验密码，保证全项目密码处理逻辑一致。</p>
 */
@Configuration
public class PasswordConfig {

    /**
     * 提供一个 BCrypt 密码编码器 Bean。
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
