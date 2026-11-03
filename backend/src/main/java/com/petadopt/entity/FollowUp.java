package com.petadopt.entity;

import lombok.Data;

/**
 * 回访记录表：宠物被领养后，志愿者定期回访并记录情况。
 */
@Data
public class FollowUp {
    private Long id;
    private Long adoptId;       // 领养记录 ID（外键）
    private Long volunteerId;   // 回访志愿者 ID（外键）
    private String content;     // 回访内容
    private String petCondition;// 宠物当前状况
    private String isNormal;    // 是否正常（是/否）
    private String followTime;  // 回访时间
    private String createTime;  // 记录创建时间

    // 联表查询字段
    private String volunteerName; // 志愿者姓名（来自 volunteer 表）
    private String petName;       // 宠物名称（来自 pet 表）
    private String petCoverImg;   // 宠物封面图（来自 pet 表）
    private String applicantName; // 领养人姓名（来自 adopt_application 表）
    private String phone;         // 领养人联系电话（来自 adopt_application 表，AES 加密，展示前需解密）
    private String auditTime;     // 领养通过时间（来自 adopt_application 表，用于计算领养天数）

    // 计算字段：领养天数（从领养通过到现在过了多少天，由 service 计算后填充）
    private Integer adoptDays;
}
