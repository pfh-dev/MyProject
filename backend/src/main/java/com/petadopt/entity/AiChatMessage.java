package com.petadopt.entity;

import lombok.Data;

/**
 * AI 聊天消息表：一次会话中的单条对话记录（一问一答各存一条）。
 */
@Data
public class AiChatMessage {
    private Long id;
    private String sessionId;   // 所属会话 ID（外键，关联 ai_chat_session）
    private String role;        // 消息角色（user 用户 / assistant AI）
    private String content;     // 消息内容
    private String createTime;  // 发送时间
}
