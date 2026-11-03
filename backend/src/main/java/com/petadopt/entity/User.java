package com.petadopt.entity;

import lombok.Data;

/**
 * 用户表：平台所有账号，通过 role 区分管理员、员工、志愿者、普通用户。
 */
@Data
public class User {
    private Long id;
    private String username;    // 登录用户名
    private String password;    // 密码（BCrypt 加密存储，非明文）
    private String nickname;    // 昵称
    private String realName;    // 真实姓名
    private String phone;       // 手机号（AES 加密存储）
    private String email;       // 邮箱
    private String avatar;      // 头像地址
    private String gender;      // 性别
    private Integer age;        // 年龄
    private String address;     // 地址
    private String description; // 个人简介
    private String role;        // 角色（ADMIN/STAFF/VOLUNTEER/USER）
    private Integer status;     // 账号状态（1 正常 / 0 禁用）
    private String createTime;  // 注册时间
    private String updateTime;  // 更新时间
}
