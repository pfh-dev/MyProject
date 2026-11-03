package com.petadopt.controller;

import com.petadopt.common.Result;
import com.petadopt.entity.Notification;
import com.petadopt.security.UserContext;
import com.petadopt.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 消息通知接口
 * <p>
 * 负责用户「站内消息/通知」的读取：查看我的通知列表、未读数量，
 * 以及把单条或全部通知标记为已读。所有接口都基于当前登录用户（UserContext.userId()），
 * 因此无需在入参中传用户 ID。
 */
@RestController
@RequestMapping("/api/notification")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    /**
     * 查看「我」的通知列表
     * <p>
     * 请求方式：GET /api/notification/my
     * 入参：无（后端通过 UserContext.userId() 获取当前用户）
     * 返回：Result&lt;List&lt;Notification&gt;&gt;
     * 权限：无（登录用户即可）
     */
    @GetMapping("/my")
    public Result<List<Notification>> my() {
        return Result.success(notificationService.myList(UserContext.userId()));
    }

    /**
     * 获取「我」的未读通知数量
     * <p>
     * 请求方式：GET /api/notification/unread-count
     * 入参：无
     * 返回：Result&lt;Map&lt;String,Object&gt;&gt;，data 中的 "count" 键对应未读数量
     * 权限：无（登录用户即可）
     */
    @GetMapping("/unread-count")
    public Result<Map<String, Object>> unreadCount() {
        Map<String, Object> map = new HashMap<>();
        map.put("count", notificationService.unreadCount(UserContext.userId()));
        return Result.success(map);
    }

    /**
     * 将某条通知标记为已读
     * <p>
     * 请求方式：PUT /api/notification/{id}/read
     * 入参：id（路径参数，通知 ID）
     * 返回：Result&lt;Void&gt;
     * 权限：无（登录用户即可）
     */
    @PutMapping("/{id}/read")
    public Result<Void> read(@PathVariable Long id) {
        notificationService.read(id);
        return Result.success();
    }

    /**
     * 将「我」的全部通知标记为已读
     * <p>
     * 请求方式：PUT /api/notification/read-all
     * 入参：无（后端根据当前用户批量处理）
     * 返回：Result&lt;Void&gt;
     * 权限：无（登录用户即可）
     */
    @PutMapping("/read-all")
    public Result<Void> readAll() {
        notificationService.readAll(UserContext.userId());
        return Result.success();
    }
}
