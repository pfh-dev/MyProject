package com.petadopt.entity;

import lombok.Data;

/**
 * 轮播图表：首页展示的轮播广告位。
 */
@Data
public class Banner {
    private Long id;
    private String title;   // 标题
    private String img;     // 图片地址
    private String link;    // 点击跳转链接
    private Integer sort;   // 排序值（越小越靠前）
    private Integer status; // 启用状态（1 启用 / 0 停用）
}
