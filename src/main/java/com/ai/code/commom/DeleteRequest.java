package com.ai.code.commom;

import jakarta.validation.constraints.Min;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author alh
 * @description 删除请求参数
 * @date 2026/04/27 11:09
 */
@Data
public class DeleteRequest implements Serializable {

    /**
     * id
     */
    @Min(value = 0, message = "id不能小于0")
    private Long id;

    @Serial
    private static final long serialVersionUID = 1L;
}
