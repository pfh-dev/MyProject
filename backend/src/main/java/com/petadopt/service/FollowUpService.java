package com.petadopt.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.petadopt.common.PageResult;
import com.petadopt.entity.FollowUp;
import com.petadopt.mapper.FollowUpMapper;
import com.petadopt.mapper.VolunteerMapper;
import com.petadopt.security.AesUtil;
import com.petadopt.security.UserContext;
import com.petadopt.util.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 领养回访服务
 * <p>
 * 负责领养成功后的定期回访。系统会在领养审核通过后自动生成一条「待回访计划」，
 * 之后由志愿者上门/电话回访并补写回访记录，形成对领养宠物的长期跟踪闭环。
 */
@Service
public class FollowUpService {

    @Autowired
    private FollowUpMapper followUpMapper;

    @Autowired
    private VolunteerMapper volunteerMapper;

    @Autowired
    private AesUtil aesUtil;

    /**
     * 新增回访记录
     * <p>
     * 若前端未指定志愿者、且当前登录用户本身是志愿者，则自动把当前用户关联为该回访的执行志愿者。
     *
     * @param followUp 回访实体
     */
    public void add(FollowUp followUp) {
        // 未指定志愿者且当前用户是志愿者时，自动回填当前用户的志愿者信息
        if (followUp.getVolunteerId() == null && "VOLUNTEER".equals(UserContext.role())) {
            var volunteer = volunteerMapper.selectByUserId(UserContext.userId());
            if (volunteer != null) {
                followUp.setVolunteerId(volunteer.getId());
            }
        }
        followUp.setCreateTime(DateUtil.now());
        followUpMapper.insert(followUp);
    }

    /**
     * 领养通过后自动生成一条「待回访计划」。
     * <p>
     * 约定：follow_time 为未来时间、content 为空，表示尚未执行的计划；
     * 志愿者实际回访后补写 content 即成为记录。
     *
     * @param adoptId 领养申请 id
     */
    public void createPlan(Long adoptId) {
        FollowUp followUp = new FollowUp();
        followUp.setAdoptId(adoptId);
        followUp.setFollowTime(DateUtil.afterDays(7)); // 计划在 7 天后回访
        followUp.setCreateTime(DateUtil.now());
        followUpMapper.insert(followUp);
    }

    /**
     * 查询某条领养申请对应的全部回访记录
     *
     * @param adoptId 领养申请 id
     * @return 回访列表
     */
    public List<FollowUp> listByAdopt(Long adoptId) {
        return followUpMapper.selectByAdoptId(adoptId);
    }

    /**
     * 后台分页查询回访记录（支持关键字、回访状态筛选）
     * <p>
     * 查出列表后逐个解密领养人电话，并根据领养通过时间（audit_time）计算
     * 「领养天数」，方便后台直接展示「领养了多久、该回访了」。
     *
     * @param status   回访状态筛选（待回访/已回访，空为全部）
     * @param keyword  搜索关键字
     * @param pageNum  页码
     * @param pageSize 每页条数
     * @return 分页结果
     */
    public PageResult<FollowUp> page(String status, String keyword, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<FollowUp> list = followUpMapper.selectList(keyword, status);
        for (FollowUp followUp : list) {
            followUp.setPhone(aesUtil.decrypt(followUp.getPhone())); // 解密领养人电话
            followUp.setAdoptDays((int) DateUtil.daysSince(followUp.getAuditTime())); // 计算领养天数
        }
        PageInfo<FollowUp> info = new PageInfo<>(list);
        return new PageResult<>(info.getTotal(), info.getList());
    }

    /**
     * 登记回访：把一条「待回访计划」补充成「已回访记录」。
     * <p>
     * 志愿者/管理员回访后填写内容、宠物状况、是否正常。若当前登录用户本身是
     * 志愿者且未指定执行志愿者，则自动把该志愿者关联为本次回访的执行人。
     *
     * @param followUp 回访实体（id 必填，其余字段按需填写）
     */
    public void update(FollowUp followUp) {
        if (followUp.getVolunteerId() == null && "VOLUNTEER".equals(UserContext.role())) {
            var volunteer = volunteerMapper.selectByUserId(UserContext.userId());
            if (volunteer != null) {
                followUp.setVolunteerId(volunteer.getId());
            }
        }
        if (followUp.getFollowTime() == null) {
            followUp.setFollowTime(DateUtil.now());
        }
        followUpMapper.updateById(followUp);
    }

    /**
     * 删除回访记录
     *
     * @param id 回访记录 id
     */
    public void delete(Long id) {
        followUpMapper.deleteById(id);
    }
}
