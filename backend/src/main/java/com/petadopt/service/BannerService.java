package com.petadopt.service;

import com.petadopt.entity.Banner;
import com.petadopt.mapper.BannerMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 轮播图服务
 * <p>
 * 负责首页轮播图（Banner）的维护与展示。
 * 区分两条查询链路：前台只展示启用的轮播图，后台展示全部（含未启用）。
 */
@Service
public class BannerService {

    @Autowired
    private BannerMapper bannerMapper;

    /**
     * 前台展示：只查询启用的轮播图
     *
     * @return 已启用的轮播图列表
     */
    public List<Banner> publicList() {
        return bannerMapper.selectAllWithStatus();
    }

    /**
     * 后台管理：查询全部轮播图（含未启用）
     *
     * @return 全部轮播图列表
     */
    public List<Banner> adminList() {
        return bannerMapper.selectAll();
    }

    /**
     * 新增轮播图
     *
     * @param banner 轮播图实体
     */
    public void add(Banner banner) {
        bannerMapper.insert(banner);
    }

    /**
     * 修改轮播图
     *
     * @param banner 轮播图实体
     */
    public void update(Banner banner) {
        bannerMapper.update(banner);
    }

    /**
     * 删除轮播图
     *
     * @param id 轮播图 id
     */
    public void delete(Long id) {
        bannerMapper.deleteById(id);
    }
}
