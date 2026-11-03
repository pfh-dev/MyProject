package com.petadopt.mapper;

import com.petadopt.entity.Article;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 文章表的数据库操作接口（操作 article 表）。
 */
public interface ArticleMapper {

    /** 根据 ID 查询单篇文章 */
    Article selectById(Long id);

    /** 新增一篇文章 */
    int insert(Article article);

    /** 更新文章内容 */
    int update(Article article);

    /** 根据 ID 删除文章 */
    int deleteById(Long id);

    /** 按类型和关键词模糊查询文章列表 */
    List<Article> selectList(@Param("type") String type, @Param("keyword") String keyword);

    /** 浏览量自增 1 */
    int incrementView(Long id);
}
