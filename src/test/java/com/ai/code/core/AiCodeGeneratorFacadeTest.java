package com.ai.code.core;

import com.ai.code.model.enums.CodeGenTypeEnum;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author alh
 * @description
 * @date 2026/4/29 15:38
 */
@SpringBootTest
class AiCodeGeneratorFacadeTest {

    @Resource
    private  AiCodeGeneratorFacade aiCodeGeneratorFacade;

    @Test
    void generatorAndSaveCode() {
        File file = aiCodeGeneratorFacade.generatorAndSaveCode("生成一个用户登录代码，不超过100行", CodeGenTypeEnum.MULTI_FILE, 1L);
        assertNotNull(file);
    }

    @Test
    void generatorAndSaveCodeStream() {
        Flux<String> flux = aiCodeGeneratorFacade.generatorAndSaveCodeStream("生成一个用户登录代码，不超过100行", CodeGenTypeEnum.MULTI_FILE, 1L);
        List<String> result = flux.collectList().block();
        assertNotNull(result);
        System.out.println(String.join("", result));
    }

    @Test
    void generateVueProjectCodeStream() {
        Flux<String> codeStream = aiCodeGeneratorFacade.generatorAndSaveCodeStream(
                "简单的任务记录网站，总代码量不超过 200 行",
                CodeGenTypeEnum.VUE_PROJECT, 1L);
        // 阻塞等待所有数据收集完成
        List<String> result = codeStream.collectList().block();
        // 验证结果
        Assertions.assertNotNull(result);
        String completeContent = String.join("", result);
        Assertions.assertNotNull(completeContent);
    }

}