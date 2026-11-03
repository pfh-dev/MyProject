package com.petadopt.controller;

import com.petadopt.common.PageResult;
import com.petadopt.common.Result;
import com.petadopt.entity.Pet;
import com.petadopt.security.RequireRole;
import com.petadopt.service.PetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 宠物管理接口
 * <p>
 * 负责「待领养宠物」档案的管理。既包括面向普通用户/游客的公开查询
 * （列表、详情），也包括面向管理员/员工的后台管理（分页查询、新增、
 * 修改、上下架、审核、删除）。审核（review）与上架状态（status）分开控制，
 * 保证宠物档案先审核通过再对外展示。
 */
@RestController
@RequestMapping("/api/pet")
public class PetController {

    @Autowired
    private PetService petService;

    /**
     * 前台公开的宠物列表（仅展示已审核通过、上架的宠物）
     * <p>
     * 请求方式：GET /api/pet/list
     * 入参：
     *   - name：按宠物名称模糊搜索（可选）
     *   - categoryId：按分类 ID 筛选（可选）
     *   - pageNum：页码，默认 1
     *   - pageSize：每页条数，默认 8
     * 返回：Result&lt;PageResult&lt;Pet&gt;&gt;
     * 权限：无（游客/登录用户均可查看）
     */
    @GetMapping("/list")
    public Result<PageResult<Pet>> list(@RequestParam(required = false) String name,
                                        @RequestParam(required = false) Long categoryId,
                                        @RequestParam(defaultValue = "1") int pageNum,
                                        @RequestParam(defaultValue = "8") int pageSize) {
        return Result.success(petService.publicPage(name, categoryId, pageNum, pageSize));
    }

    /**
     * 宠物详情
     * <p>
     * 请求方式：GET /api/pet/detail/{id}
     * 入参：id（路径参数，宠物 ID）
     * 返回：Result&lt;Pet&gt;，data 为该宠物的完整档案信息
     * 权限：无
     */
    @GetMapping("/detail/{id}")
    public Result<Pet> detail(@PathVariable Long id) {
        return Result.success(petService.detail(id));
    }

    /**
     * 后台分页查询宠物（含未审核、下架等所有状态）
     * <p>
     * 请求方式：GET /api/pet/page
     * 入参：
     *   - name：名称关键字（可选）
     *   - categoryId：分类 ID（可选）
     *   - status：上架状态筛选（可选）
     *   - reviewStatus：审核状态筛选（可选）
     *   - pageNum / pageSize：分页参数，默认 1 / 10
     * 返回：Result&lt;PageResult&lt;Pet&gt;&gt;
     * 权限：ADMIN、STAFF
     */
    @RequireRole({"ADMIN", "STAFF"})
    @GetMapping("/page")
    public Result<PageResult<Pet>> page(@RequestParam(required = false) String name,
                                        @RequestParam(required = false) Long categoryId,
                                        @RequestParam(required = false) String status,
                                        @RequestParam(required = false) String reviewStatus,
                                        @RequestParam(defaultValue = "1") int pageNum,
                                        @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(petService.page(name, categoryId, status, reviewStatus, pageNum, pageSize));
    }

    /**
     * 新增宠物档案
     * <p>
     * 请求方式：POST /api/pet
     * 入参：pet（请求体，Pet 对象，含名称、分类、照片、描述、救助背景等）
     * 返回：Result&lt;Void&gt;
     * 权限：ADMIN、STAFF
     */
    @RequireRole({"ADMIN", "STAFF"})
    @PostMapping
    public Result<Void> add(@RequestBody Pet pet) {
        petService.add(pet);
        return Result.success();
    }

    /**
     * 修改宠物档案
     * <p>
     * 请求方式：PUT /api/pet/{id}
     * 入参：
     *   - id：路径参数，宠物 ID
     *   - pet：请求体，修改后的宠物信息
     * 说明：此处会把路径中的 id 覆盖写入 pet 对象，再交给 service 更新，
     *       确保修改的是指定那条记录。
     * 返回：Result&lt;Void&gt;
     * 权限：ADMIN、STAFF
     */
    @RequireRole({"ADMIN", "STAFF"})
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody Pet pet) {
        pet.setId(id);
        petService.update(pet);
        return Result.success();
    }

    /**
     * 修改宠物上架 / 下架状态
     * <p>
     * 请求方式：PUT /api/pet/{id}/status
     * 入参：
     *   - id：路径参数，宠物 ID
     *   - status：请求参数，目标上架状态
     * 返回：Result&lt;Void&gt;
     * 权限：ADMIN、STAFF
     */
    @RequireRole({"ADMIN", "STAFF"})
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam String status) {
        petService.updateStatus(id, status);
        return Result.success();
    }

    /**
     * 审核宠物档案（通过 / 驳回）
     * <p>
     * 请求方式：PUT /api/pet/{id}/review
     * 入参：
     *   - id：路径参数，宠物 ID
     *   - reviewStatus：请求参数，审核结果（如通过 PASSED / 驳回 REJECTED）
     * 返回：Result&lt;Void&gt;
     * 权限：ADMIN、STAFF
     */
    @RequireRole({"ADMIN", "STAFF"})
    @PutMapping("/{id}/review")
    public Result<Void> review(@PathVariable Long id, @RequestParam String reviewStatus) {
        petService.review(id, reviewStatus);
        return Result.success();
    }

    /**
     * 删除宠物档案
     * <p>
     * 请求方式：DELETE /api/pet/{id}
     * 入参：id（路径参数，宠物 ID）
     * 返回：Result&lt;Void&gt;
     * 权限：ADMIN（仅管理员可删除）
     */
    @RequireRole({"ADMIN"})
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        petService.delete(id);
        return Result.success();
    }
}
