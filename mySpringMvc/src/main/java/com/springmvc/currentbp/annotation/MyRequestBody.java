package com.springmvc.currentbp.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 暂时没有使用，因为自动注入参数
 * @author baopan
 * @createTime 20181219
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Deprecated
public @interface MyRequestBody {
    boolean required() default true;
    String value() default "";
    String name() default "";
}
