package com.ai.code.ai;

import com.ai.code.ai.model.HtmlCodeResult;
import com.ai.code.ai.model.MultiFileCodeResult;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import reactor.core.publisher.Flux;

/**
 * @author alh
 * @description ai代码生成接口
 * @date 2026/4/29 12:09
 */
public interface AiCodeGeneratorService {

    /**
     * 根据用户消息生成代码
     * @param userMessage 用户消息
     * @return 生成的代码
     */
    @SystemMessage(fromResource = "prompt/codegen-html-system-prompt.txt")
    HtmlCodeResult generateHtmlCode(String userMessage);

    /**
     * 根据用户消息生成多文件代码
     * @param userMessage 用户消息
     * @return 生成的代码
     */
    @SystemMessage(fromResource = "prompt/codegen-multi-file-system-prompt.txt")
    MultiFileCodeResult generateMultiFileCode(String userMessage);


    /**
     * 根据用户消息生成代码流
     * @param userMessage 用户消息
     * @return 生成的代码流
     */
    @SystemMessage(fromResource = "prompt/codegen-html-system-prompt.txt")
    Flux<String> generateHtmlCodeStream(String userMessage);

    /**
     * 根据用户消息生成多文件代码流
     * @param userMessage 用户消息
     * @return 生成的代码流
     */
    @SystemMessage(fromResource = "prompt/codegen-multi-file-system-prompt.txt")
    Flux<String> generateMultiFileCodeStream(String userMessage);
}
