package com.petadopt.service;

import com.petadopt.entity.PetCategory;
import com.petadopt.mapper.PetCategoryMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 宠物分类服务
 * <p>
 * 负责宠物种类（如猫、狗、兔子等）的查询与后台维护。
 * 分类是宠物档案的基础字典数据，前端发布宠物、按分类筛选列表时都会用到。
 */
@Service
public class PetCategoryService {

    @Autowired
    private PetCategoryMapper categoryMapper;

    /**
     * 查询全部分类（供前端下拉框/筛选栏使用）
     *
     * @return 分类列表
     */
    public List<PetCategory> all() {
        return categoryMapper.selectAll();
    }

    /**
     * 新增分类
     *
     * @param category 分类实体
     */
    public void add(PetCategory category) {
        categoryMapper.insert(category);
    }

    /**
     * 修改分类
     *
     * @param category 分类实体
     */
    public void update(PetCategory category) {
        categoryMapper.update(category);
    }

    /**
     * 删除分类
     *
     * @param id 分类 id
     */
    public void delete(Long id) {
        categoryMapper.deleteById(id);
    }
}
