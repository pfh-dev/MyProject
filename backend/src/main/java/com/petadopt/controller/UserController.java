package com.petadopt.controller;

import com.petadopt.common.PageResult;
import com.petadopt.common.Result;
import com.petadopt.entity.User;
import com.petadopt.security.RequireRole;
import com.petadopt.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户管理接口
 * <p>
 * 负责平台后台对「用户账号」的管理操作：分页查询用户、启用/禁用账号、
 * 调整用户角色、删除用户。本类所有接口仅限 ADMIN（管理员）角色调用，
 * 因此在类上统一标注了 @RequireRole({"ADMIN"})。
 */
@RestController
@RequestMapping("/api/user")
@RequireRole({"ADMIN"})
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 分页查询用户列表（后台管理用）
     * <p>
     * 请求方式：GET /api/user/page
     * 入参：
     *   - keyword：关键字，可按用户名/昵称等模糊搜索（可选）
     *   - role：角色筛选，如 USER/VOLUNTEER/STAFF/ADMIN（可选）
     *   - pageNum：页码，默认 1
     *   - pageSize：每页条数，默认 10
     * 返回：Result&lt;PageResult&lt;User&gt;&gt;，data 里包含 total（总数）和 list（用户列表）
     * 权限：ADMIN（类级 @RequireRole 生效）
     */
    @GetMapping("/page")
    public Result<PageResult<User>> page(@RequestParam(required = false) String keyword,
                                         @RequestParam(required = false) String role,
                                         @RequestParam(defaultValue = "1") int pageNum,
                                         @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(userService.page(keyword, role, pageNum, pageSize));
    }

    /**
     * 启用 / 禁用用户账号
     * <p>
     * 请求方式：PUT /api/user/{id}/status
     * 入参：
     *   - id：路径参数，要操作的用户 ID
     *   - status：请求参数，目标状态（如 0 禁用、1 正常）
     * 返回：Result&lt;Void&gt;，仅表示操作成功与否
     * 权限：ADMIN
     */
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        userService.updateStatus(id, status);
        return Result.success();
    }

    /**
     * 修改用户角色（如把普通用户升级为志愿者/员工/管理员）
     * <p>
     * 请求方式：PUT /api/user/{id}/role
     * 入参：
     *   - id：路径参数，用户 ID
     *   - role：请求参数，目标角色名（USER/VOLUNTEER/STAFF/ADMIN）
     * 返回：Result&lt;Void&gt;
     * 权限：ADMIN
     */
    @PutMapping("/{id}/role")
    public Result<Void> updateRole(@PathVariable Long id, @RequestParam String role) {
        userService.updateRole(id, role);
        return Result.success();
    }

    /**
     * 删除用户
     * <p>
     * 请求方式：DELETE /api/user/{id}
     * 入参：id（路径参数，用户 ID）
     * 返回：Result&lt;Void&gt;
     * 权限：ADMIN
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return Result.success();
    }
}
