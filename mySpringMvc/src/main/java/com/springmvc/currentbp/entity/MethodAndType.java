package com.springmvc.currentbp.entity;

/**
 * @author baopan
 * @createTime 20181219
 */
public class MethodAndType {
    private String name;
    private Class<?> methodType;
    public MethodAndType(){}
    public MethodAndType(String name,Class<?> methodType){
        this.name=name;
        this.methodType=methodType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Class<?> getMethodType() {
        return methodType;
    }

    public void setMethodType(Class<?> methodType) {
        this.methodType = methodType;
    }

    @Override
    public String toString() {
        return "MethodAndType{" +
                "name='" + name + '\'' +
                ", methodType=" + methodType +
                '}';
    }
}
