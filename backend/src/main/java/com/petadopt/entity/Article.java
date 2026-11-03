package com.petadopt.entity;

import lombok.Data;

/**
 * 文章表：领养科普、资讯等图文内容。
 */
@Data
public class Article {
    private Long id;
    private String title;       // 标题
    private String cover;       // 封面图
    private String content;     // 正文内容
    private String type;        // 文章类型（如科普/资讯）
    private String author;      // 作者
    private Integer viewCount;  // 浏览量
    private String createTime;  // 发布时间
}
