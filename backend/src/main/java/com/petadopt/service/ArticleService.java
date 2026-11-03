package com.petadopt.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.petadopt.common.BusinessException;
import com.petadopt.common.PageResult;
import com.petadopt.entity.Article;
import com.petadopt.mapper.ArticleMapper;
import com.petadopt.util.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 文章服务
 * <p>
 * 负责资讯/科普文章的查询、发布与维护，并附带阅读量统计功能。
 * type 用于区分文章类型（如资讯、科普、公告等）。
 */
@Service
public class ArticleService {

    @Autowired
    private ArticleMapper articleMapper;

    /**
     * 分页查询文章列表（支持类型、关键字筛选）
     *
     * @param type     文章类型
     * @param keyword  搜索关键字
     * @param pageNum  页码
     * @param pageSize 每页条数
     * @return 分页结果
     */
    public PageResult<Article> list(String type, String keyword, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Article> list = articleMapper.selectList(type, keyword);
        PageInfo<Article> info = new PageInfo<>(list);
        return new PageResult<>(info.getTotal(), info.getList());
    }

    /**
     * 查看文章详情（同时自增阅读量）
     * <p>
     * 每次查看详情都会先把阅读量 +1，再返回最新内容，实现简单的浏览量统计。
     *
     * @param id 文章 id
     * @return 文章实体
     * @throws BusinessException 文章不存在时抛出
     */
    public Article detail(Long id) {
        articleMapper.incrementView(id); // 阅读量 +1
        Article article = articleMapper.selectById(id);
        if (article == null) {
            throw new BusinessException("文章不存在");
        }
        return article;
    }

    /**
     * 新增文章（初始化阅读量为 0 并记录创建时间）
     *
     * @param article 文章实体
     */
    public void add(Article article) {
        article.setViewCount(0); // 新文章阅读量从 0 开始
        article.setCreateTime(DateUtil.now());
        articleMapper.insert(article);
    }

    /**
     * 修改文章
     *
     * @param article 文章实体
     */
    public void update(Article article) {
        articleMapper.update(article);
    }

    /**
     * 删除文章
     *
     * @param id 文章 id
     */
    public void delete(Long id) {
        articleMapper.deleteById(id);
    }
}
