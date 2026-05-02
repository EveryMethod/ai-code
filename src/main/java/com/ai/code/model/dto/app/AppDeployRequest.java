package com.ai.code.model.dto.app;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @description 应用部署请求参数
 * @author alh
 * @date 2026/05/02 12:21
 */
@Data
public class AppDeployRequest implements Serializable {

    /**
     * 应用 id
     */
    @NotNull(message = "应用 id 不能为空")
    @Min(value = 0, message = "应用 id 不能小于 0")
    private Long appId;

    @Serial
    private static final long serialVersionUID = 1L;
}
