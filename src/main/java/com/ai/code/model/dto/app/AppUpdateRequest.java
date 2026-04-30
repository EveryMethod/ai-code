package com.ai.code.model.dto.app;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * @description 应用更新请求DTO
 * @author alh
 * @date 2026-04-30 11:31:09
 */
@Data
public class AppUpdateRequest implements Serializable {

    /**
     * id
     */
    @NotNull(message = "id不能为空")
    private Long id;

    /**
     * 应用名称
     */
    private String appName;

    private static final long serialVersionUID = 1L;
}
