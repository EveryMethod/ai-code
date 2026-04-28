package com.ai.code.model.dto.user;

import com.ai.code.annotation.FieldMatch;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author alh
 * @description: 用户注册请求DTO
 * @date 2026/04/28 11:31:09
 */

@Data
@FieldMatch(first = "userPassword", second = "checkPassword", message = "密码不一致")
public class UserRegisterRequestDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 3191241716373120793L;

    /**
     * 账号
     */
    @NotBlank(message = "账号不能为空")
    @Size(min = 4, message = "账号长度不能小于4")
    private String userAccount;

    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空")
    @Size(min = 8, message = "密码长度不能小于8")
    private String userPassword;

    /**
     * 确认密码
     */
    @NotBlank(message = "确认密码不能为空")
    @Size(min = 8, message = "确认密码长度不能小于8")
    private String checkPassword;
}
