package com.springmvc.currentbp.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author baopan
 * @createTime 20181126
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})//作用到类上
public @interface MyController {
    String value();
}
