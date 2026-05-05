package com.ai.code.core;

import cn.hutool.json.JSONUtil;
import com.ai.code.ai.AiCodeGeneratorService;
import com.ai.code.ai.AiCodeGeneratorServiceFactory;
import com.ai.code.ai.model.HtmlCodeResult;
import com.ai.code.ai.model.MultiFileCodeResult;
import com.ai.code.ai.model.message.AiResponseMessage;
import com.ai.code.ai.model.message.ToolExecutedMessage;
import com.ai.code.ai.model.message.ToolRequestMessage;
import com.ai.code.core.parse.CodeParseExecutor;
import com.ai.code.core.saver.CodeFileSaverExecutor;
import com.ai.code.model.enums.CodeGenTypeEnum;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.tool.ToolExecution;
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

    private final AiCodeGeneratorServiceFactory aiCodeGeneratorServiceFactory;

    /**
     * 根据用户消息生成代码并保存
     *
     * @param userMessage 用户消息
     * @param typeEnum    代码生成类型
     * @return 生成的代码文件
     */
    public File generatorAndSaveCode(String userMessage, CodeGenTypeEnum typeEnum, Long appId) {
        // 通过appId获取对应的AiCodeGeneratorService
        AiCodeGeneratorService aiCodeGeneratorService = aiCodeGeneratorServiceFactory.getAiCodeGeneratorService(appId, typeEnum);
        return switch (typeEnum) {
            case HTML -> {
                HtmlCodeResult result = aiCodeGeneratorService.generateHtmlCode(userMessage);
                yield CodeFileSaverExecutor.executeSaver(result, typeEnum, appId);
            }
            case MULTI_FILE -> {
                MultiFileCodeResult result = aiCodeGeneratorService.generateMultiFileCode(userMessage);
                yield CodeFileSaverExecutor.executeSaver(result, typeEnum, appId);
            }
            default -> throw new IllegalArgumentException("Invalid type enum: " + typeEnum);
        };
    }

    /**
     * 根据用户消息生成代码流
     *
     * @param userMessage 用户消息
     * @param typeEnum    代码生成类型
     * @return 生成的代码文件
     */
    public Flux<String> generatorAndSaveCodeStream(String userMessage, CodeGenTypeEnum typeEnum, Long appId) {
        // 通过appId获取对应的AiCodeGeneratorService
        AiCodeGeneratorService aiCodeGeneratorService = aiCodeGeneratorServiceFactory.getAiCodeGeneratorService(appId, typeEnum);
        return switch (typeEnum) {
            case HTML -> {
                Flux<String> result = aiCodeGeneratorService.generateHtmlCodeStream(userMessage);
                yield processCodeStream(result, CodeGenTypeEnum.HTML, appId);
            }
            case MULTI_FILE -> {
                Flux<String> result = aiCodeGeneratorService.generateMultiFileCodeStream(userMessage);
                yield processCodeStream(result, CodeGenTypeEnum.MULTI_FILE, appId);
            }
            case VUE_PROJECT -> {
                TokenStream tokenStream = aiCodeGeneratorService.generateVueProjectCodeStream(appId, userMessage);
                yield processTokenStream(tokenStream);
            }
            default -> throw new IllegalArgumentException("Invalid type enum: " + typeEnum);
        };
    }

    /**
     * 处理Token流
     *
     * @param tokenStream Token流
     * @return 流式响应对象
     */
    private Flux<String> processTokenStream(TokenStream tokenStream) {
        return Flux.create(sink -> {
            tokenStream.onPartialResponse((String partialResponse) -> {
                        sink.next(JSONUtil.toJsonStr(new AiResponseMessage(partialResponse)));
                    })
                    .onPartialToolExecutionRequest((index, toolExecutionRequest) -> {
                        sink.next(JSONUtil.toJsonStr(new ToolRequestMessage(toolExecutionRequest)));
                    })
                    .onToolExecuted((ToolExecution toolExecution) -> {
                        sink.next(JSONUtil.toJsonStr(new ToolExecutedMessage(toolExecution)));
                    })
                    .onCompleteResponse((ChatResponse chatResponse) -> {
                        sink.complete();
                    })
                    .onError((Throwable error) -> {
                        log.error("Error processing token stream: {}", error.getMessage());
//                        error.printStackTrace();
                        sink.error(error);
                    })
                    .start();
        });
    }


    /**
     * 处理代码流
     *
     * @param codeStream 代码流
     * @param typeEnum   代码生成类型
     * @return 生成的代码文件
     */
    private Flux<String> processCodeStream(Flux<String> codeStream, CodeGenTypeEnum typeEnum, Long appId) {
        StringBuilder codeBuilder = new StringBuilder();
        return codeStream
                .doOnNext(codeBuilder::append)
                .doOnComplete(() -> {
                    try {
                        String completeCode = codeBuilder.toString();
                        Object parseRes = CodeParseExecutor.executeParse(completeCode, typeEnum);
                        File file = CodeFileSaverExecutor.executeSaver(parseRes, typeEnum, appId);
                        log.info("File saved: {}", file.getAbsolutePath());
                    } catch (Exception e) {
                        log.error("Error saving file: {}", e.getMessage());
                    }
                });
    }
}
