package com.petadopt.mapper;

import com.petadopt.entity.FollowUp;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 回访记录表的数据操作接口（操作 follow_up 表）。
 */
public interface FollowUpMapper {

    /** 新增一条回访记录 */
    int insert(FollowUp followUp);

    /** 查询某条领养记录下的所有回访记录 */
    List<FollowUp> selectByAdoptId(Long adoptId);

    /** 按关键词、回访状态查询回访记录列表（联表带出志愿者名、宠物名、领养人、领养通过时间等） */
    List<FollowUp> selectList(@Param("keyword") String keyword, @Param("status") String status);

    /** 根据 ID 删除回访记录 */
    int deleteById(Long id);

    /** 更新回访记录（登记回访：填写内容、宠物状况、是否正常等） */
    int updateById(FollowUp followUp);
}
