package com.petadopt.entity;

import lombok.Data;

/**
 * 救助信息表：用户上报需要救助的流浪/受伤动物，由平台分配处理。
 */
@Data
public class Rescue {
    private Long id;
    private String title;       // 救助标题
    private String description; // 情况描述
    private String images;      // 现场图片（多张）
    private String location;    // 发现地点
    private String contact;     // 联系方式（AES 加密存储）
    private String status;      // 处理状态（待受理/处理中/已完成等）
    private Long reporterId;    // 上报人 ID（外键）
    private Long assigneeId;    // 受理人/负责人 ID（外键）
    private String handleRemark;// 处理备注
    private String createTime;  // 上报时间
    private String handleTime;  // 处理完成时间

    // 联表查询字段
    private String reporterName;  // 上报人姓名（来自 user 表）
    private String assigneeName;  // 受理人姓名（来自 user 表）
}
