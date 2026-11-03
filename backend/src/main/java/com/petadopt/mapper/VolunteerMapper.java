package com.petadopt.mapper;

import com.petadopt.entity.Volunteer;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 志愿者申请表的数据操作接口（操作 volunteer 表）。
 */
public interface VolunteerMapper {

    /** 根据 ID 查询单条志愿者申请 */
    Volunteer selectById(Long id);

    /** 根据用户 ID 查询该用户的志愿者申请 */
    Volunteer selectByUserId(Long userId);

    /** 新增志愿者申请 */
    int insert(Volunteer volunteer);

    /** 根据 ID 删除志愿者申请 */
    int deleteById(Long id);

    /** 按状态和关键词查询志愿者申请列表 */
    List<Volunteer> selectList(@Param("status") String status, @Param("keyword") String keyword);

    /** 查询已审核通过的志愿者列表 */
    List<Volunteer> selectApproved();

    /** 更新志愿者审核状态 */
    int updateStatus(@Param("id") Long id, @Param("status") String status);
}
