package com.ai.code.core.saver;

import com.ai.code.ai.model.HtmlCodeResult;
import com.ai.code.exception.BusinessException;
import com.ai.code.exception.ErrorCode;
import com.ai.code.model.enums.CodeGenTypeEnum;
import org.apache.commons.lang3.StringUtils;

/**
 * @author alh
 * @description html代码文件保存模版类
 * @date 2026/4/29 21:03
 */
public class HtmlCodeFileSaveTemplate extends BaseCodeFileSaverTemplate<HtmlCodeResult> {

    @Override
    protected CodeGenTypeEnum getCodeType() {
        return CodeGenTypeEnum.HTML;
    }

    @Override
    protected void saveFile(HtmlCodeResult result, String baseDirPath) {
        saveFile(baseDirPath, result.getHtmlCode(), "index.html");
    }

    @Override
    protected void validInput(HtmlCodeResult result) {
        super.validInput(result);
        if (StringUtils.isBlank(result.getHtmlCode())) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"html代码不能为空");
        }
    }


}
