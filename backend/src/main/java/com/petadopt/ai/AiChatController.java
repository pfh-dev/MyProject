package com.petadopt.ai;

import com.petadopt.common.Result;
import com.petadopt.entity.AiChatMessage;
import com.petadopt.entity.AiChatSession;
import com.petadopt.security.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * AI 对话相关的 HTTP 接口层（Controller）。
 *
 * 角色：把前端的聊天请求转发给底层的 AI Agent（PetAssistantService），
 * 同时负责「聊天会话 / 聊天记录」的增查，供前端展示历史记录。
 *
 * 对外统一挂在 /api/ai 路径下，返回结果用项目统一的 Result 包装类。
 */
@RestController
@RequestMapping("/api/ai")
public class AiChatController {

    @Autowired
    private PetAssistantService assistantService;

    @Autowired
    private AiChatService aiChatService;

    /**
     * 流式对话（纯文本流，前端 ReadableStream 逐 token 渲染）。
     *
     * 什么是「流式输出」：AI 不是一次性把整段回答返回，而是一个字一个字地往外吐。
     * 这里返回的是 Reactor 的 Flux<String>——一个「异步的、可分多次发射的字符串序列」，
     * 每收到一小段回答就立刻推给前端，前端就能像 ChatGPT 一样逐字显示，体验更流畅。
     *
     * 参数：
     *  - memoryId：会话记忆的唯一标识（通常是会话 ID），用于区分不同用户/不同对话的记忆。
     *  - message：用户本次输入的问题。
     *
     * 处理流程：
     *  1. 先把用户这句话落库（role=user），方便前端展示历史记录；
     *  2. 调用 AI Agent 的 chat 方法，得到一个 Flux 流；
     *  3. doOnNext：每来一段回答就拼进 StringBuilder（边收边记）；
     *  4. doOnComplete：当流全部结束时，把拼接好的完整回答落库（role=assistant）。
     */
    @GetMapping(value = "/chat", produces = "text/plain;charset=UTF-8")
    public Flux<String> chat(@RequestParam String memoryId, @RequestParam String message) {
        aiChatService.saveMessage(memoryId, "user", message);
        StringBuilder sb = new StringBuilder();
        return assistantService.chat(memoryId, message)
                .doOnNext(sb::append)
                .doOnComplete(() -> aiChatService.saveMessage(memoryId, "assistant", sb.toString()));
    }

    /**
     * 智能匹配推荐（非流式）。
     *
     * 与 /chat 不同，这里走的是 recommend（非流式）方法：一次性拿到完整推荐文本再返回。
     * 参数 petId：宠物 ID，从 URL 路径中提取（@PathVariable）。
     * 内部拼好提示词，让 AI 结合该宠物的详细信息生成领养推荐文案。
     */
    @GetMapping("/match/{petId}")
    public Result<String> match(@PathVariable Long petId) {
        String result = assistantService.recommend(
                "宠物ID为 " + petId + " 的宠物，请结合它的详细信息，生成一段领养匹配推荐，"
                        + "说明它适合什么样的领养人、领养后需要注意什么。");
        return Result.success(result);
    }

    /**
     * 查询当前用户的所有 AI 会话列表。
     * UserContext.userId() 从登录态（安全上下文）里取出当前用户 ID，保证只查到自己的会话。
     */
    @GetMapping("/sessions")
    public Result<List<AiChatSession>> sessions() {
        return Result.success(aiChatService.sessions(UserContext.userId()));
    }

    /**
     * 新建 / 保存一个 AI 会话。
     * 前端传进来的 session 可能不带用户 ID，这里强制写入当前登录用户的 ID，防止越权。
     */
    @PostMapping("/session")
    public Result<Void> saveSession(@RequestBody AiChatSession session) {
        session.setUserId(UserContext.userId());
        aiChatService.saveSession(session);
        return Result.success();
    }

    /**
     * 查询某个会话下的全部聊天消息（按会话 ID 拉取历史记录）。
     */
    @GetMapping("/messages/{sessionId}")
    public Result<List<AiChatMessage>> messages(@PathVariable String sessionId) {
        return Result.success(aiChatService.messages(sessionId));
    }
}
