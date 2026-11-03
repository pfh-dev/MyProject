package com.petadopt.entity;

import lombok.Data;

/**
 * 宠物档案表：每只待领养宠物的完整信息。
 */
@Data
public class Pet {
    private Long id;
    private String name;        // 宠物名字
    private Long categoryId;    // 宠物分类 ID（外键）
    private String breed;       // 品种
    private String gender;      // 性别
    private String age;         // 年龄
    private String vaccine;     // 疫苗情况
    private String sterilization; // 是否绝育
    private String description; // 简介描述
    private String detail;      // 详细说明
    private String coverImg;    // 封面图
    private String images;      // 图片列表（多张图，通常逗号/JSON 分隔）
    private String status;      // 领养状态（待领养/已领养/下架等）
    private String source;      // 来源（如救助转入、用户发布）
    private String reviewStatus; // 审核状态（待审核/通过/驳回）
    private Long createUserId;  // 发布人 ID（外键）
    private String createTime;  // 创建时间
    private String updateTime;  // 更新时间

    // 联表查询字段
    private String categoryName; // 分类名称（来自 pet_category 表）
}
