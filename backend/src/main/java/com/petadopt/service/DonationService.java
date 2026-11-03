package com.petadopt.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.petadopt.common.BusinessException;
import com.petadopt.common.PageResult;
import com.petadopt.entity.Donation;
import com.petadopt.mapper.DonationMapper;
import com.petadopt.security.UserContext;
import com.petadopt.util.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 捐赠服务
 * <p>
 * 负责爱心捐赠的发起、查询与确认闭环：
 * 用户提交捐赠 → 记录为「待确认」→ 管理员线下收款/核实后确认 → 通知捐赠人结果。
 */
@Service
public class DonationService {

    @Autowired
    private DonationMapper donationMapper;

    @Autowired
    private NotificationService notificationService;

    /**
     * 用户提交捐赠
     * <p>
     * 自动记录当前用户、置为「待确认」状态并写入捐赠时间，等待管理员线下核实。
     *
     * @param donation 捐赠实体
     */
    public void donate(Donation donation) {
        donation.setUserId(UserContext.userId());
        donation.setStatus("待确认");
        donation.setDonateTime(DateUtil.now());
        donationMapper.insert(donation);
    }

    /**
     * 查询当前用户的捐赠记录
     *
     * @return 捐赠列表
     */
    public List<Donation> myList() {
        return donationMapper.selectByUserId(UserContext.userId());
    }

    /**
     * 后台分页查询捐赠记录（支持状态、关键字筛选）
     *
     * @param status   捐赠状态筛选
     * @param keyword  搜索关键字
     * @param pageNum  页码
     * @param pageSize 每页条数
     * @return 分页结果
     */
    public PageResult<Donation> page(String status, String keyword, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Donation> list = donationMapper.selectList(status, keyword);
        PageInfo<Donation> info = new PageInfo<>(list);
        return new PageResult<>(info.getTotal(), info.getList());
    }

    /**
     * 确认捐赠（更新状态并通知捐赠人）
     * <p>
     * 管理员线下核实收款后调用，更新捐赠状态；若捐赠绑定了用户，则推送通知告知结果。
     *
     * @param id     捐赠记录 id
     * @param status 新的状态（如已确认/已驳回）
     * @throws BusinessException 记录不存在时抛出
     */
    public void confirm(Long id, String status) {
        Donation donation = donationMapper.selectById(id);
        if (donation == null) {
            throw new BusinessException("捐赠记录不存在");
        }
        // 更新捐赠状态
        donationMapper.updateStatus(id, status);
        // 有绑定用户时才发送通知（匿名捐赠可能没有 userId）
        if (donation.getUserId() != null) {
            notificationService.send(donation.getUserId(), "捐赠已确认",
                    "感谢您的爱心捐赠！捐赠状态已更新为：" + status, "系统", id);
        }
    }
}
