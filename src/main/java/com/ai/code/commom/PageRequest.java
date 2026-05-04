package com.ai.code.commom;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import lombok.Data;

/**
 * @author alh
 * @description 分页请求参数
 * @date 2026/04/27 11:09
 */
@Data
public class PageRequest {

    /**
     * 当前页号
     */
    private int pageNum = 1;

    /**
     * 页面大小
     */
//    @Max(value = 20, message = "页面大小不能超过20")
    private int pageSize = 10;

    /**
     * 排序字段
     */
    private String sortField;

    /**
     * 排序顺序（默认降序）
     */
    private String sortOrder = "desc";
}
