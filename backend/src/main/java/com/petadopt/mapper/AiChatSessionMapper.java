package com.petadopt.mapper;

import com.petadopt.entity.AiChatSession;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * AI 聊天会话表的数据库操作接口（操作 ai_chat_session 表）。
 */
public interface AiChatSessionMapper {

    /** 根据 ID 查询单个会话 */
    AiChatSession selectById(String id);

    /** 新建一个会话 */
    int insert(AiChatSession session);

    /** 查询某个用户的所有会话 */
    List<AiChatSession> selectByUserId(Long userId);

    /** 修改会话标题 */
    int updateTitle(@Param("id") String id, @Param("title") String title);
}
