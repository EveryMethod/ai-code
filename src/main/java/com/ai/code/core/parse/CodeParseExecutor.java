package com.ai.code.core.parse;

import com.ai.code.model.enums.CodeGenTypeEnum;

/**
 * @author alh
 * @description 根据代码生成类型执行相应的解析逻辑
 * @date 2026/4/29 20:30
 */
public class CodeParseExecutor {

    /**
     * HTML代码解析
     */
    private static final HtmlCodeParse HTML_CODE_PARSE = new HtmlCodeParse();

    /**
     * 多文件代码解析
     */
    private static final MultiFileCodeParse MULTI_FILE_CODE_PARSE = new MultiFileCodeParse();

    /**
     * 根据代码生成类型执行相应的解析逻辑
     * @param codeContent 代码内容
     * @param typeEnum 代码生成类型
     * @return 解析结果
     */
    public static Object executeParse(String codeContent, CodeGenTypeEnum typeEnum) {
        return switch (typeEnum) {
            case HTML -> HTML_CODE_PARSE.parseCode(codeContent);
            case MULTI_FILE -> MULTI_FILE_CODE_PARSE.parseCode(codeContent);
            default -> throw new IllegalArgumentException("Invalid type enum: " + typeEnum);
        };
    }
}
