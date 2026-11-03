package com.petadopt.controller;

import com.petadopt.common.PageResult;
import com.petadopt.common.Result;
import com.petadopt.entity.Article;
import com.petadopt.security.RequireRole;
import com.petadopt.service.ArticleService;
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
 * 文章/资讯管理接口
 * <p>
 * 负责平台「文章、资讯、科普」等内容的管理：前台查询列表与详情，
 * 后台新增、修改、删除文章。文章按 type（类型）区分，如公告、养宠知识等。
 */
@RestController
@RequestMapping("/api/article")
public class ArticleController {

    @Autowired
    private ArticleService articleService;

    /**
     * 前台分页查询文章列表
     * <p>
     * 请求方式：GET /api/article/list
     * 入参：
     *   - type：文章类型筛选（可选）
     *   - keyword：关键字搜索（可选）
     *   - pageNum / pageSize：分页参数，默认 1 / 10
     * 返回：Result&lt;PageResult&lt;Article&gt;&gt;
     * 权限：无
     */
    @GetMapping("/list")
    public Result<PageResult<Article>> list(@RequestParam(required = false) String type,
                                            @RequestParam(required = false) String keyword,
                                            @RequestParam(defaultValue = "1") int pageNum,
                                            @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(articleService.list(type, keyword, pageNum, pageSize));
    }

    /**
     * 文章详情
     * <p>
     * 请求方式：GET /api/article/detail/{id}
     * 入参：id（路径参数，文章 ID）
     * 返回：Result&lt;Article&gt;
     * 权限：无
     */
    @GetMapping("/detail/{id}")
    public Result<Article> detail(@PathVariable Long id) {
        return Result.success(articleService.detail(id));
    }

    /**
     * 新增文章
     * <p>
     * 请求方式：POST /api/article
     * 入参：article（请求体，Article 对象，含标题、内容、类型、封面等）
     * 返回：Result&lt;Void&gt;
     * 权限：ADMIN、STAFF
     */
    @RequireRole({"ADMIN", "STAFF"})
    @PostMapping
    public Result<Void> add(@RequestBody Article article) {
        articleService.add(article);
        return Result.success();
    }

    /**
     * 修改文章
     * <p>
     * 请求方式：PUT /api/article
     * 入参：article（请求体，需携带文章 ID 及修改后的内容）
     * 返回：Result&lt;Void&gt;
     * 权限：ADMIN、STAFF
     */
    @RequireRole({"ADMIN", "STAFF"})
    @PutMapping
    public Result<Void> update(@RequestBody Article article) {
        articleService.update(article);
        return Result.success();
    }

    /**
     * 删除文章
     * <p>
     * 请求方式：DELETE /api/article/{id}
     * 入参：id（路径参数，文章 ID）
     * 返回：Result&lt;Void&gt;
     * 权限：ADMIN
     */
    @RequireRole({"ADMIN"})
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        articleService.delete(id);
        return Result.success();
    }
}
