package com.petadopt.entity;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 捐赠记录表：用户对平台或救助对象的捐款捐物。
 */
@Data
public class Donation {
    private Long id;
    private Long userId;        // 捐赠人 ID（外键）
    private String type;        // 捐赠类型（钱款/物资）
    private BigDecimal amount;  // 捐赠金额（捐物时为 0）
    private String itemName;    // 物资名称
    private Integer quantity;   // 物资数量
    private String description; // 备注说明
    private String status;      // 处理状态（待处理/已接收等）
    private String donateTime;  // 捐赠时间

    // 联表查询字段
    private String username;    // 捐赠人用户名（来自 user 表）
}
