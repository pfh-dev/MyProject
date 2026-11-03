package com.petadopt.entity;

import lombok.Data;

/**
 * 操作日志表：记录管理员等用户的关键操作，便于审计追溯。
 */
@Data
public class OperationLog {
    private Long id;
    private Long userId;     // 操作人 ID（外键）
    private String username; // 操作人用户名
    private String module;   // 操作模块（如宠物管理、用户管理）
    private String operation;// 操作动作（如新增、删除、审核）
    private String params;   // 操作参数（通常为 JSON 字符串）
    private String ip;       // 操作人 IP 地址
    private String createTime; // 操作时间
}
