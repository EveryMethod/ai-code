package com.ai.code.aop;

import com.ai.code.annotation.AuthCheck;
import com.ai.code.exception.BusinessException;
import com.ai.code.exception.ErrorCode;
import com.ai.code.model.entity.User;
import com.ai.code.model.enums.UserRoleEnum;
import com.ai.code.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * @author alh
 * @description 权限校验切面
 * @date 2026/4/28 16:47
 */
@Component
@Aspect
@Slf4j
@RequiredArgsConstructor
public class AuthInterceptor {

    private final UserService userService;

    @Around("@annotation(authCheck)")
    public Object doIntercept(ProceedingJoinPoint point, AuthCheck authCheck) throws Throwable {
        String mustRole = authCheck.mustRole();
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        User loginUser = userService.getLoginUser(request);
        // 登录用户角色的权限
        UserRoleEnum enumByValue = UserRoleEnum.getEnumByValue(loginUser.getUserRole());
        // 不需要权限，直接放行
        if (mustRole == null) {
            return point.proceed();
        }
        if (enumByValue == null) {
            // 登录用户角色没有权限，直接抛异常
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 方法需要的权限
        UserRoleEnum anEnum = UserRoleEnum.getEnumByValue(mustRole);
        if (UserRoleEnum.ADMIN.equals(anEnum) && !UserRoleEnum.ADMIN.equals(enumByValue)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 通过普通用户的权限校验，放行
        return point.proceed();
    }
}
