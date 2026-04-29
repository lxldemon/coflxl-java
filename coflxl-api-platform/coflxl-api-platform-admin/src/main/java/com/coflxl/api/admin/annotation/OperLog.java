package com.coflxl.api.admin.annotation;

import java.lang.annotation.*;

@Target({ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperLog {
    /**
     * 模块
     */
    String title() default "";

    /**
     * 功能
     */
    String businessType() default "OTHER";
}
