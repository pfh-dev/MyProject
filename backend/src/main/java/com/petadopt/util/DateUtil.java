package com.petadopt.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * 日期时间工具类：提供常用的时间字符串生成方法。
 *
 * <p>数据库里时间字段通常存字符串（"yyyy-MM-dd HH:mm:ss" 格式），
 * 这个类把"获取当前时间"和"计算若干天后的时间"封装起来，避免各业务代码里
 * 重复写格式化逻辑，统一格式也方便展示和比较。</p>
 */
public class DateUtil {

    // 统一的时间格式模板，全项目都用这个格式
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 获取当前时间字符串。
     *
     * @return 形如 "2026-11-02 10:30:00" 的当前时间
     */
    public static String now() {
        return LocalDateTime.now().format(FORMATTER);
    }

    /**
     * 获取当前时间往后推 N 天的时间字符串。
     *
     * @param days 天数
     * @return N 天后的时间字符串，例如 afterDays(7) 返回一周后的时间
     */
    public static String afterDays(int days) {
        return LocalDateTime.now().plusDays(days).format(FORMATTER);
    }

    /**
     * 计算从某个时间点到当前时间经过了几个整天。
     *
     * <p>用于计算「领养天数」：领养通过后，从审核通过时间（audit_time）
     * 到今天一共过了多少天。若传入时间为空，说明还没有领养通过时间，返回 0。</p>
     *
     * @param dateTime 起始时间字符串（"yyyy-MM-dd HH:mm:ss" 格式）
     * @return 经过的整天数
     */
    public static long daysSince(String dateTime) {
        if (dateTime == null || dateTime.isEmpty()) {
            return 0;
        }
        LocalDateTime from = LocalDateTime.parse(dateTime, FORMATTER);
        return ChronoUnit.DAYS.between(from, LocalDateTime.now());
    }
}
