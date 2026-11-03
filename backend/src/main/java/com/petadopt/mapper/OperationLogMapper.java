package com.petadopt.mapper;

import com.petadopt.entity.OperationLog;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 操作日志表的数据操作接口（操作 operation_log 表）。
 */
public interface OperationLogMapper {

    /** 新增一条操作日志 */
    int insert(OperationLog log);

    /** 按关键词模糊查询操作日志列表 */
    List<OperationLog> selectList(@Param("keyword") String keyword);
}
