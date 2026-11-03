package com.petadopt.entity;

import lombok.Data;

/**
 * 通知表：系统给用户推送的消息（如审核结果、进度提醒）。
 */
@Data
public class Notification {
    private Long id;
    private Long userId;    // 接收用户 ID（外键）
    private String title;   // 通知标题
    private String content; // 通知内容
    private String type;    // 通知类型（领养/救助/系统等）
    private Long bizId;     // 关联的业务 ID（如领养申请 ID）
    private Integer isRead; // 是否已读（1 已读 / 0 未读）
    private String createTime; // 创建时间
}
