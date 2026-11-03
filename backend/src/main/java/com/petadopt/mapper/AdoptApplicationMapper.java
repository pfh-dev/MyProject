package com.petadopt.mapper;

import com.petadopt.entity.AdoptApplication;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 领养申请表的数据库操作接口（操作 adopt_application 表）。
 */
public interface AdoptApplicationMapper {

    /** 根据 ID 查询单条领养申请 */
    AdoptApplication selectById(Long id);

    /** 新增一条领养申请 */
    int insert(AdoptApplication application);

    /** 根据 ID 删除领养申请 */
    int deleteById(Long id);

    /** 按审核状态和关键词模糊查询申请列表（联表带出宠物名、用户名等） */
    List<AdoptApplication> selectList(@Param("status") String status, @Param("keyword") String keyword);

    /** 查询某个用户提交的所有申请 */
    List<AdoptApplication> selectByUserId(Long userId);

    /** 查询某个用户对某只宠物是否已提交过申请（用于防重复申请） */
    AdoptApplication selectByPetAndUser(@Param("petId") Long petId, @Param("userId") Long userId);

    /** 审核申请：更新状态、审核人、审核时间和审核意见 */
    int updateAudit(@Param("id") Long id,
                    @Param("status") String status,
                    @Param("auditUserId") Long auditUserId,
                    @Param("auditTime") String auditTime,
                    @Param("auditRemark") String auditRemark);
}
