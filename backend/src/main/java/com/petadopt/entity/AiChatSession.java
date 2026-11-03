package com.petadopt.entity;

import lombok.Data;

/**
 * AI 聊天会话表：用户与 AI 的一次完整对话（一个会话下有多条消息）。
 */
@Data
public class AiChatSession {
    private String id;          // 会话 ID（字符串类型，通常为 UUID）
    private Long userId;        // 所属用户 ID（外键）
    private String title;       // 会话标题
    private String createTime;  // 创建时间
}
