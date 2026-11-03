package com.petadopt.ai;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ChatMessageDeserializer;
import dev.langchain4j.data.message.ChatMessageSerializer;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * 基于 Redis 的 AI 会话记忆存储（ChatMemoryStore 的实现）。
 *
 * 角色：这是 LangChain4j 的「记忆存储」抽象接口 ChatMemoryStore 的具体实现，
 * 负责把每次对话的上下文（一组 ChatMessage）真正持久化到 Redis。
 *
 * 为什么用 Redis 而不是内存？
 *  - 服务重启后记忆不丢失；
 *  - 天然的 key-value 结构，正好用 memoryId 当 key；
 *  - 多实例部署时共享同一份记忆。
 *
 * 存储约定：
 *  - key = memoryId（即会话 ID），value = 序列化后的 JSON 字符串；
 *  - 每条记忆设置 1 天过期（TTL），过期后自动清除，避免 Redis 无限膨胀。
 */
@Repository
public class RedisChatMemoryStore implements ChatMemoryStore {

    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 读取某个会话的历史记忆。
     * @param memoryId 会话 ID（作为 Redis 的 key）
     * @return 反序列化后的消息列表；若没有则返回空列表
     */
    @Override
    public List<ChatMessage> getMessages(Object memoryId) {
        String json = redisTemplate.opsForValue().get(memoryId);
        if (json == null) {
            return new ArrayList<>();
        }
        return ChatMessageDeserializer.messagesFromJson(json);
    }

    /**
     * 保存（覆盖写）某个会话的历史记忆。
     * @param memoryId 会话 ID
     * @param messages 最新的完整消息列表
     * 注意：这里是「整体覆盖」而非「追加」——每次把新的完整列表序列化后写回，并刷新 1 天过期时间。
     */
    @Override
    public void updateMessages(Object memoryId, List<ChatMessage> messages) {
        String json = ChatMessageSerializer.messagesToJson(messages);
        redisTemplate.opsForValue().set(memoryId.toString(), json, Duration.ofDays(1));
    }

    /**
     * 删除某个会话的记忆（例如会话被删除时清空对应上下文）。
     * @param memoryId 会话 ID
     */
    @Override
    public void deleteMessages(Object memoryId) {
        redisTemplate.delete(memoryId.toString());
    }
}
