package com.petadopt.ai;

import com.petadopt.entity.AdoptApplication;
import com.petadopt.entity.Pet;
import com.petadopt.entity.PetCategory;
import com.petadopt.mapper.AdoptApplicationMapper;
import com.petadopt.mapper.PetCategoryMapper;
import com.petadopt.mapper.PetMapper;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * AI Agent 可调用的工具集（查询数据库回答宠物 / 领养相关问题）。
 *
 * 概念说明——「工具（Tool）」：大模型只有语言能力，没有眼睛去看数据库。
 * 我们把「查宠物」「查领养进度」等能力包装成一个个方法，用 @Tool 注解标记后交给 AI。
 * 当用户的问题需要这些数据时，模型会自动「调用工具」→ 拿到真实结果 → 再总结成自然语言回答。
 *
 * @Component("petTools")：注册成 Spring Bean 且命名为 "petTools"，
 * 对应 @AiService 里 tools = "petTools" 的引用。
 */
@Component("petTools")
public class PetTools {

    @Autowired
    private PetMapper petMapper;

    @Autowired
    private PetCategoryMapper categoryMapper;

    @Autowired
    private AdoptApplicationMapper adoptMapper;

    /**
     * 工具：搜索可领养的宠物，可按名称关键字或分类过滤。
     *
     * @Tool：告诉 LangChain4j 这是一个可供 AI 调用的工具，括号里的中文是对工具功能的描述，
     *        模型会据此判断「什么时候该调它」。
     * @P：描述该参数的用途，让模型知道该往这个参数里填什么（参数说明会随工具定义一起发给模型）。
     *
     * 逻辑说明：
     *  1. 分类名（如「猫咪」）不是直接查库，而是先遍历全部分类，
     *     用「互相包含」的方式模糊匹配出真实的分类 ID；
     *  2. 拿名称关键字 + 分类 ID 去查「公开可领养」的宠物列表；
     *  3. 最多返回前 10 条，拼成一段人类可读的文本给模型。
     */
    @Tool("搜索可领养的宠物，可按名称关键字或分类过滤")
    public String searchPets(@P("宠物名称关键字，可为空") String name,
                             @P("分类名称，如：猫咪、狗狗，可为空") String category) {
        Long categoryId = null;
        if (category != null && !category.isEmpty()) {
            for (PetCategory c : categoryMapper.selectAll()) {
                if (c.getName().contains(category) || category.contains(c.getName())) {
                    categoryId = c.getId();
                    break;
                }
            }
        }
        List<Pet> pets = petMapper.selectPublicList(name, categoryId);
        if (pets == null || pets.isEmpty()) {
            return "暂未找到符合条件的可领养宠物。";
        }
        StringBuilder sb = new StringBuilder("找到以下可领养宠物：\n");
        int limit = Math.min(pets.size(), 10);
        for (int i = 0; i < limit; i++) {
            Pet p = pets.get(i);
            sb.append(i + 1).append(". ").append(p.getName())
                    .append("（").append(p.getBreed() == null ? "品种未知" : p.getBreed())
                    .append("，").append(p.getGender() == null ? "" : p.getGender())
                    .append("，").append(p.getAge() == null ? "" : p.getAge()).append("）")
                    .append(" - ").append(p.getDescription() == null ? "" : p.getDescription())
                    .append("\n");
        }
        return sb.toString();
    }

    /**
     * 工具：获取指定宠物的详细信息。
     * @P("宠物ID") 告诉模型：调用本工具时，需要传入一个宠物 ID（long 类型）。
     * 返回：把宠物的名称、品种、性别、年龄、疫苗、绝育、简介、领养状态拼成文本。
     */
    @Tool("获取指定宠物的详细信息")
    public String getPetDetail(@P("宠物ID") long petId) {
        Pet p = petMapper.selectById(petId);
        if (p == null) {
            return "未找到该宠物。";
        }
        return "宠物信息：名称=" + p.getName()
                + "，品种=" + (p.getBreed() == null ? "未知" : p.getBreed())
                + "，性别=" + (p.getGender() == null ? "未知" : p.getGender())
                + "，年龄=" + (p.getAge() == null ? "未知" : p.getAge())
                + "，疫苗=" + (p.getVaccine() == null ? "未知" : p.getVaccine())
                + "，绝育=" + (p.getSterilization() == null ? "未知" : p.getSterilization())
                + "，简介=" + (p.getDescription() == null ? "无" : p.getDescription())
                + "，领养状态=" + p.getStatus();
    }

    /**
     * 工具：查询指定用户的领养申请进度。
     * 根据用户 ID 查出其所有领养申请，逐条拼出「宠物名 + 审核状态 +（可选）审核备注」。
     */
    @Tool("查询指定用户的领养申请进度")
    public String getAdoptStatus(@P("用户ID") long userId) {
        List<AdoptApplication> list = adoptMapper.selectByUserId(userId);
        if (list == null || list.isEmpty()) {
            return "该用户暂无领养申请记录。";
        }
        StringBuilder sb = new StringBuilder("领养申请记录：\n");
        for (AdoptApplication a : list) {
            sb.append("- 宠物=").append(a.getPetName())
                    .append("，状态=").append(a.getStatus());
            if (a.getAuditRemark() != null && !a.getAuditRemark().isEmpty()) {
                sb.append("，备注=").append(a.getAuditRemark());
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    /**
     * 工具：获取救助流浪动物的通用建议。
     * 这个工具不查库，直接返回一段固定文案，供 AI 在回答「怎么救助流浪动物」时引用。
     */
    @Tool("获取救助流浪动物的通用建议")
    public String getRescueTips() {
        return "救助流浪动物建议：1. 先观察其健康状况和情绪，避免贸然接触；"
                + "2. 可提供干净的水和食物；3. 若受伤或攻击性强，请联系专业机构；"
                + "4. 通过平台「救助上报」功能提交线索，等待工作人员处理。";
    }
}
