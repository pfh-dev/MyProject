package com.petadopt.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 分页结果封装。
 *
 * <p>分页接口（如宠物列表）除了返回数据本身，还需要告诉前端"一共有多少条"，
 * 前端才能算出总页数、渲染分页器。所以用这个对象同时携带总数和当页数据。</p>
 *
 * @param <T> 列表元素类型，例如宠物实体 Pet、文章实体 Article
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> {

    /** 符合条件的总记录数（用于前端算总页数） */
    private Long total;
    /** 当前页的数据列表 */
    private List<T> list;
}
