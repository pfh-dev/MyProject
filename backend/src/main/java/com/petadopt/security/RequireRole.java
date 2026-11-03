package com.petadopt.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 角色访问控制注解，标注在 Controller 类或方法上。
 *
 * <p>用法示例：{@code @RequireRole("ADMIN")} 表示只有管理员才能访问。
 * 可以传多个角色：{@code @RequireRole({"ADMIN", "STAFF"})} 表示管理员和员工都可以。</p>
 *
 * <p>它本身不做校验，只是"打标签"；真正的校验在 {@link RoleInterceptor} 里读取这个注解并判断。</p>
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireRole {

    /**
     * 允许访问的角色列表，只要当前用户角色命中其中一个即可放行。
     */
    String[] value();
}
