package com.petadopt.mapper;

import com.petadopt.entity.PetCategory;

import java.util.List;

/**
 * 宠物分类表的数据操作接口（操作 pet_category 表）。
 */
public interface PetCategoryMapper {

    /** 查询所有分类 */
    List<PetCategory> selectAll();

    /** 新增分类 */
    int insert(PetCategory category);

    /** 更新分类 */
    int update(PetCategory category);

    /** 根据 ID 删除分类 */
    int deleteById(Long id);
}
