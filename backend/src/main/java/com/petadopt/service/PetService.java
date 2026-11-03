package com.petadopt.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.petadopt.common.BusinessException;
import com.petadopt.common.PageResult;
import com.petadopt.entity.Pet;
import com.petadopt.mapper.PetMapper;
import com.petadopt.security.UserContext;
import com.petadopt.util.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 宠物档案服务
 * <p>
 * 管理宠物从「上架 → 待领养 → 领养中 → 已领养」的全生命周期，
 * 同时区分前台展示（只展示已通过审核、可领养的宠物）和后台管理（全量查询）两条数据链路。
 */
@Service
public class PetService {

    @Autowired
    private PetMapper petMapper;

    /**
     * 前台公开的宠物分页列表
     * <p>
     * 只查询审核通过、可供用户浏览领养的宠物，供首页/领养大厅展示。
     *
     * @param name       宠物名字关键字
     * @param categoryId 分类 id
     * @param pageNum    页码
     * @param pageSize   每页条数
     * @return 分页结果
     */
    public PageResult<Pet> publicPage(String name, Long categoryId, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Pet> list = petMapper.selectPublicList(name, categoryId);
        PageInfo<Pet> info = new PageInfo<>(list);
        return new PageResult<>(info.getTotal(), info.getList());
    }

    /**
     * 查询宠物详情
     *
     * @param id 宠物 id
     * @return 宠物实体
     * @throws BusinessException 宠物不存在时抛出
     */
    public Pet detail(Long id) {
        Pet pet = petMapper.selectById(id);
        // 查不到说明 id 非法或已被删除，直接抛业务异常给前端提示
        if (pet == null) {
            throw new BusinessException("宠物不存在");
        }
        return pet;
    }

    /**
     * 后台宠物分页列表（支持名字、分类、状态、审核状态多条件筛选）
     *
     * @param name         宠物名字关键字
     * @param categoryId   分类 id
     * @param status       领养状态（待领养/领养中/已领养等）
     * @param reviewStatus 审核状态（待审核/已通过/已拒绝）
     * @param pageNum      页码
     * @param pageSize     每页条数
     * @return 分页结果
     */
    public PageResult<Pet> page(String name, Long categoryId, String status, String reviewStatus, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Pet> list = petMapper.selectList(name, categoryId, status, reviewStatus);
        PageInfo<Pet> info = new PageInfo<>(list);
        return new PageResult<>(info.getTotal(), info.getList());
    }

    /**
     * 后台新增宠物
     * <p>
     * 后台直接发布的宠物默认视为「已审核通过」，来源标记为 ADMIN，并自动记录创建人。
     *
     * @param pet 宠物实体
     */
    public void add(Pet pet) {
        pet.setSource("ADMIN"); // 来源：后台管理员录入
        // 未指定状态时默认为「待领养」
        pet.setStatus(pet.getStatus() == null ? "待领养" : pet.getStatus());
        pet.setReviewStatus("已通过"); // 后台录入无需再审核
        pet.setCreateUserId(UserContext.userId()); // 记录当前登录管理员的 id
        pet.setCreateTime(DateUtil.now());
        petMapper.insert(pet);
    }

    /**
     * 修改宠物信息
     *
     * @param pet 宠物实体
     */
    public void update(Pet pet) {
        petMapper.update(pet);
    }

    /**
     * 修改宠物领养状态（待领养/领养中/已领养）
     *
     * @param id     宠物 id
     * @param status 新的领养状态
     */
    public void updateStatus(Long id, String status) {
        petMapper.updateStatus(id, status);
    }

    /**
     * 宠物信息审核（前台用户上报的宠物需要后台审核）
     *
     * @param id           宠物 id
     * @param reviewStatus 审核结果（已通过/已拒绝）
     */
    public void review(Long id, String reviewStatus) {
        petMapper.updateReviewStatus(id, reviewStatus);
    }

    /**
     * 删除宠物档案
     *
     * @param id 宠物 id
     */
    public void delete(Long id) {
        petMapper.deleteById(id);
    }
}
