package com.springmvc.currentbp.entity;

import java.lang.reflect.Method;
import java.util.List;

/**
 * @author baopan
 * @createTime 20181127
 */
public class ClassFunction {
    private String path;
    private String simpleClassName;
    private Method method;

    private List<MethodAndType> params;

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

    public List<MethodAndType> getParams() {
        return params;
    }

    public void setParams(List<MethodAndType> params) {
        this.params = params;
    }

    @Override
    public String toString() {
        return "ClassFunction{" +
                "path='" + path + '\'' +
                ", simpleClassName='" + simpleClassName + '\'' +
                ", method=" + method +
                ", params=" + params +
                '}';
    }
}
