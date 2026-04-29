package com.ai.code.ai.model;

import dev.langchain4j.model.output.structured.Description;
import lombok.Data;

/**
 * @author alh
 * @description 多文件 代码结果
 * @date 2026/4/29 14:20
 */
@Data
@Description("多文件 代码结果")
public class MultiFileCodeResult {

    /**
     * html代码
     */
    @Description("html 代码")
    private String htmlCode;

    /**
     * 描述
     */
    @Description("描述")
    private String description;

    /**
     * css 代码
     */
    @Description("css 代码")
    private String cssCode;

    /**
     * js 代码
     */
    @Description("js 代码")
    private String jsCode;
}
