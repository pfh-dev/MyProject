package com.petadopt.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.petadopt.common.BusinessException;
import com.petadopt.common.PageResult;
import com.petadopt.entity.Volunteer;
import com.petadopt.mapper.UserMapper;
import com.petadopt.mapper.VolunteerMapper;
import com.petadopt.security.AesUtil;
import com.petadopt.security.UserContext;
import com.petadopt.util.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 志愿者服务
 * <p>
 * 负责志愿者的申请、查询与审核闭环：
 * 用户在线提交申请 → 管理员审核 → 通过后自动把用户角色提升为 VOLUNTEER，并推送通知。
 * 志愿者后续可参与救助线索的处理、领养回访等工作。
 */
@Service
public class VolunteerService {

    @Autowired
    private VolunteerMapper volunteerMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private AesUtil aesUtil;

    /**
     * 用户提交志愿者申请
     * <p>
     * 流程：
     * 1. 取当前登录用户 id；
     * 2. 检查是否已有「未结束」的申请（已拒绝/已离职的可以重新申请，其他情况视为重复申请）；
     * 3. 加密手机号、置为「申请中」状态并记录申请时间后入库。
     *
     * @param volunteer 志愿者申请实体
     * @throws BusinessException 已存在有效申请时抛出
     */
    public void apply(Volunteer volunteer) {
        Long userId = UserContext.userId();
        // 查该用户是否已有申请记录
        Volunteer existing = volunteerMapper.selectByUserId(userId);
        // 已有申请且不是「已拒绝/已离职」这类可重新申请的状态，则拦截重复提交
        if (existing != null && !"已拒绝".equals(existing.getStatus()) && !"已离职".equals(existing.getStatus())) {
            throw new BusinessException("您已提交过志愿者申请");
        }
        volunteer.setUserId(userId);
        volunteer.setPhone(aesUtil.encrypt(volunteer.getPhone())); // 手机号加密入库
        volunteer.setStatus("申请中");
        volunteer.setApplyTime(DateUtil.now());
        volunteerMapper.insert(volunteer);
    }

    /**
     * 查询当前用户的志愿者申请记录（个人中心展示）
     *
     * @return 志愿者申请实体，可能为 null
     */
    public Volunteer myApply() {
        return volunteerMapper.selectByUserId(UserContext.userId());
    }

    /**
     * 后台分页查询志愿者申请（支持状态、关键字筛选）
     * <p>
     * 查出列表后逐个解密手机号再返回。
     *
     * @param status   申请状态筛选
     * @param keyword  搜索关键字
     * @param pageNum  页码
     * @param pageSize 每页条数
     * @return 分页结果
     */
    public PageResult<Volunteer> page(String status, String keyword, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Volunteer> list = volunteerMapper.selectList(status, keyword);
        for (Volunteer volunteer : list) {
            volunteer.setPhone(aesUtil.decrypt(volunteer.getPhone())); // 解密手机号
        }
        PageInfo<Volunteer> info = new PageInfo<>(list);
        return new PageResult<>(info.getTotal(), info.getList());
    }

    /**
     * 查询所有「已通过」的志愿者（供任务指派下拉框使用）
     *
     * @return 已通过审核的志愿者列表
     */
    public List<Volunteer> approved() {
        return volunteerMapper.selectApproved();
    }

    /**
     * 审核志愿者申请（核心联动方法）
     * <p>
     * 业务规则：
     * - 审核「已通过」→ 把对应用户的角色升级为 VOLUNTEER，并发系统通知；
     * - 审核「已拒绝」→ 只发通知，角色不变；
     * - 审核「已离职」→ 角色回退逻辑交由后续处理（本方法只更新状态）。
     *
     * @param id     申请 id
     * @param status 审核结果（已通过/已拒绝/已离职）
     * @throws BusinessException 申请不存在时抛出
     */
    public void audit(Long id, String status) {
        Volunteer volunteer = volunteerMapper.selectById(id);
        if (volunteer == null) {
            throw new BusinessException("志愿者申请不存在");
        }
        // 先更新申请状态
        volunteerMapper.updateStatus(id, status);
        // 通过后赋予志愿者角色，拒绝/离职则回退为普通用户
        if ("已通过".equals(status)) {
            userMapper.updateRole(volunteer.getUserId(), "VOLUNTEER");
            notificationService.send(volunteer.getUserId(), "志愿者申请通过", "恭喜！您已成为平台志愿者。", "系统", id);
        } else if ("已拒绝".equals(status)) {
            notificationService.send(volunteer.getUserId(), "志愿者申请未通过", "很抱歉，您的志愿者申请未通过。", "系统", id);
        }
    }

    /**
     * 删除志愿者申请记录
     *
     * @param id 申请 id
     */
    public void delete(Long id) {
        volunteerMapper.deleteById(id);
    }
}
