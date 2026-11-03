package com.petadopt.service;

import com.petadopt.entity.Notification;
import com.petadopt.mapper.NotificationMapper;
import com.petadopt.util.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 站内通知服务
 * <p>
 * 负责平台消息的发送与查询，是很多业务动作（审核通过/拒绝、捐赠确认、救助处理等）
 * 完成后的「收尾环节」：各业务 Service 在关键节点调用 send() 给用户推送一条站内通知，
 * 从而把「业务动作」和「用户提醒」串起来。
 */
@Service
public class NotificationService {

    @Autowired
    private NotificationMapper notificationMapper;

    /**
     * 发送一条站内通知
     * <p>
     * 接收方 userId 为空时直接返回（例如匿名捐赠没有绑定用户），避免插入脏数据。
     * type 用于区分通知来源（如"系统""领养""救助"），bizId 记录关联的业务主键，方便前端跳转详情。
     *
     * @param userId  接收通知的用户 id
     * @param title   通知标题
     * @param content 通知内容
     * @param type    通知类型/来源
     * @param bizId   关联的业务 id（如领养申请 id、救助线索 id），可为空
     */
    public void send(Long userId, String title, String content, String type, Long bizId) {
        // 没有接收人时直接放弃发送，防止空指针与无效通知
        if (userId == null) {
            return;
        }
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setType(type);
        notification.setBizId(bizId);
        notification.setIsRead(0); // 新通知默认为未读
        notification.setCreateTime(DateUtil.now());
        notificationMapper.insert(notification);
    }

    /**
     * 查询某用户的全部通知列表
     *
     * @param userId 用户 id
     * @return 该用户的通知列表
     */
    public List<Notification> myList(Long userId) {
        return notificationMapper.selectByUserId(userId);
    }

    /**
     * 将某条通知标记为已读
     *
     * @param id 通知 id
     */
    public void read(Long id) {
        notificationMapper.updateRead(id);
    }

    /**
     * 将某用户的全部通知一键标记为已读
     *
     * @param userId 用户 id
     */
    public void readAll(Long userId) {
        notificationMapper.updateReadAll(userId);
    }

    /**
     * 统计某用户的未读通知数量（用于前端红点/角标提醒）
     *
     * @param userId 用户 id
     * @return 未读通知数量
     */
    public long unreadCount(Long userId) {
        return notificationMapper.countUnread(userId);
    }
}
