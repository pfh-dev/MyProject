package com.petadopt.mapper;

import com.petadopt.entity.AiChatMessage;

import java.util.List;

/**
 * AI 聊天消息表的数据库操作接口（操作 ai_chat_message 表）。
 */
public interface AiChatMessageMapper {

    /** 新增一条聊天消息 */
    int insert(AiChatMessage message);

    /** 按会话 ID 查询该会话下的所有消息 */
    List<AiChatMessage> selectBySessionId(String sessionId);
}
