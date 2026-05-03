package com.ai.code.service;

import com.ai.code.model.dto.chatMemory.ChatHistoryQueryRequest;
import com.ai.code.model.entity.User;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.ai.code.model.entity.ChatHistory;

import java.time.LocalDateTime;

/**
 * 对话历史 服务层。
 *
 * @author alh
 * @since 2026-04-28 11:31:09
 */
public interface ChatHistoryService extends IService<ChatHistory> {


    /**
     * 保存对话历史
     *
     * @param appId     应用id
     * @param userId    用户id
     * @param message   对话内容
     * @param messageType   对话类型
     * @return 是否保存成功
     */
    boolean saveChatHistory(Long appId, Long userId, String message, String messageType);

    /**
     * 根据应用id删除对话历史
     *
     * @param appId 应用id
     * @return 是否删除成功
     */
    boolean deleteByAppId(Long appId);

    /**
     * 获取查询条件
     *
     * @param chatHistoryQueryRequest 查询条件
     * @return 查询条件
     */
    QueryWrapper getQueryWrapper(ChatHistoryQueryRequest chatHistoryQueryRequest);

    /**
     * 根据应用id分页查询对话历史
     *
     * @param appId       应用id
     * @param pageSize    页大小
     * @param lastCreateTime 最后创建时间
     * @param loginUser   登录用户
     * @return 对话历史分页列表
     */
    Page<ChatHistory> listAppChatHistoryByPage(Long appId, int pageSize,
                                               LocalDateTime lastCreateTime,
                                               User loginUser);
}
