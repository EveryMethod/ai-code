package com.ai.code.ai;

import com.ai.code.service.ChatHistoryService;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import dev.langchain4j.community.store.memory.chat.redis.RedisChatMemoryStore;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * @author alh
 * @description ai 服务生产工厂
 * @date 2026/4/29 12:29
 */
@Configuration
@Slf4j
public class AiCodeGeneratorServiceFactory {
    /**
     * ChatModel
     */
    @Resource
    private ChatModel chatModel;

    @Resource
    private StreamingChatModel streamingChatModel;

    @Resource
    private RedisChatMemoryStore redisChatMemoryStore;

    @Resource
    private ChatHistoryService chatHistoryService;

    /**
     * AI 服务实例缓存
     * 缓存大小为1000，写入后30分钟过期，访问后10分钟过期
     */
    private final Cache<Long, AiCodeGeneratorService> serviceCache = Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(Duration.ofMinutes(30))
            .expireAfterAccess(Duration.ofMinutes(10))
            .removalListener((key, value, cause) -> {
                log.debug("AI 服务服务实例被移除，appId:{},原因：{}", key, value);
            })
            .build();

    /**
     * 创建 AI 服务实例
     *
     * @param appId 应用程序ID
     * @return AiCodeGeneratorService
     */
    private AiCodeGeneratorService createAiCodeGeneratorService(Long appId) {
        log.info("为 appId:{} 创建 AI 服务实例", appId);
        MessageWindowChatMemory memory = MessageWindowChatMemory.builder()
                .id(appId)
                .chatMemoryStore(redisChatMemoryStore)
                .maxMessages(20)
                .build();
        // 加载会话历史
        chatHistoryService.loadChatHistoryToMemory(appId, memory, 20);
        return AiServices.builder(AiCodeGeneratorService.class)
                .chatModel(chatModel)
                .streamingChatModel(streamingChatModel)
                .chatMemory(memory)
                .build();
    }

    @Bean
    public AiCodeGeneratorService aiCodeGeneratorService() {
        return getAiCodeGeneratorService(0L);
    }

    /**
     * 根据APPID获取独立的对话记忆
     *
     * @param appId 应用程序ID
     * @return AiCodeGeneratorService
     */
    public AiCodeGeneratorService getAiCodeGeneratorService(Long appId) {
        return serviceCache.get(appId, this::createAiCodeGeneratorService);
    }
}
