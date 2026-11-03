package com.petadopt.common.enums;

/**
 * 用户角色枚举。
 *
 * <p>系统把用户分成四类，权限从大到小：</p>
 * <ul>
 *   <li>ADMIN —— 管理员：拥有最高权限，可管理用户、审核等；</li>
 *   <li>STAFF —— 员工：平台工作人员，处理领养、救助等业务；</li>
 *   <li>VOLUNTEER —— 志愿者：协助开展救助、回访等工作；</li>
 *   <li>USER —— 普通用户：来领养宠物的普通访客。</li>
 * </ul>
 *
 * <p>角色配合 {@code @RequireRole} 注解和 {@code RoleInterceptor} 拦截器一起，
 * 实现对接口的精细化权限控制。</p>
 */
public enum Role {
    ADMIN("管理员"),
    STAFF("员工"),
    VOLUNTEER("志愿者"),
    USER("普通用户");

    /** 角色的中文显示名，用于前端展示 */
    private final String label;

    Role(String label) {
        this.label = label;
    }

    /**
     * 获取角色中文名。
     */
    public String getLabel() {
        return label;
    }
}
