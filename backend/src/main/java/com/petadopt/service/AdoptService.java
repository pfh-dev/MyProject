package com.petadopt.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.petadopt.common.BusinessException;
import com.petadopt.common.PageResult;
import com.petadopt.entity.AdoptApplication;
import com.petadopt.entity.Pet;
import com.petadopt.mapper.AdoptApplicationMapper;
import com.petadopt.mapper.PetMapper;
import com.petadopt.security.AesUtil;
import com.petadopt.security.UserContext;
import com.petadopt.util.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 领养申请服务
 * <p>
 * 负责领养申请的全流程：用户提交申请 → 管理员审核 → 通过后联动宠物状态、
 * 自动生成回访计划并推送通知，形成「领养 → 回访」的完整业务闭环。
 * 涉及跨模块联动（宠物状态、回访计划、站内通知），因此审核方法加了事务保证数据一致。
 */
@Service
public class AdoptService {

    @Autowired
    private AdoptApplicationMapper adoptMapper;

    @Autowired
    private PetMapper petMapper;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private FollowUpService followUpService;

    @Autowired
    private AesUtil aesUtil;

    /**
     * 用户提交领养申请
     * <p>
     * 流程：
     * 1. 校验是否已对该宠物提交过申请（防止重复申请）；
     * 2. 校验宠物是否存在、是否处于可领养状态（待领养/领养中）；
     * 3. 记录申请人、加密手机号、置为「待审核」并写入申请时间。
     *
     * @param application 领养申请实体
     * @throws BusinessException 重复申请、宠物不存在或不可领养时抛出
     */
    public void apply(AdoptApplication application) {
        Long userId = UserContext.userId();
        // 防重复：同一用户对同一宠物只能申请一次
        if (adoptMapper.selectByPetAndUser(application.getPetId(), userId) != null) {
            throw new BusinessException("您已申请领养该宠物，请勿重复申请");
        }
        Pet pet = petMapper.selectById(application.getPetId());
        if (pet == null) {
            throw new BusinessException("宠物不存在");
        }
        // 只有「待领养」或「领养中」的宠物才允许申请
        if (!"待领养".equals(pet.getStatus()) && !"领养中".equals(pet.getStatus())) {
            throw new BusinessException("该宠物当前不可领养");
        }
        application.setUserId(userId);
        application.setPhone(aesUtil.encrypt(application.getPhone())); // 手机号加密入库
        application.setStatus("待审核");
        application.setApplyTime(DateUtil.now());
        adoptMapper.insert(application);
    }

    /**
     * 查询当前用户的领养申请列表
     *
     * @return 申请列表
     */
    public List<AdoptApplication> myList() {
        return adoptMapper.selectByUserId(UserContext.userId());
    }

    /**
     * 后台分页查询领养申请（支持状态、关键字筛选）
     * <p>
     * 查出列表后逐个解密手机号再返回。
     *
     * @param status   申请状态筛选
     * @param keyword  搜索关键字
     * @param pageNum  页码
     * @param pageSize 每页条数
     * @return 分页结果
     */
    public PageResult<AdoptApplication> page(String status, String keyword, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<AdoptApplication> list = adoptMapper.selectList(status, keyword);
        for (AdoptApplication application : list) {
            application.setPhone(aesUtil.decrypt(application.getPhone())); // 解密手机号
        }
        PageInfo<AdoptApplication> info = new PageInfo<>(list);
        return new PageResult<>(info.getTotal(), info.getList());
    }

    /**
     * 审核领养申请（核心联动方法，带事务）
     * <p>
     * 业务规则：
     * 1. 申请不存在或已审核过（状态非「待审核」）则拒绝操作，防止重复审核；
     * 2. 更新申请状态并记录审核人、审核时间、备注；
     * 3. 审核「已通过」→ 宠物状态改为「已领养」→ 自动生成回访计划 → 发送通过通知；
     * 4. 审核「已拒绝」→ 发送拒绝通知（附备注原因）。
     * <p>
     * 整个方法标记 @Transactional：任一步骤失败都会整体回滚，避免出现
     * 「申请通过了但宠物没改状态 / 回访计划没生成」的数据不一致。
     *
     * @param id     申请 id
     * @param status 审核结果（已通过/已拒绝）
     * @param remark 审核备注（拒绝原因等）
     * @throws BusinessException 申请不存在或已审核时抛出
     */
    @Transactional
    public void audit(Long id, String status, String remark) {
        AdoptApplication application = adoptMapper.selectById(id);
        if (application == null) {
            throw new BusinessException("申请不存在");
        }
        // 防止重复审核：只有「待审核」状态才允许操作
        if (!"待审核".equals(application.getStatus())) {
            throw new BusinessException("该申请已审核");
        }
        // 更新审核结果（含审核人、审核时间、备注）
        adoptMapper.updateAudit(id, status, UserContext.userId(), DateUtil.now(), remark);
        if ("已通过".equals(status)) {
            // 审核通过的三联动：改宠物状态 → 生成回访计划 → 通知申请人
            petMapper.updateStatus(application.getPetId(), "已领养");
            followUpService.createPlan(id);
            notificationService.send(application.getUserId(), "领养申请通过", "恭喜！您的领养申请已通过审核，请尽快联系平台安排领养。", "领养", id);
        } else {
            // 审核不通过：仅通知，宠物状态保持不变
            notificationService.send(application.getUserId(), "领养申请未通过",
                    "很抱歉，您的领养申请未通过。原因：" + (remark == null ? "" : remark), "领养", id);
        }
    }
}
