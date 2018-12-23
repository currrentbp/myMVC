package com.springmvc.currentbp.entity;

/**
 * @author baopan
 * @createTime 20181214
 */
public class BeanRelation {

    private String beanName;
    private Class<?> classType;
    private Object bean;

    public BeanRelation() {
    }

    public BeanRelation(String beanName, Class<?> classType, Object bean) {
        this.bean = bean;
        this.beanName = beanName;
        this.classType = classType;
    }

    public String getBeanName() {
        return beanName;
    }

    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }

    public Class<?> getClassType() {
        return classType;
    }

    public void setClassType(Class<?> classType) {
        this.classType = classType;
    }

    public Object getBean() {
        return bean;
    }

    public void setBean(Object bean) {
        this.bean = bean;
    }

    @Override
    public String toString() {
        return "BeanRelation{" +
                "beanName='" + beanName + '\'' +
                ", classType=" + classType +
                ", bean=" + bean +
                '}';
    }
}
