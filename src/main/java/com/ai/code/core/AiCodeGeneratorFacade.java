package com.ai.code.core;

import com.ai.code.ai.AiCodeGeneratorService;
import com.ai.code.ai.model.HtmlCodeResult;
import com.ai.code.ai.model.MultiFileCodeResult;
import com.ai.code.core.parse.CodeParseExecutor;
import com.ai.code.core.saver.CodeFileSaverExecutor;
import com.ai.code.model.enums.CodeGenTypeEnum;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.File;

/**
 * @author alh
 * @description 代码生成 门面模式主类
 * @date 2026/4/29 15:21
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AiCodeGeneratorFacade {

    private final AiCodeGeneratorService aiCodeGeneratorService;

    /**
     * 根据用户消息生成代码并保存
     * @param userMessage 用户消息
     * @param typeEnum 代码生成类型
     * @return 生成的代码文件
     */
    public File generatorAndSaveCode(String userMessage, @NotNull CodeGenTypeEnum typeEnum) {
        return switch (typeEnum)  {
            case HTML -> {
                HtmlCodeResult result = aiCodeGeneratorService.generateHtmlCode(userMessage);
                yield CodeFileSaverExecutor.executeSaver(result, typeEnum);
            }
            case MULTI_FILE -> {
                MultiFileCodeResult result = aiCodeGeneratorService.generateMultiFileCode(userMessage);
                yield CodeFileSaverExecutor.executeSaver(result, typeEnum);
            }
            default -> throw new IllegalArgumentException("Invalid type enum: " + typeEnum);
        };
    }

    /**
     * 根据用户消息生成代码流
     * @param userMessage 用户消息
     * @param typeEnum 代码生成类型
     * @return 生成的代码文件
     */
    public Flux<String> generatorAndSaveCodeStream(String userMessage, @NotNull CodeGenTypeEnum typeEnum) {
        return switch (typeEnum)  {
            case HTML -> {
                Flux<String> result = aiCodeGeneratorService.generateHtmlCodeStream(userMessage);
                yield  processCodeStream(result, typeEnum);
            }
            case MULTI_FILE -> {
                Flux<String> result = aiCodeGeneratorService.generateMultiFileCodeStream(userMessage);
                yield processCodeStream(result, typeEnum);
            }
            default -> throw new IllegalArgumentException("Invalid type enum: " + typeEnum);
        };
    }


    /**
     * 处理代码流
     * @param codeStream 代码流
     * @param typeEnum 代码生成类型
     * @return 生成的代码文件
     */
    private Flux<String> processCodeStream(Flux<String> codeStream, CodeGenTypeEnum typeEnum) {
        StringBuilder codeBuilder = new StringBuilder();
        return codeStream
                .doOnNext(codeBuilder::append)
                .doOnComplete(() -> {
                    try {
                        String completeCode = codeBuilder.toString();
                        Object parseRes = CodeParseExecutor.executeParse(completeCode, typeEnum);
                        File file = CodeFileSaverExecutor.executeSaver(parseRes, typeEnum);
                        log.info("File saved: {}", file.getAbsolutePath());
                    } catch (Exception e) {
                        log.error("Error saving file: {}", e.getMessage());
                    }
                });
    }
}
