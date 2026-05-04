package com.ai.code.service.impl;

import cn.hutool.core.util.StrUtil;
import com.ai.code.constant.UserConstant;
import com.ai.code.exception.ErrorCode;
import com.ai.code.exception.ThrowUtils;
import com.ai.code.model.dto.chatMemory.ChatHistoryQueryRequest;
import com.ai.code.model.entity.App;
import com.ai.code.model.entity.User;
import com.ai.code.model.enums.ChatHistoryMessageTypeEnum;
import com.ai.code.service.AppService;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.ai.code.model.entity.ChatHistory;
import com.ai.code.mapper.ChatHistoryMapper;
import com.ai.code.service.ChatHistoryService;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 对话历史 服务层实现。
 *
 * @author alh
 * @since 2026-04-28 11:31:09
 */
@Service
//@RequiredArgsConstructor
@Slf4j
public class ChatHistoryServiceImpl extends ServiceImpl<ChatHistoryMapper, ChatHistory>  implements ChatHistoryService{

    @Lazy
    @Resource
    private AppService appService;

    @Override
    public boolean saveChatHistory(Long appId, Long userId, String message, String messageType) {
        // 校验消息类型是否有效
        ChatHistoryMessageTypeEnum messageTypeEnum = ChatHistoryMessageTypeEnum.getEnumByValue(messageType);
        ThrowUtils.throwIf(messageTypeEnum == null, ErrorCode.PARAMS_ERROR, "无效的消息类型");
        return this.save(ChatHistory.builder()
                .appId(appId)
                .message(message)
                .userId(userId)
                .messageType(messageTypeEnum.getValue())
                .build());
    }

    @Override
    public boolean deleteByAppId(Long appId) {
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID不能为空");
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq("appId", appId);
        return this.remove(queryWrapper);
    }

    /**
     * 获取查询包装类
     *
     * @param chatHistoryQueryRequest 聊天历史查询请求
     * @return 查询包装类
     */
    @Override
    public QueryWrapper getQueryWrapper(ChatHistoryQueryRequest chatHistoryQueryRequest) {
        QueryWrapper queryWrapper = QueryWrapper.create();
        if (chatHistoryQueryRequest == null) {
            return queryWrapper;
        }
        Long id = chatHistoryQueryRequest.getId();
        String message = chatHistoryQueryRequest.getMessage();
        String messageType = chatHistoryQueryRequest.getMessageType();
        Long appId = chatHistoryQueryRequest.getAppId();
        Long userId = chatHistoryQueryRequest.getUserId();
        LocalDateTime lastCreateTime = chatHistoryQueryRequest.getLastCreateTime();
        String sortField = chatHistoryQueryRequest.getSortField();
        String sortOrder = chatHistoryQueryRequest.getSortOrder();
        // 拼接查询条件
        queryWrapper.eq("id", id)
                .like("message", message)
                .eq("message_type", messageType)
                .eq("app_id", appId)
                .eq("user_id", userId);
        // 游标查询逻辑 - 只使用 createTime 作为游标
        if (lastCreateTime != null) {
            queryWrapper.lt("createTime", lastCreateTime);
        }
        // 排序
        if (StrUtil.isNotBlank(sortField)) {
            queryWrapper.orderBy(sortField, "ascend".equals(sortOrder));
        } else {
            // 默认按创建时间降序排列
            queryWrapper.orderBy("create_time", false);
        }
        return queryWrapper;
    }

    @Override
    public Page<ChatHistory> listAppChatHistoryByPage(Long appId, int pageSize,
                                                      LocalDateTime lastCreateTime,
                                                      User loginUser) {
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID不能为空");
        ThrowUtils.throwIf(pageSize <= 0 || pageSize > 50, ErrorCode.PARAMS_ERROR, "页面大小必须在1-50之间");
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        // 验证权限：只有应用创建者和管理员可以查看
        App app = appService.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        boolean isAdmin = UserConstant.ADMIN_ROLE.equals(loginUser.getUserRole());
        boolean isCreator = app.getUserId().equals(loginUser.getId());
        ThrowUtils.throwIf(!isAdmin && !isCreator, ErrorCode.NO_AUTH_ERROR, "无权查看该应用的对话历史");
        // 构建查询条件
        ChatHistoryQueryRequest queryRequest = new ChatHistoryQueryRequest();
        queryRequest.setAppId(appId);
        queryRequest.setLastCreateTime(lastCreateTime);
        QueryWrapper queryWrapper = this.getQueryWrapper(queryRequest);
        // 查询数据
        return this.page(Page.of(1, pageSize), queryWrapper);
    }

    /**
     * 加载对话历史到对话记忆中
     *
     * @param appId       应用ID
     * @param memory      对话记忆
     * @param maxMessages 最大消息数
     * @return 加载的消息数
     */
    @Override
    public int loadChatHistoryToMemory(Long appId, MessageWindowChatMemory memory, int maxMessages) {
        try {
            List<ChatHistory> chatHistory = this.list(QueryWrapper.create()
                    .eq(ChatHistory::getAppId, appId)
                    .orderBy(ChatHistory::getCreateTime, false)
                    .limit(1, maxMessages));
            if (CollectionUtils.isEmpty(chatHistory)) {
                return 0;
            }
            // 反转列表，确保按照时间正序
            chatHistory = chatHistory.reversed();
            // 按照时间顺序将消息加载到对话记忆中
            int count = 0;
            // 先清理对话历史缓存，防止重复加载
            memory.clear();
            for (ChatHistory history : chatHistory) {
                if (ChatHistoryMessageTypeEnum.USER.getValue().equals(history.getMessageType())) {
                    memory.add(UserMessage.from(history.getMessage()));
                } else if (ChatHistoryMessageTypeEnum.AI.getValue().equals(history.getMessageType())) {
                    memory.add(AiMessage.from(history.getMessage()));
                }
                count++;
            }
            log.info("Loaded {} messages into memory for app {}", count, appId);
            return count;
        } catch (Exception e) {
            log.error("Error loading chat history into memory for app {}", appId, e);
            return 0;
        }
    }



}
