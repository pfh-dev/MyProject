package com.petadopt.mapper;

import com.petadopt.entity.Rescue;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 救助信息表的数据操作接口（操作 rescue 表）。
 */
public interface RescueMapper {

    /** 根据 ID 查询单条救助信息 */
    Rescue selectById(Long id);

    /** 新增一条救助信息 */
    int insert(Rescue rescue);

    /** 更新救助信息 */
    int update(Rescue rescue);

    /** 根据 ID 删除救助信息 */
    int deleteById(Long id);

    /** 按状态和关键词查询救助列表（联表带出上报人、受理人姓名） */
    List<Rescue> selectList(@Param("status") String status, @Param("keyword") String keyword);

    /** 查询某个用户上报的救助信息 */
    List<Rescue> selectByReporterId(Long reporterId);

    /** 更新救助处理状态 */
    int updateStatus(@Param("id") Long id, @Param("status") String status);

    /** 分配受理人，并更新状态 */
    int assign(@Param("id") Long id, @Param("assigneeId") Long assigneeId, @Param("status") String status);

    /** 完成处理：更新状态、处理备注和处理时间 */
    int handle(@Param("id") Long id,
               @Param("status") String status,
               @Param("handleRemark") String handleRemark,
               @Param("handleTime") String handleTime);
}
