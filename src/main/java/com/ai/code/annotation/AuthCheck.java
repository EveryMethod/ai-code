package com.ai.code.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author alh
 * @description 权限校验
 * @date 2026/4/28 16:43
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AuthCheck {

    /**
     * 必须有的角色 默认为空表示没有角色限制
     */
    String mustRole() default "";
}
