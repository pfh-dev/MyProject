package com.petadopt.controller;

import com.petadopt.common.PageResult;
import com.petadopt.common.Result;
import com.petadopt.entity.Volunteer;
import com.petadopt.security.RequireRole;
import com.petadopt.service.VolunteerService;
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
 * 志愿者管理接口
 * <p>
 * 负责「志愿者申请与审核」：用户申请成为志愿者、查看自己的申请记录，
 * 以及管理员/员工分页查询、查看已通过志愿者、审核申请、删除等。
 */
@RestController
@RequestMapping("/api/volunteer")
public class VolunteerController {

    @Autowired
    private VolunteerService volunteerService;

    /**
     * 提交志愿者申请
     * <p>
     * 请求方式：POST /api/volunteer/apply
     * 入参：volunteer（请求体，Volunteer 对象，含个人简介、可服务时间等）
     * 返回：Result&lt;Void&gt;
     * 权限：无（登录用户即可申请）
     */
    @PostMapping("/apply")
    public Result<Void> apply(@RequestBody Volunteer volunteer) {
        volunteerService.apply(volunteer);
        return Result.success();
    }

    /**
     * 查看「我」的志愿者申请信息
     * <p>
     * 请求方式：GET /api/volunteer/my
     * 入参：无（根据当前登录用户自动获取）
     * 返回：Result&lt;Volunteer&gt;，data 为我的申请记录（单条）
     * 权限：无
     */
    @GetMapping("/my")
    public Result<Volunteer> my() {
        return Result.success(volunteerService.myApply());
    }

    /**
     * 后台分页查询志愿者申请
     * <p>
     * 请求方式：GET /api/volunteer/page
     * 入参：
     *   - status：审核状态筛选（可选）
     *   - keyword：关键字（可选）
     *   - pageNum / pageSize：分页参数，默认 1 / 10
     * 返回：Result&lt;PageResult&lt;Volunteer&gt;&gt;
     * 权限：ADMIN、STAFF
     */
    @RequireRole({"ADMIN", "STAFF"})
    @GetMapping("/page")
    public Result<PageResult<Volunteer>> page(@RequestParam(required = false) String status,
                                              @RequestParam(required = false) String keyword,
                                              @RequestParam(defaultValue = "1") int pageNum,
                                              @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(volunteerService.page(status, keyword, pageNum, pageSize));
    }

    /**
     * 查询所有「已通过」的志愿者（用于指派救助任务等场景）
     * <p>
     * 请求方式：GET /api/volunteer/approved
     * 入参：无
     * 返回：Result&lt;List&lt;Volunteer&gt;&gt;，data 为已通过审核的志愿者列表
     * 权限：ADMIN、STAFF
     */
    @RequireRole({"ADMIN", "STAFF"})
    @GetMapping("/approved")
    public Result<List<Volunteer>> approved() {
        return Result.success(volunteerService.approved());
    }

    /**
     * 审核志愿者申请（通过 / 驳回）
     * <p>
     * 请求方式：PUT /api/volunteer/{id}/audit
     * 入参：
     *   - id：路径参数，申请 ID
     *   - status：请求参数，审核结果状态
     * 返回：Result&lt;Void&gt;
     * 权限：ADMIN、STAFF
     */
    @RequireRole({"ADMIN", "STAFF"})
    @PutMapping("/{id}/audit")
    public Result<Void> audit(@PathVariable Long id, @RequestParam String status) {
        volunteerService.audit(id, status);
        return Result.success();
    }

    /**
     * 删除志愿者申请记录
     * <p>
     * 请求方式：DELETE /api/volunteer/{id}
     * 入参：id（路径参数，申请 ID）
     * 返回：Result&lt;Void&gt;
     * 权限：ADMIN
     */
    @RequireRole({"ADMIN"})
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        volunteerService.delete(id);
        return Result.success();
    }
}
