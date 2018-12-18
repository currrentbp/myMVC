package com.currentbp.entity;

import java.lang.reflect.Method;

/**
 * @author baopan
 * @createTime 20181127
 */
public class ClassFunction {
    private String path;
    private String simpleClassName;
    private Method method;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getSimpleClassName() {
        return simpleClassName;
    }

    public void setSimpleClassName(String simpleClassName) {
        this.simpleClassName = simpleClassName;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    @Override
    public String toString() {
        return "ClassFunction{" +
                "path='" + path + '\'' +
                ", simpleClassName='" + simpleClassName + '\'' +
                ", method=" + method +
                '}';
    }
}
