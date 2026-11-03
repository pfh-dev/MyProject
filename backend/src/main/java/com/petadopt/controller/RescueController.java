package com.petadopt.controller;

import com.petadopt.common.PageResult;
import com.petadopt.common.Result;
import com.petadopt.entity.Rescue;
import com.petadopt.security.RequireRole;
import com.petadopt.service.RescueService;
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

import java.util.List;

/**
 * 救助信息接口
 * <p>
 * 负责「流浪动物救助」相关信息的处理：用户上报需要救助的动物，
 * 管理员/员工/志愿者查询并处理救助任务（指派人员、更新处理状态）。
 */
@RestController
@RequestMapping("/api/rescue")
public class RescueController {

    @Autowired
    private RescueService rescueService;

    /**
     * 上报一条救助信息（发现流浪动物需要救助）
     * <p>
     * 请求方式：POST /api/rescue
     * 入参：rescue（请求体，Rescue 对象，含位置、动物描述、联系方式等）
     * 返回：Result&lt;Void&gt;
     * 权限：无（登录用户即可上报）
     */
    @PostMapping
    public Result<Void> report(@RequestBody Rescue rescue) {
        rescueService.report(rescue);
        return Result.success();
    }

    /**
     * 查看「我」上报的救助信息列表
     * <p>
     * 请求方式：GET /api/rescue/my
     * 入参：无（根据当前登录用户自动获取）
     * 返回：Result&lt;List&lt;Rescue&gt;&gt;
     * 权限：无
     */
    @GetMapping("/my")
    public Result<List<Rescue>> my() {
        return Result.success(rescueService.myList());
    }

    /**
     * 后台分页查询救助信息
     * <p>
     * 请求方式：GET /api/rescue/page
     * 入参：
     *   - status：处理状态筛选（可选）
     *   - keyword：关键字（可选）
     *   - pageNum / pageSize：分页参数，默认 1 / 10
     * 返回：Result&lt;PageResult&lt;Rescue&gt;&gt;
     * 权限：ADMIN、STAFF、VOLUNTEER
     */
    @RequireRole({"ADMIN", "STAFF", "VOLUNTEER"})
    @GetMapping("/page")
    public Result<PageResult<Rescue>> page(@RequestParam(required = false) String status,
                                           @RequestParam(required = false) String keyword,
                                           @RequestParam(defaultValue = "1") int pageNum,
                                           @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(rescueService.page(status, keyword, pageNum, pageSize));
    }

    /**
     * 指派救助任务给某个人员（志愿者/员工）
     * <p>
     * 请求方式：PUT /api/rescue/{id}/assign
     * 入参：
     *   - id：路径参数，救助信息 ID
     *   - assigneeId：请求参数，被指派人员的用户 ID
     * 返回：Result&lt;Void&gt;
     * 权限：ADMIN、STAFF
     */
    @RequireRole({"ADMIN", "STAFF"})
    @PutMapping("/{id}/assign")
    public Result<Void> assign(@PathVariable Long id, @RequestParam Long assigneeId) {
        rescueService.assign(id, assigneeId);
        return Result.success();
    }

    /**
     * 处理救助任务（更新处理状态，如「已处理」）
     * <p>
     * 请求方式：PUT /api/rescue/{id}/handle
     * 入参：
     *   - id：路径参数，救助信息 ID
     *   - status：请求参数，处理后的状态
     *   - remark：请求参数，处理备注（可选）
     * 返回：Result&lt;Void&gt;
     * 权限：ADMIN、STAFF、VOLUNTEER
     */
    @RequireRole({"ADMIN", "STAFF", "VOLUNTEER"})
    @PutMapping("/{id}/handle")
    public Result<Void> handle(@PathVariable Long id,
                               @RequestParam String status,
                               @RequestParam(required = false) String remark) {
        rescueService.handle(id, status, remark);
        return Result.success();
    }

    /**
     * 删除救助信息
     * <p>
     * 请求方式：DELETE /api/rescue/{id}
     * 入参：id（路径参数，救助信息 ID）
     * 返回：Result&lt;Void&gt;
     * 权限：ADMIN
     */
    @RequireRole({"ADMIN"})
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        rescueService.delete(id);
        return Result.success();
    }
}
