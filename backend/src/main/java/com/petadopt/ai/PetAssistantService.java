package com.petadopt.ai;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.spring.AiService;
import dev.langchain4j.service.spring.AiServiceWiringMode;
import reactor.core.publisher.Flux;

/**
 * 宠物咨询 AI Agent（声明式接口）。
 *
 * 这是整个 AI 模块的核心，体现了 LangChain4j 的「Agent 范式」：
 * 你只需要写一个普通接口 + 几个注解，LangChain4j 就会在启动时自动生成实现类，
 * 帮你完成「拼提示词 → 调用大模型 → 记忆管理 → 工具调用 → 返回结果」整套流程。
 *
 * 注解 @AiService 说明：
 *  - wiringMode = EXPLICIT：显式装配模式，下面每个组件都按「Bean 名称」手动指定，
 *    而不是让框架去猜，更清晰可控。
 *  - chatModel = "openAiChatModel"：非流式聊天模型（一次返回完整回答）。
 *  - streamingChatModel = "openAiStreamingChatModel"：流式聊天模型（逐 token 输出）。
 *  - chatMemory = "chatMemory"：默认会话记忆。
 *  - chatMemoryProvider = "chatMemoryProvider"：按 memoryId 提供隔离记忆。
 *  - tools = "petTools"：绑定一个工具 Bean，让 AI 在需要时能调用 PetTools 里的方法查数据库。
 *
 * 关于「工具调用（Tool Calling）」：当用户问「有哪些猫咪可以领养」时，
 * 大模型本身看不到数据库，它会主动决定调用 searchPets 工具拿到真实数据，再组织成回答。
 */
@AiService(
        wiringMode = AiServiceWiringMode.EXPLICIT,
        chatModel = "openAiChatModel",
        streamingChatModel = "openAiStreamingChatModel",
        chatMemory = "chatMemory",
        chatMemoryProvider = "chatMemoryProvider",
        tools = "petTools"
)
public interface PetAssistantService {

    /**
     * 流式对话方法（返回 Flux<String>，逐段输出）。
     *
     * 参数注解含义：
     *  - @MemoryId String memoryId：标记这是「记忆 ID」，框架会根据它从 Redis 取/存对应的历史上下文。
     *  - @UserMessage String message：标记这是用户本次说的话，会被拼进提示词发给模型。
     *
     * @SystemMessage(fromResource = "prompt/system-prompt.txt")：
     *  - 从 classpath 下的 prompt/system-prompt.txt 读取「系统提示词」（角色设定），
     *    每次请求都会把这段人设放在最前面，告诉 AI「你是谁、该怎么回答」。
     */
    @SystemMessage(fromResource = "prompt/system-prompt.txt")
    Flux<String> chat(@MemoryId String memoryId, @UserMessage String message);

    /**
     * 非流式推荐方法（返回完整 String，一次拿全）。
     *
     * 与 chat 的区别：没有 @MemoryId，即「不带记忆」——推荐场景不需要上下文，
     * 每次都是独立的一次性问答。@UserMessage 同样表示用户输入。
     */
    @SystemMessage(fromResource = "prompt/system-prompt.txt")
    String recommend(@UserMessage String message);
}
