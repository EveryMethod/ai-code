package com.ai.code.ai.model;

import dev.langchain4j.model.output.structured.Description;
import lombok.Data;

/**
 * @author alh
 * @description html 代码生成结果
 * @date 2026/4/29 14:18
 */
@Data
@Description("html 代码生成结果")
public class HtmlCodeResult {

    /**
     * html 代码
     */
    @Description("html 代码")
    private String htmlCode;

    /**
     * 描述
     */
    @Description("描述")
    private String description;
}
