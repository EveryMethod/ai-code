package com.ai.code.core.saver;

import com.ai.code.ai.model.MultiFileCodeResult;
import com.ai.code.exception.BusinessException;
import com.ai.code.exception.ErrorCode;
import com.ai.code.model.enums.CodeGenTypeEnum;
import org.apache.commons.lang3.StringUtils;

/**
 * @author alh
 * @description 多文件代码保存模版类
 * @date 2026/4/29 21:09
 */
public class MultiFileCodeSaveTemplate extends BaseCodeFileSaverTemplate<MultiFileCodeResult> {

    @Override
    protected CodeGenTypeEnum getCodeType() {
        return CodeGenTypeEnum.MULTI_FILE;
    }


    @Override
    protected void saveFile(MultiFileCodeResult result, String baseDirPath) {
        saveFile(baseDirPath, result.getHtmlCode(), "index.html");
        saveFile(baseDirPath, result.getCssCode(), "style.css");
        saveFile(baseDirPath, result.getJsCode(), "script.js");
    }

    @Override
    protected void validInput(MultiFileCodeResult result) {
        super.validInput(result);
        if (StringUtils.isBlank(result.getHtmlCode())) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"html代码不能为空");
        }
        if (StringUtils.isBlank(result.getCssCode())) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"css代码不能为空");
        }
        if (StringUtils.isBlank(result.getJsCode())) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"js代码不能为空");
        }
    }


}
