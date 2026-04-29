package com.ai.code.ai;

import com.ai.code.ai.model.HtmlCodeResult;
import com.ai.code.ai.model.MultiFileCodeResult;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author alh
 * @description
 * @date 2026/4/29 12:40
 */
@SpringBootTest
class AiCodeGeneratorServiceTest {

    @Resource
    private AiCodeGeneratorService aiCodeGeneratorService;

    @Test
    void generateHtmlCode() {
        HtmlCodeResult code = aiCodeGeneratorService.generateHtmlCode("生成一个简单的html页面，页面上有一个按钮，点击按钮弹出一个对话框，对话框上显示“hello world”");
        Assertions.assertNotNull(code);
    }

    @Test
    void generateMultiFileCode() {
        MultiFileCodeResult code = aiCodeGeneratorService.generateMultiFileCode("生成一个简单的博客html页面，不超过20行代码");
        Assertions.assertNotNull(code);
    }
}