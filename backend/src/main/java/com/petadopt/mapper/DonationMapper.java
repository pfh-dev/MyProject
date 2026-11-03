package com.petadopt.mapper;

import com.petadopt.entity.Donation;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 捐赠记录表的数据操作接口（操作 donation 表）。
 */
public interface DonationMapper {

    /** 新增一条捐赠记录 */
    int insert(Donation donation);

    /** 根据 ID 查询单条捐赠记录 */
    Donation selectById(Long id);

    /** 按状态和关键词查询捐赠列表（联表带出捐赠人用户名） */
    List<Donation> selectList(@Param("status") String status, @Param("keyword") String keyword);

    /** 查询某个用户的捐赠记录 */
    List<Donation> selectByUserId(Long userId);

    /** 更新捐赠处理状态 */
    int updateStatus(@Param("id") Long id, @Param("status") String status);
}
