package com.ai.code.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author alh
 * @description: 字段匹配注解
 * @date 2026/04/28 13:00:09
 */
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = FieldMatchValidator.class)
public @interface FieldMatch {

    String message() default "字段不匹配";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
    
    // 定义需要比较的字段名
    String first();
    String second();
}