package com.petadopt.mapper;

import com.petadopt.entity.Pet;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 宠物档案表的数据操作接口（操作 pet 表）。
 */
public interface PetMapper {

    /** 根据 ID 查询单只宠物（联表带出分类名） */
    Pet selectById(Long id);

    /** 新增宠物档案 */
    int insert(Pet pet);

    /** 更新宠物档案 */
    int update(Pet pet);

    /** 根据 ID 删除宠物档案 */
    int deleteById(Long id);

    /** 按名称、分类、状态、审核状态多条件查询（后台管理用） */
    List<Pet> selectList(@Param("name") String name,
                         @Param("categoryId") Long categoryId,
                         @Param("status") String status,
                         @Param("reviewStatus") String reviewStatus);

    /** 查询公开可领养的宠物列表（前台展示，只含已审核通过的） */
    List<Pet> selectPublicList(@Param("name") String name, @Param("categoryId") Long categoryId);

    /** 更新宠物领养状态 */
    int updateStatus(@Param("id") Long id, @Param("status") String status);

    /** 更新宠物审核状态 */
    int updateReviewStatus(@Param("id") Long id, @Param("reviewStatus") String reviewStatus);
}
