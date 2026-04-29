package com.ai.code.core.saver;


import com.ai.code.ai.model.HtmlCodeResult;
import com.ai.code.ai.model.MultiFileCodeResult;
import com.ai.code.model.enums.CodeGenTypeEnum;
import org.aspectj.apache.bcel.classfile.Code;

import java.io.File;

/**
 * @author alh
 * @description 代码文件保存执行器
 *              根据代码生成类型执行相应的代码文件保存逻辑
 * @date 2026/4/29 21:15
 */
public class CodeFileSaverExecutor {

    /**
     * HTML代码文件保存模板
     */
    private static final HtmlCodeFileSaveTemplate HTML_CODE_FILE_SAVE_TEMPLATE = new HtmlCodeFileSaveTemplate();

    /**
     * 多文件代码保存模板
     */
    private static final MultiFileCodeSaveTemplate MULTI_FILE_CODE_SAVE_TEMPLATE = new MultiFileCodeSaveTemplate();

    /**
     * 根据代码生成类型执行相应的代码文件保存逻辑
     *
     * @param codeResult 代码结果
     * @param codeGenType 代码生成类型
     * @return 保存的文件
     */
    public static File executeSaver(Object codeResult, CodeGenTypeEnum codeGenType) {
        return switch (codeGenType) {
            case HTML -> HTML_CODE_FILE_SAVE_TEMPLATE.saveCode((HtmlCodeResult) codeResult);
            case MULTI_FILE -> MULTI_FILE_CODE_SAVE_TEMPLATE.saveCode((MultiFileCodeResult) codeResult);
            default -> throw new IllegalArgumentException("Invalid code gen type: " + codeGenType);
        };
    }
}
