package com.petadopt.mapper;

import com.petadopt.entity.User;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户表的数据操作接口（操作 user 表）。
 */
public interface UserMapper {

    /** 根据用户名查询用户（登录时用） */
    User selectByUsername(String username);

    /** 根据 ID 查询用户 */
    User selectById(Long id);

    /** 新增用户 */
    int insert(User user);

    /** 更新用户信息 */
    int update(User user);

    /** 根据 ID 删除用户 */
    int deleteById(Long id);

    /** 按关键词和角色查询用户列表 */
    List<User> selectList(@Param("keyword") String keyword, @Param("role") String role);

    /** 更新用户账号状态（启用/禁用） */
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    /** 更新用户角色 */
    int updateRole(@Param("id") Long id, @Param("role") String role);
}
