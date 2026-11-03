package com.petadopt.controller;

import com.petadopt.common.Result;
import com.petadopt.security.RequireRole;
import com.petadopt.service.StatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 数据统计接口
 * <p>
 * 负责后台「数据看板」所需的统计信息：整体概览（各类数量）、趋势、
 * 分布等，供管理员/员工查看平台运营情况。本类所有接口都要求 ADMIN 或 STAFF 角色。
 */
@RestController
@RequestMapping("/api/stats")
@RequireRole({"ADMIN", "STAFF"})
public class StatsController {

    @Autowired
    private StatsService statsService;

    /**
     * 统计概览（如宠物总数、领养数、用户数、捐赠总额等）
     * <p>
     * 请求方式：GET /api/stats/overview
     * 入参：无
     * 返回：Result&lt;Map&lt;String,Object&gt;&gt;，data 为各统计指标的键值对
     * 权限：ADMIN、STAFF（类级 @RequireRole 生效）
     */
    @GetMapping("/overview")
    public Result<Map<String, Object>> overview() {
        return Result.success(statsService.overview());
    }

    /**
     * 趋势统计（如近一段时间领养/救助数量的变化趋势）
     * <p>
     * 请求方式：GET /api/stats/trend
     * 入参：无
     * 返回：Result&lt;Map&lt;String,Object&gt;&gt;，data 为趋势数据
     * 权限：ADMIN、STAFF
     */
    @GetMapping("/trend")
    public Result<Map<String, Object>> trend() {
        return Result.success(statsService.trend());
    }

    /**
     * 分布统计（如宠物按分类分布、用户按角色分布等）
     * <p>
     * 请求方式：GET /api/stats/distribution
     * 入参：无
     * 返回：Result&lt;Map&lt;String,Object&gt;&gt;，data 为分布数据
     * 权限：ADMIN、STAFF
     */
    @GetMapping("/distribution")
    public Result<Map<String, Object>> distribution() {
        return Result.success(statsService.distribution());
    }
}
