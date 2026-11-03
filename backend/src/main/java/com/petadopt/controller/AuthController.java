package com.petadopt.controller;

import com.petadopt.common.Result;
import com.petadopt.dto.LoginRequest;
import com.petadopt.dto.PasswordRequest;
import com.petadopt.dto.RegisterRequest;
import com.petadopt.entity.User;
import com.petadopt.service.AuthService;
import com.petadopt.vo.LoginVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证 / 账户接口
 * <p>
 * 负责用户「注册、登录、退出、查看个人信息、修改密码、修改资料」等基础账户操作。
 * 注册和登录返回登录凭证信息（LoginVO，含 token 与用户信息），其余接口依赖登录状态。
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    /**
     * 用户注册
     * <p>
     * 请求方式：POST /api/auth/register
     * 入参：request（请求体，RegisterRequest，含用户名、密码等，@Validated 做参数校验）
     * 返回：Result&lt;LoginVO&gt;，data 含 token 和用户信息（注册成功即视为已登录）
     * 权限：无（匿名可访问）
     */
    @PostMapping("/register")
    public Result<LoginVO> register(@Validated @RequestBody RegisterRequest request) {
        return Result.success(authService.register(request));
    }

    /**
     * 用户登录
     * <p>
     * 请求方式：POST /api/auth/login
     * 入参：request（请求体，LoginRequest，含账号、密码）
     * 返回：Result&lt;LoginVO&gt;，data 含 token 和用户信息
     * 权限：无（匿名可访问）
     */
    @PostMapping("/login")
    public Result<LoginVO> login(@Validated @RequestBody LoginRequest request) {
        return Result.success(authService.login(request));
    }

    /**
     * 获取当前登录用户的信息
     * <p>
     * 请求方式：GET /api/auth/info
     * 入参：无（后端根据 token 识别当前用户）
     * 返回：Result&lt;User&gt;，data 为当前用户信息
     * 权限：无（需登录）
     */
    @GetMapping("/info")
    public Result<User> info() {
        return Result.success(authService.info());
    }

    /**
     * 退出登录
     * <p>
     * 请求方式：POST /api/auth/logout
     * 入参：无
     * 返回：Result&lt;Void&gt;
     * 权限：无（需登录）
     */
    @PostMapping("/logout")
    public Result<Void> logout() {
        authService.logout();
        return Result.success();
    }

    /**
     * 修改密码
     * <p>
     * 请求方式：PUT /api/auth/password
     * 入参：request（请求体，PasswordRequest，含旧密码、新密码，@Validated 做校验）
     * 返回：Result&lt;Void&gt;
     * 权限：无（需登录）
     */
    @PutMapping("/password")
    public Result<Void> password(@Validated @RequestBody PasswordRequest request) {
        authService.changePassword(request);
        return Result.success();
    }

    /**
     * 修改个人资料（如昵称、头像、联系方式等）
     * <p>
     * 请求方式：PUT /api/auth/profile
     * 入参：request（请求体，User 对象，含要更新的资料字段）
     * 返回：Result&lt;Void&gt;
     * 权限：无（需登录）
     */
    @PutMapping("/profile")
    public Result<Void> profile(@RequestBody User request) {
        authService.updateProfile(request);
        return Result.success();
    }
}
