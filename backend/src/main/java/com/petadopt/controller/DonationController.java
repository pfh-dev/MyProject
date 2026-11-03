package com.petadopt.controller;

import com.petadopt.common.PageResult;
import com.petadopt.common.Result;
import com.petadopt.entity.Donation;
import com.petadopt.security.RequireRole;
import com.petadopt.service.DonationService;
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
 * 捐款/捐赠管理接口
 * <p>
 * 负责「爱心捐款/捐赠」：用户发起捐赠、查看自己的捐赠记录，
 * 以及管理员/员工分页查询所有捐赠并确认收款状态。
 */
@RestController
@RequestMapping("/api/donation")
public class DonationController {

    @Autowired
    private DonationService donationService;

    /**
     * 发起一笔捐赠（捐款/捐物）
     * <p>
     * 请求方式：POST /api/donation
     * 入参：donation（请求体，Donation 对象，含金额/物品、用途说明等）
     * 返回：Result&lt;Void&gt;
     * 权限：无（登录用户即可捐赠）
     */
    @PostMapping
    public Result<Void> donate(@RequestBody Donation donation) {
        donationService.donate(donation);
        return Result.success();
    }

    /**
     * 查看「我」的捐赠记录列表
     * <p>
     * 请求方式：GET /api/donation/my
     * 入参：无（根据当前登录用户自动获取）
     * 返回：Result&lt;List&lt;Donation&gt;&gt;
     * 权限：无
     */
    @GetMapping("/my")
    public Result<List<Donation>> my() {
        return Result.success(donationService.myList());
    }

    /**
     * 后台分页查询所有捐赠记录
     * <p>
     * 请求方式：GET /api/donation/page
     * 入参：
     *   - status：捐赠状态筛选（可选）
     *   - keyword：关键字（可选）
     *   - pageNum / pageSize：分页参数，默认 1 / 10
     * 返回：Result&lt;PageResult&lt;Donation&gt;&gt;
     * 权限：ADMIN、STAFF
     */
    @RequireRole({"ADMIN", "STAFF"})
    @GetMapping("/page")
    public Result<PageResult<Donation>> page(@RequestParam(required = false) String status,
                                             @RequestParam(required = false) String keyword,
                                             @RequestParam(defaultValue = "1") int pageNum,
                                             @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(donationService.page(status, keyword, pageNum, pageSize));
    }

    /**
     * 确认捐赠（如确认到账 / 完成捐赠）
     * <p>
     * 请求方式：PUT /api/donation/{id}/confirm
     * 入参：
     *   - id：路径参数，捐赠记录 ID
     *   - status：请求参数，确认后的状态
     * 返回：Result&lt;Void&gt;
     * 权限：ADMIN、STAFF
     */
    @RequireRole({"ADMIN", "STAFF"})
    @PutMapping("/{id}/confirm")
    public Result<Void> confirm(@PathVariable Long id, @RequestParam String status) {
        donationService.confirm(id, status);
        return Result.success();
    }
}
