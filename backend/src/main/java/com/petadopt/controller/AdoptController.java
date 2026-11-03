package com.petadopt.controller;

import com.petadopt.common.PageResult;
import com.petadopt.common.Result;
import com.petadopt.entity.AdoptApplication;
import com.petadopt.security.RequireRole;
import com.petadopt.service.AdoptService;
import org.springframework.beans.factory.annotation.Autowired;
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
 * 领养申请接口
 * <p>
 * 负责「领养申请」流程：用户提交领养申请、查看自己的申请记录，
 * 以及管理员/员工分页查询所有申请并审核（通过 / 驳回）。
 */
@RestController
@RequestMapping("/api/adopt")
public class AdoptController {

    @Autowired
    private AdoptService adoptService;

    /**
     * 提交领养申请
     * <p>
     * 请求方式：POST /api/adopt
     * 入参：application（请求体，AdoptApplication 对象，含宠物 ID、申请人信息、申请理由等）
     * 返回：Result&lt;Void&gt;
     * 权限：无（登录用户即可申请）
     */
    @PostMapping
    public Result<Void> apply(@RequestBody AdoptApplication application) {
        adoptService.apply(application);
        return Result.success();
    }

    /**
     * 查看「我」提交的领养申请列表
     * <p>
     * 请求方式：GET /api/adopt/my
     * 入参：无（后端根据当前登录用户自动获取）
     * 返回：Result&lt;List&lt;AdoptApplication&gt;&gt;，data 为我的申请列表
     * 权限：无（登录用户查看自己的记录）
     */
    @GetMapping("/my")
    public Result<List<AdoptApplication>> my() {
        return Result.success(adoptService.myList());
    }

    /**
     * 后台分页查询所有领养申请
     * <p>
     * 请求方式：GET /api/adopt/page
     * 入参：
     *   - status：申请状态筛选（可选）
     *   - keyword：关键字（可选）
     *   - pageNum / pageSize：分页参数，默认 1 / 10
     * 返回：Result&lt;PageResult&lt;AdoptApplication&gt;&gt;
     * 权限：ADMIN、STAFF
     */
    @RequireRole({"ADMIN", "STAFF"})
    @GetMapping("/page")
    public Result<PageResult<AdoptApplication>> page(@RequestParam(required = false) String status,
                                                     @RequestParam(required = false) String keyword,
                                                     @RequestParam(defaultValue = "1") int pageNum,
                                                     @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(adoptService.page(status, keyword, pageNum, pageSize));
    }

    /**
     * 审核领养申请（通过 / 驳回）
     * <p>
     * 请求方式：PUT /api/adopt/{id}/audit
     * 入参：
     *   - id：路径参数，申请 ID
     *   - status：请求参数，审核结果状态
     *   - remark：请求参数，审核备注/驳回原因（可选）
     * 返回：Result&lt;Void&gt;
     * 权限：ADMIN、STAFF
     */
    @RequireRole({"ADMIN", "STAFF"})
    @PutMapping("/{id}/audit")
    public Result<Void> audit(@PathVariable Long id,
                              @RequestParam String status,
                              @RequestParam(required = false) String remark) {
        adoptService.audit(id, status, remark);
        return Result.success();
    }
}
