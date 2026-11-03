package com.petadopt.ai;

import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * AI 会话记忆配置类（Spring @Configuration）。
 *
 * 这里解决的是 LangChain4j 里一个核心概念——「会话记忆（Chat Memory）」：
 * 大模型本身是无状态的，你每次提问它都「失忆」。要让 AI 记住上一句聊了什么，
 * 就必须把历史对话保存起来，并在下次提问时一起发给模型。
 *
 * 本类的设计目标是「每个 memoryId 一份独立记忆」：
 *  - 通过 Redis（ChatMemoryStore）把记忆持久化，重启不丢；
 *  - 用 memoryId（通常是会话 ID）把不同用户、不同对话的记忆隔离开，互不串扰。
 */
@Configuration
public class AiConfig {

    @Autowired
    private ChatMemoryStore chatMemoryStore;

    /**
     * 定义一个「默认的」会话记忆 Bean（chatMemory）。
     * MessageWindowChatMemory 采用「滑动窗口」策略：只保留最近 maxMessages(20) 条消息，
     * 超过就丢掉最旧的，避免上下文越堆越长、消耗过多 token。
     * 这个 Bean 主要供 @AiService 里 chatMemory = "chatMemory" 引用。
     */
    @Bean
    public ChatMemory chatMemory() {
        return MessageWindowChatMemory.builder().maxMessages(20).build();
    }

    /**
     * 定义一个「按 memoryId 动态提供记忆」的 Provider（chatMemoryProvider）。
     *
     * 为什么需要它？因为默认的 chatMemory 是全局共享一份记忆，所有用户会互相看到对方的上下文。
     * 而 ChatMemoryProvider 是一个函数式接口：每次调用都根据传入的 memoryId 重新构建一份记忆，
     * 并指定：
     *  - id(memoryId)：这份记忆归属哪个会话；
     *  - chatMemoryStore(chatMemoryStore)：读写走 Redis，实现持久化 + 隔离。
     *
     * 这样不同 memoryId 的用户就各用各的记忆，不会串话。
     */
    @Bean
    public ChatMemoryProvider chatMemoryProvider() {
        return memoryId -> MessageWindowChatMemory.builder()
                .id(memoryId)
                .maxMessages(20)
                .chatMemoryStore(chatMemoryStore)
                .build();
    }
}
