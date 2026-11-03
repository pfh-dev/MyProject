package com.petadopt.service;

import com.petadopt.mapper.StatsMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 数据统计服务
 * <p>
 * 为后台管理看板提供统计数据：汇总各项核心指标（宠物、领养、救助、用户、捐赠、志愿者数量）、
 * 近 7 天趋势、以及分类/状态分布等，供前端图表展示。
 */
@Service
public class StatsService {

    @Autowired
    private StatsMapper statsMapper;

    /**
     * 核心指标总览
     * <p>
     * 一次性返回各业务模块的累计数量，作为看板顶部的数字卡片。
     *
     * @return key-value 形式的统计指标集合
     */
    public Map<String, Object> overview() {
        Map<String, Object> map = new HashMap<>();
        map.put("petCount", statsMapper.countPets());           // 宠物总数
        map.put("adoptCount", statsMapper.countAdoptions());    // 领养总数
        map.put("rescueCount", statsMapper.countRescues());     // 救助线索总数
        map.put("userCount", statsMapper.countUsers());         // 用户总数
        map.put("donationCount", statsMapper.countDonations()); // 捐赠总数
        map.put("volunteerCount", statsMapper.countVolunteers());// 志愿者总数
        return map;
    }

    /**
     * 近 7 天趋势（领养与救助）
     * <p>
     * 用于折线图展示近期领养、救助数量的变化趋势。
     *
     * @return 包含领养趋势、救助趋势两组数据的集合
     */
    public Map<String, Object> trend() {
        Map<String, Object> map = new HashMap<>();
        map.put("adoptTrend", statsMapper.adoptTrend(7));   // 近 7 天领养趋势
        map.put("rescueTrend", statsMapper.rescueTrend(7)); // 近 7 天救助趋势
        return map;
    }

    /**
     * 分布统计（宠物分类分布、领养状态分布）
     * <p>
     * 用于饼图/柱状图展示各类别、各状态的数量占比。
     *
     * @return 包含分类分布、领养状态分布的集合
     */
    public Map<String, Object> distribution() {
        Map<String, Object> map = new HashMap<>();
        map.put("petCategory", statsMapper.petCategoryDist()); // 宠物按分类的数量分布
        map.put("adoptStatus", statsMapper.adoptStatusDist()); // 领养按状态的数量分布
        return map;
    }
}
