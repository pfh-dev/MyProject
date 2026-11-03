package com.petadopt.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.petadopt.common.BusinessException;
import com.petadopt.common.PageResult;
import com.petadopt.entity.Pet;
import com.petadopt.entity.Rescue;
import com.petadopt.mapper.PetMapper;
import com.petadopt.mapper.RescueMapper;
import com.petadopt.security.AesUtil;
import com.petadopt.security.UserContext;
import com.petadopt.util.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 救助线索服务
 * <p>
 * 负责流浪动物救助线索的上报、分派与处理，并实现「救助 → 领养」的业务闭环：
 * 用户上报线索 → 管理员分派给志愿者 → 处理完成后，若成功救助则自动转成一条
 * 待领养的宠物档案，让被救助的动物进入领养流程。
 */
@Service
public class RescueService {

    @Autowired
    private RescueMapper rescueMapper;

    @Autowired
    private PetMapper petMapper;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private AesUtil aesUtil;

    /**
     * 用户上报救助线索
     * <p>
     * 记录上报人、置为「待处理」状态、加密联系方式并写入创建时间。
     *
     * @param rescue 救助线索实体
     */
    public void report(Rescue rescue) {
        rescue.setReporterId(UserContext.userId());
        rescue.setStatus("待处理");
        rescue.setContact(aesUtil.encrypt(rescue.getContact())); // 联系方式加密
        rescue.setCreateTime(DateUtil.now());
        rescueMapper.insert(rescue);
    }

    /**
     * 查询当前用户上报的救助线索列表
     * <p>
     * 查完后逐个解密联系方式再返回。
     *
     * @return 救助线索列表
     */
    public List<Rescue> myList() {
        List<Rescue> list = rescueMapper.selectByReporterId(UserContext.userId());
        for (Rescue rescue : list) {
            rescue.setContact(aesUtil.decrypt(rescue.getContact())); // 解密联系方式
        }
        return list;
    }

    /**
     * 后台分页查询救助线索（支持状态、关键字筛选）
     * <p>
     * 查出列表后逐个解密联系方式再返回。
     *
     * @param status   处理状态筛选
     * @param keyword  搜索关键字
     * @param pageNum  页码
     * @param pageSize 每页条数
     * @return 分页结果
     */
    public PageResult<Rescue> page(String status, String keyword, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Rescue> list = rescueMapper.selectList(status, keyword);
        for (Rescue rescue : list) {
            rescue.setContact(aesUtil.decrypt(rescue.getContact())); // 解密联系方式
        }
        PageInfo<Rescue> info = new PageInfo<>(list);
        return new PageResult<>(info.getTotal(), info.getList());
    }

    /**
     * 分派救助线索给指定志愿者
     * <p>
     * 分派后线索状态自动变为「处理中」。
     *
     * @param id         线索 id
     * @param assigneeId 负责处理该线索的志愿者/用户 id
     */
    public void assign(Long id, Long assigneeId) {
        rescueMapper.assign(id, assigneeId, "处理中");
    }

    /**
     * 处理救助线索（核心联动方法）
     * <p>
     * 业务规则：
     * 1. 更新线索处理状态、备注与处理时间；
     * 2. 若本次是从「非已救助」变为「已救助」，说明救助首次成功，自动转一条宠物档案
     *    （通过 newlyRescued 标记避免重复调用产生多条宠物）；
     * 3. 有上报人时推送处理结果通知。
     *
     * @param id     线索 id
     * @param status 处理结果（处理中/已救助/无法救助等）
     * @param remark 处理备注
     * @throws BusinessException 线索不存在时抛出
     */
    public void handle(Long id, String status, String remark) {
        Rescue rescue = rescueMapper.selectById(id);
        if (rescue == null) {
            throw new BusinessException("救助线索不存在");
        }
        // 只有「从未救助 → 已救助」时才转宠物档案，避免重复调用产生多条宠物
        boolean newlyRescued = "已救助".equals(status) && !"已救助".equals(rescue.getStatus());
        rescueMapper.handle(id, status, remark, DateUtil.now());
        if (newlyRescued) {
            createPetFromRescue(rescue); // 首次成功救助 → 自动建宠物档案
        }
        if (rescue.getReporterId() != null) {
            notificationService.send(rescue.getReporterId(), "救助线索处理结果",
                    "您上报的救助线索「" + rescue.getTitle() + "」处理状态更新为：" + status, "救助", id);
        }
    }

    /**
     * 救助成功后，把救助线索转成一条「待领养」的宠物档案，串起「救助 → 领养」闭环。
     * 品种/性别/年龄等详情信息由管理员后续在宠物管理里补充。
     *
     * @param rescue 已救助的线索
     */
    private void createPetFromRescue(Rescue rescue) {
        Pet pet = new Pet();
        pet.setName(rescue.getTitle()); // 先用线索标题作为宠物名字
        pet.setDescription(rescue.getDescription());
        pet.setImages(rescue.getImages());
        pet.setCoverImg(firstImage(rescue.getImages())); // 取第一张图作封面
        pet.setStatus("待领养");      // 新档案默认待领养
        pet.setSource("RESCUE");      // 来源标记为「救助」
        pet.setReviewStatus("已通过"); // 救助转入的档案默认已审核
        pet.setCreateUserId(UserContext.userId());
        pet.setCreateTime(DateUtil.now());
        petMapper.insert(pet);
    }

    /**
     * 从逗号分隔的图片串中取第一张图（用于封面）
     *
     * @param images 逗号分隔的图片地址串
     * @return 第一张图片地址，无图片返回 null
     */
    private String firstImage(String images) {
        if (images == null || images.isEmpty()) {
            return null;
        }
        for (String s : images.split(",")) {
            String t = s.trim();
            if (!t.isEmpty()) {
                return t;
            }
        }
        return null;
    }

    /**
     * 删除救助线索
     *
     * @param id 线索 id
     */
    public void delete(Long id) {
        rescueMapper.deleteById(id);
    }
}
