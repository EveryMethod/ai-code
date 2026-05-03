package com.ai.code.model.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import java.io.Serializable;
import java.time.LocalDateTime;

import java.io.Serial;

import com.mybatisflex.core.keygen.KeyGenerators;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 对话历史 实体类。
 *
 * @author alh
 * @since 2026-04-28 11:31:09
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("chat_history")
public class ChatHistory implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @Id(keyType = KeyType.Generator, value = KeyGenerators.snowFlakeId)
    @Column("id")
    private Long id;

    /**
     * 消息
     */
    @Column("message")
    @NotBlank(message = "消息不能为空")
    private String message;

    /**
     * user/ai
     */
    @Column("message_type")
    @NotBlank(message = "消息类型不能为空")
    private String messageType;

    /**
     * 应用id
     */
    @Column("app_id")
    @NotNull(message = "应用id不能为空")
    @Min(value = 0, message = "应用id不能小于0")
    private Long appId;

    /**
     * 创建用户id
     */
    @Column("user_id")
    @NotNull(message = "创建用户id不能为空")
    private Long userId;

    /**
     * 创建时间
     */
    @Column("create_time")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Column("update_time")
    private LocalDateTime updateTime;

    /**
     * 是否删除
     */
    @Column(value = "is_delete", isLogicDelete = true)
    private Integer isDelete;

}
