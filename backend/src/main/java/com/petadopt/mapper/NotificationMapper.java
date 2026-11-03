package com.petadopt.mapper;

import com.petadopt.entity.Notification;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 通知表的数据操作接口（操作 notification 表）。
 */
public interface NotificationMapper {

    /** 新增一条通知 */
    int insert(Notification notification);

    /** 查询某个用户的所有通知 */
    List<Notification> selectByUserId(Long userId);

    /** 将单条通知标记为已读 */
    int updateRead(@Param("id") Long id);

    /** 将某个用户的所有通知标记为已读 */
    int updateReadAll(Long userId);

    /** 统计某个用户的未读通知数量 */
    long countUnread(Long userId);
}
