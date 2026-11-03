package com.petadopt.controller;

import com.petadopt.common.Result;
import com.petadopt.entity.Banner;
import com.petadopt.security.RequireRole;
import com.petadopt.service.BannerService;
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
 * 轮播图（Banner）管理接口
 * <p>
 * 负责首页「轮播图」的管理。前台用户查看上架中的轮播图，
 * 管理员/员工可查看全部（含下架）并新增、修改、删除轮播图。
 */
@RestController
@RequestMapping("/api/banner")
public class BannerController {

    @Autowired
    private BannerService bannerService;

    /**
     * 前台查询轮播图列表（仅上架中的）
     * <p>
     * 请求方式：GET /api/banner/all
     * 入参：无
     * 返回：Result&lt;List&lt;Banner&gt;&gt;
     * 权限：无
     */
    @GetMapping("/all")
    public Result<List<Banner>> all() {
        return Result.success(bannerService.publicList());
    }

    /**
     * 后台查询全部轮播图列表（含下架）
     * <p>
     * 请求方式：GET /api/banner/list
     * 入参：无
     * 返回：Result&lt;List&lt;Banner&gt;&gt;
     * 权限：ADMIN、STAFF
     */
    @RequireRole({"ADMIN", "STAFF"})
    @GetMapping("/list")
    public Result<List<Banner>> list() {
        return Result.success(bannerService.adminList());
    }

    /**
     * 新增轮播图
     * <p>
     * 请求方式：POST /api/banner
     * 入参：banner（请求体，Banner 对象，含图片地址、跳转链接、排序等）
     * 返回：Result&lt;Void&gt;
     * 权限：ADMIN、STAFF
     */
    @RequireRole({"ADMIN", "STAFF"})
    @PostMapping
    public Result<Void> add(@RequestBody Banner banner) {
        bannerService.add(banner);
        return Result.success();
    }

    /**
     * 修改轮播图
     * <p>
     * 请求方式：PUT /api/banner
     * 入参：banner（请求体，需携带轮播图 ID 及修改后的信息）
     * 返回：Result&lt;Void&gt;
     * 权限：ADMIN、STAFF
     */
    @RequireRole({"ADMIN", "STAFF"})
    @PutMapping
    public Result<Void> update(@RequestBody Banner banner) {
        bannerService.update(banner);
        return Result.success();
    }

    /**
     * 删除轮播图
     * <p>
     * 请求方式：DELETE /api/banner/{id}
     * 入参：id（路径参数，轮播图 ID）
     * 返回：Result&lt;Void&gt;
     * 权限：ADMIN
     */
    @RequireRole({"ADMIN"})
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        bannerService.delete(id);
        return Result.success();
    }
}
