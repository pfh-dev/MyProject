package com.petadopt.controller;

import com.petadopt.common.PageResult;
import com.petadopt.common.Result;
import com.petadopt.entity.FollowUp;
import com.petadopt.security.RequireRole;
import com.petadopt.service.FollowUpService;
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
 * 领养回访接口
 * <p>
 * 负责「领养后回访」记录的管理：宠物被领养后，工作人员/志愿者定期回访
 * 了解宠物生活情况，并登记回访记录。本类提供新增回访、按领养申请查询
 * 回访、分页查询、删除等能力。
 */
@RestController
@RequestMapping("/api/follow-up")
public class FollowUpController {

    @Autowired
    private FollowUpService followUpService;

    /**
     * 新增一条回访记录
     * <p>
     * 请求方式：POST /api/follow-up
     * 入参：followUp（请求体，FollowUp 对象，含领养申请 ID、回访内容、时间等）
     * 返回：Result&lt;Void&gt;
     * 权限：ADMIN、STAFF、VOLUNTEER
     */
    @RequireRole({"ADMIN", "STAFF", "VOLUNTEER"})
    @PostMapping
    public Result<Void> add(@RequestBody FollowUp followUp) {
        followUpService.add(followUp);
        return Result.success();
    }

    /**
     * 按领养申请查询其全部回访记录
     * <p>
     * 请求方式：GET /api/follow-up/adopt/{adoptId}
     * 入参：adoptId（路径参数，领养申请 ID）
     * 返回：Result&lt;List&lt;FollowUp&gt;&gt;，data 为该申请对应的回访记录列表
     * 权限：无
     */
    @GetMapping("/adopt/{adoptId}")
    public Result<List<FollowUp>> listByAdopt(@PathVariable Long adoptId) {
        return Result.success(followUpService.listByAdopt(adoptId));
    }

    /**
     * 分页查询回访记录（支持状态、关键字筛选）
     * <p>
     * 请求方式：GET /api/follow-up/page
     * 入参：
     *   - status：回访状态筛选（待回访/已回访，可选）
     *   - keyword：关键字（可选，匹配宠物名/志愿者/领养人）
     *   - pageNum / pageSize：分页参数，默认 1 / 10
     * 返回：Result&lt;PageResult&lt;FollowUp&gt;&gt;，每条含领养人、领养天数等
     * 权限：ADMIN、STAFF、VOLUNTEER
     */
    @RequireRole({"ADMIN", "STAFF", "VOLUNTEER"})
    @GetMapping("/page")
    public Result<PageResult<FollowUp>> page(@RequestParam(required = false) String status,
                                             @RequestParam(required = false) String keyword,
                                             @RequestParam(defaultValue = "1") int pageNum,
                                             @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(followUpService.page(status, keyword, pageNum, pageSize));
    }

    /**
     * 登记回访：把一条「待回访计划」补充成「已回访记录」。
     * <p>
     * 请求方式：PUT /api/follow-up/{id}
     * 入参：id（路径参数）+ followUp（请求体，含回访内容、宠物状况、是否正常、回访时间等）
     * 返回：Result&lt;Void&gt;
     * 权限：ADMIN、STAFF、VOLUNTEER
     */
    @RequireRole({"ADMIN", "STAFF", "VOLUNTEER"})
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody FollowUp followUp) {
        followUp.setId(id);
        followUpService.update(followUp);
        return Result.success();
    }

    /**
     * 删除回访记录
     * <p>
     * 请求方式：DELETE /api/follow-up/{id}
     * 入参：id（路径参数，回访记录 ID）
     * 返回：Result&lt;Void&gt;
     * 权限：ADMIN
     */
    @RequireRole({"ADMIN"})
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        followUpService.delete(id);
        return Result.success();
    }
}
