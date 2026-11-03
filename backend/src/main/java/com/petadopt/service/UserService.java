package com.petadopt.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.petadopt.common.PageResult;
import com.petadopt.entity.User;
import com.petadopt.mapper.UserMapper;
import com.petadopt.security.AesUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 用户管理服务
 * <p>
 * 供后台管理员使用，负责用户的列表查询、启用/禁用、角色调整、删除等管理操作。
 * 注意：这里涉及手机号等敏感字段，会配合 AesUtil 做加解密处理。
 */
@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private AesUtil aesUtil;

    /**
     * 分页查询用户列表（支持按关键字、角色筛选）
     * <p>
     * 流程：
     * 1. 用 PageHelper 开启分页（它会拦截下一条查询 SQL 自动加 limit）；
     * 2. 查出用户列表后，把密码置空（绝不能把密码返回给前端），并解密手机号；
     * 3. 用 PageInfo 拿到总条数，封装成统一的 PageResult 返回。
     *
     * @param keyword  搜索关键字（用户名/昵称等）
     * @param role     角色筛选
     * @param pageNum  页码
     * @param pageSize 每页条数
     * @return 分页结果
     */
    public PageResult<User> page(String keyword, String role, int pageNum, int pageSize) {
        // 开启分页，PageHelper 内部通过 ThreadLocal 给下一条查询 SQL 追加分页参数
        PageHelper.startPage(pageNum, pageSize);
        List<User> list = userMapper.selectList(keyword, role);
        // 逐个处理：清空密码 + 解密手机号，保证敏感数据不出后端
        for (User user : list) {
            user.setPassword(null);
            user.setPhone(aesUtil.decrypt(user.getPhone()));
        }
        PageInfo<User> info = new PageInfo<>(list);
        return new PageResult<>(info.getTotal(), info.getList());
    }

    /**
     * 修改用户状态（启用/禁用）
     *
     * @param id     用户 id
     * @param status 状态值（0 正常 / 1 禁用）
     */
    public void updateStatus(Long id, Integer status) {
        userMapper.updateStatus(id, status);
    }

    /**
     * 修改用户角色（如普通用户提升为志愿者/管理员）
     *
     * @param id   用户 id
     * @param role 角色标识
     */
    public void updateRole(Long id, String role) {
        userMapper.updateRole(id, role);
    }

    /**
     * 删除用户
     *
     * @param id 用户 id
     */
    public void delete(Long id) {
        userMapper.deleteById(id);
    }
}
