package com.currentbp.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author baopan
 * @createTime 20181126
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})//作用到属性上
public @interface MyAutoWire {
}
