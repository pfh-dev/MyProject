package com.petadopt.entity;

import lombok.Data;

/**
 * 领养申请表：用户对某只宠物提交领养申请，等待管理员审核。
 */
@Data
public class AdoptApplication {
    private Long id;
    private Long petId;             // 被申请领养的宠物 ID（外键）
    private Long userId;            // 提交申请的用户 ID（外键）
    private String applicantName;   // 申请人姓名
    private String phone;           // 联系电话（AES 加密存储）
    private String address;         // 联系地址
    private String occupation;      // 职业
    private String hasExperience;   // 是否有养宠经验（是/否）
    private String reason;          // 领养理由
    private String status;          // 审核状态（待审核/通过/驳回）
    private String applyTime;       // 申请时间
    private Long auditUserId;       // 审核人 ID（外键）
    private String auditTime;       // 审核时间
    private String auditRemark;     // 审核意见/备注

    // 联表查询字段
    private String petName;         // 宠物名称（来自 pet 表）
    private String petCoverImg;     // 宠物封面图（来自 pet 表）
    private String username;        // 申请人用户名（来自 user 表）
}
