package com.petadopt.entity;

import lombok.Data;

/**
 * 志愿者申请表：用户申请成为志愿者，审核通过后可参与回访等工作。
 */
@Data
public class Volunteer {
    private Long id;
    private Long userId;        // 关联用户 ID（外键）
    private String realName;    // 真实姓名
    private String phone;       // 联系电话（AES 加密存储）
    private String skills;      // 技能特长
    private String availableTime; // 可服务时间段
    private String status;      // 审核状态（待审核/通过/驳回）
    private String applyTime;   // 申请时间
    private String auditTime;   // 审核时间

    // 联表查询字段
    private String username;    // 用户名（来自 user 表）
}
