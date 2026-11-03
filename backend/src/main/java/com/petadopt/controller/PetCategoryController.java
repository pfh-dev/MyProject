package com.petadopt.controller;

import com.petadopt.common.Result;
import com.petadopt.entity.PetCategory;
import com.petadopt.security.RequireRole;
import com.petadopt.service.PetCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 宠物分类管理接口
 * <p>
 * 负责宠物的「分类」数据（如猫、狗、兔子等品种/类型）的管理。
 * 其中「查询全部分类」对所有登录用户开放，新增/修改/删除则需管理员或员工权限。
 */
@RestController
@RequestMapping("/api/pet-category")
public class PetCategoryController {

    @Autowired
    private PetCategoryService categoryService;

    /**
     * 查询所有宠物分类（用于前台展示、下拉选择等）
     * <p>
     * 请求方式：GET /api/pet-category/all
     * 入参：无
     * 返回：Result&lt;List&lt;PetCategory&gt;&gt;，data 为全部分类列表
     * 权限：无（登录用户即可）
     */
    @GetMapping("/all")
    public Result<List<PetCategory>> all() {
        return Result.success(categoryService.all());
    }

    /**
     * 新增宠物分类
     * <p>
     * 请求方式：POST /api/pet-category
     * 入参：category（请求体，PetCategory 对象，含分类名称等信息）
     * 返回：Result&lt;Void&gt;
     * 权限：ADMIN、STAFF
     */
    @RequireRole({"ADMIN", "STAFF"})
    @PostMapping
    public Result<Void> add(@RequestBody PetCategory category) {
        categoryService.add(category);
        return Result.success();
    }

    /**
     * 修改宠物分类
     * <p>
     * 请求方式：PUT /api/pet-category
     * 入参：category（请求体，需携带分类 ID 及修改后的信息）
     * 返回：Result&lt;Void&gt;
     * 权限：ADMIN、STAFF
     */
    @RequireRole({"ADMIN", "STAFF"})
    @PutMapping
    public Result<Void> update(@RequestBody PetCategory category) {
        categoryService.update(category);
        return Result.success();
    }

    /**
     * 删除宠物分类
     * <p>
     * 请求方式：DELETE /api/pet-category/{id}
     * 入参：id（路径参数，分类 ID）
     * 返回：Result&lt;Void&gt;
     * 权限：ADMIN（仅管理员，删除属高风险操作）
     */
    @RequireRole({"ADMIN"})
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return Result.success();
    }
}
