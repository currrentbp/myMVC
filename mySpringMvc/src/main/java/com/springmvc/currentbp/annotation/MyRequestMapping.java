package com.springmvc.currentbp.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * controller的请求路径
 *
 * @author baopan
 * @createTime 20181126
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})//作用到类上和方法上
public @interface MyRequestMapping {
    String value();
}
