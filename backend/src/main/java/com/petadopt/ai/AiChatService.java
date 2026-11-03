package com.petadopt.ai;

import com.petadopt.entity.AiChatMessage;
import com.petadopt.entity.AiChatSession;
import com.petadopt.mapper.AiChatMessageMapper;
import com.petadopt.mapper.AiChatSessionMapper;
import com.petadopt.util.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * AI 聊天记录的持久化服务（Service 层）。
 *
 * 角色：负责把「会话（AiChatSession）」和「聊天消息（AiChatMessage）」写入 / 读出数据库。
 * 注意它和 AI Agent（PetAssistantService）分工不同：
 *  - 这里只存「给人看的聊天历史」，通过 MyBatis Mapper 操作数据库；
 *  - 真正的「AI 上下文记忆」由 Redis（RedisChatMemoryStore）负责，两者互不干扰。
 */
@Service
public class AiChatService {

    @Autowired
    private AiChatSessionMapper sessionMapper;

    @Autowired
    private AiChatMessageMapper messageMapper;

    /**
     * 查询某个用户的所有会话。
     * @param userId 用户 ID
     * @return 该用户的会话列表
     */
    public List<AiChatSession> sessions(Long userId) {
        return sessionMapper.selectByUserId(userId);
    }

    /**
     * 新建一个会话：补上创建时间后写入数据库。
     * @param session 会话对象（通常由前端传入标题等信息）
     */
    public void saveSession(AiChatSession session) {
        session.setCreateTime(DateUtil.now());
        sessionMapper.insert(session);
    }

    /**
     * 更新会话标题（例如用户给对话重命名）。
     * @param id 会话 ID
     * @param title 新的标题
     */
    public void updateTitle(String id, String title) {
        sessionMapper.updateTitle(id, title);
    }

    /**
     * 查询某个会话下的所有聊天消息。
     * @param sessionId 会话 ID
     * @return 消息列表
     */
    public List<AiChatMessage> messages(String sessionId) {
        return messageMapper.selectBySessionId(sessionId);
    }

    /**
     * 保存一条聊天消息。
     * @param sessionId 所属会话 ID
     * @param role 消息角色（"user" 用户 / "assistant" AI）
     * @param content 消息内容
     */
    public void saveMessage(String sessionId, String role, String content) {
        AiChatMessage message = new AiChatMessage();
        message.setSessionId(sessionId);
        message.setRole(role);
        message.setContent(content);
        message.setCreateTime(DateUtil.now());
        messageMapper.insert(message);
    }
}
