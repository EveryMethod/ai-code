package com.ai.code.controller;

import com.ai.code.commom.BaseResponse;
import com.ai.code.commom.ResultUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author alh
 * @description 健康检查接口
 * @date 2026/4/27 16:39
 */
@RestController
@RequestMapping("/health")
public class HealthController {

    @GetMapping("/check")
    public BaseResponse<String> check() {
        return ResultUtils.success("ok");
    }
}
