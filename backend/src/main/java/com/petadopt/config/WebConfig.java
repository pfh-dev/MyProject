package com.petadopt.config;

import com.petadopt.security.LoginInterceptor;
import com.petadopt.security.RoleInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web 层全局配置：负责注册拦截器和 CORS 跨域规则。
 *
 * <p>这是 Spring MVC 的"中央调度站"，两件大事都在这里安排：</p>
 * <ol>
 *   <li>哪些接口要被登录/角色拦截器拦截，哪些可以匿名访问；</li>
 *   <li>允许哪些来源、哪些方法跨域访问后端（前后端分离项目的刚需）。</li>
 * </ol>
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private LoginInterceptor loginInterceptor;

    @Autowired
    private RoleInterceptor roleInterceptor;

    /**
     * 注册拦截器及其拦截规则。
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 登录拦截器：默认拦截所有 /api/** 接口
        registry.addInterceptor(loginInterceptor)
                .addPathPatterns("/api/**")
                // 但下面这些接口允许匿名访问（登录、注册、公开浏览等），
                // 否则用户连登录都进不去，会陷入"死锁"。
                .excludePathPatterns(
                        "/api/auth/login",          // 登录
                        "/api/auth/register",       // 注册
                        "/api/pet/list",            // 宠物列表（公开）
                        "/api/pet/detail/**",       // 宠物详情（公开）
                        "/api/pet-category/all",    // 宠物分类
                        "/api/banner/all",          // 首页轮播图
                        "/api/article/list",        // 文章列表
                        "/api/article/detail/**",   // 文章详情
                        "/api/file/download/**"     // 文件下载
                );

        // 角色拦截器：对所有 /api/** 生效，但只在标了 @RequireRole 的接口上才真正校验
        registry.addInterceptor(roleInterceptor)
                .addPathPatterns("/api/**");
    }

    /**
     * 配置 CORS 跨域访问规则。
     *
     * <p>前后端分离时，前端（如 localhost:3000）和后端（如 localhost:8080）端口不同，
     * 浏览器出于安全会默认禁止这种跨域请求。这里放开规则，让前端能正常调用后端接口。</p>
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                // 允许任意来源（开发期方便；生产可收紧为具体域名）
                .allowedOriginPatterns("*")
                // 允许的 HTTP 方法
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                // 允许携带任意请求头（如 Authorization 令牌头）
                .allowedHeaders("*")
                // 允许携带凭证（Cookie 等）
                .allowCredentials(true)
                // 预检请求结果缓存 1 小时，减少重复预检
                .maxAge(3600);
    }
}
