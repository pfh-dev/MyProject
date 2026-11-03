package com.petadopt.mapper;

import com.petadopt.entity.Banner;

import java.util.List;

/**
 * 轮播图表的数据操作接口（操作 banner 表）。
 */
public interface BannerMapper {

    /** 查询全部轮播图（后台管理用，含停用的） */
    List<Banner> selectAll();

    /** 查询启用的轮播图（前台首页用） */
    List<Banner> selectAllWithStatus();

    /** 新增轮播图 */
    int insert(Banner banner);

    /** 更新轮播图 */
    int update(Banner banner);

    /** 根据 ID 删除轮播图 */
    int deleteById(Long id);
}
