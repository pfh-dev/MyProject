package com.petadopt.mapper;

import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 数据统计接口：用于后台首页的各类统计图表数据查询。
 */
public interface StatsMapper {

    /** 统计宠物总数 */
    long countPets();

    /** 统计领养申请总数 */
    long countAdoptions();

    /** 统计救助信息总数 */
    long countRescues();

    /** 统计用户总数 */
    long countUsers();

    /** 统计捐赠记录总数 */
    long countDonations();

    /** 统计志愿者总数 */
    long countVolunteers();

    /** 统计最近 N 天内的领养申请趋势（用于折线图） */
    List<Map<String, Object>> adoptTrend(@Param("days") int days);

    /** 统计最近 N 天内的救助趋势（用于折线图） */
    List<Map<String, Object>> rescueTrend(@Param("days") int days);

    /** 统计各宠物分类的数量分布（用于饼图） */
    List<Map<String, Object>> petCategoryDist();

    /** 统计领养申请各状态的数量分布（用于饼图） */
    List<Map<String, Object>> adoptStatusDist();
}
