package com.petadopt.entity;

import lombok.Data;

/**
 * 宠物分类表：如猫、狗、兔子等大类。
 */
@Data
public class PetCategory {
    private Long id;
    private String name;       // 分类名称
    private String icon;       // 分类图标
    private Integer sort;      // 排序值（越小越靠前）
    private String createTime; // 创建时间
}
