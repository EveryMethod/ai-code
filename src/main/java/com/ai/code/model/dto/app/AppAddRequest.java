package com.ai.code.model.dto.app;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @description 应用添加请求
 * @author alh
 * @date 2026/04/30 11:31:09
 */
@Data
public class AppAddRequest implements Serializable {

    /**
     * 应用初始化的 prompt
     */
    @NotBlank(message = "应用初始化的 prompt 不能为空")
    private String initPrompt;

    @Serial
    private static final long serialVersionUID = 1L;
}
