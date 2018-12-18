package com.currentbp.servlet;


import com.currentbp.annotation.MyAutoWire;
import com.currentbp.annotation.MyBean;
import com.currentbp.annotation.MyController;
import com.currentbp.annotation.MyRequestMapping;
import com.currentbp.entity.BeanRelation;
import com.currentbp.entity.ClassFunction;
import com.currentbp.util.all.ListUtil;
import com.currentbp.util.all.StringUtil;
import org.junit.Test;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

/**
 * @author baopan
 * @createTime 20181126
 */
public class MyDispatcherServlet extends HttpServlet {
    private List<String> classPath = new ArrayList<>();
    private List<String> controllerBeans = new ArrayList<>();
    private Map<String, BeanRelation> allBeanMap = new HashMap<>();
    private List<String> needAutoWireBeanNames = new ArrayList<>();
    private Map<String, ClassFunction> controllerPath = new HashMap<>();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        dispatch(req, resp);
    }

    @Override
    public void init() throws ServletException {
        super.init();
        //1、扫描
        doScan();
        //2、实例化bean
        doAutoWireBean();
        //3、路径分发
        doPath();
    }

    private void doPath() {
        for (String controllerBean : controllerBeans) {
            BeanRelation beanRelation = allBeanMap.get(controllerBean);
            Class<?> classType = beanRelation.getClassType();
            String basePath = "";
            if (classType.isAnnotationPresent(MyRequestMapping.class)) {
                MyRequestMapping annotation = classType.getAnnotation(MyRequestMapping.class);
                basePath = annotation.value();
                if (!basePath.substring(0, 1).equals("/")) {
                    basePath = "/" + basePath;
                }
            }
            Method[] methods = classType.getMethods();
            for (Method method : methods) {
                if (method.isAnnotationPresent(MyRequestMapping.class)) {
                    String otherPath = method.getAnnotation(MyRequestMapping.class).value();
                    if (!otherPath.substring(0, 1).equals("/")) {
                        otherPath = "/" + otherPath;
                    }
                    ClassFunction classFunction = new ClassFunction();
                    classFunction.setPath(basePath + otherPath);
                    classFunction.setMethod(method);
                    classFunction.setSimpleClassName(beanRelation.getBeanName());
                    controllerPath.put(classFunction.getPath(), classFunction);
                }
            }
        }
    }

    private void doAutoWireBean() {
        for (String className : classPath) {
            String simpleName;
            try {
                //1、找出需要实例化的bean
                Class<?> aClass = Class.forName(className.replace("/", ".").replace(".class", ""));
                simpleName = StringUtil.getHumpFormat(aClass.getSimpleName());

                try {
                    if (aClass.isAnnotationPresent(MyController.class)) {
                        MyController annotation = aClass.getAnnotation(MyController.class);
                        String controllerName = annotation.value();
                        Object newInstance = aClass.newInstance();
                        controllerBeans.add(controllerName);
                        allBeanMap.put(controllerName, new BeanRelation(controllerName, aClass, newInstance));
                    } else if (aClass.isAnnotationPresent(MyBean.class)) {
                        MyBean annotation = aClass.getAnnotation(MyBean.class);
                        String beanName = annotation.value();
                        allBeanMap.put(beanName, new BeanRelation(beanName, aClass, aClass.newInstance()));
                    }
                } catch (Exception e) {
                    System.out.println("遍历bean错误:" + e.toString());
                }
                //2、找出需要填充bean的地方
                Field[] declaredFields = aClass.getDeclaredFields();
                for (Field field : declaredFields) {
                    boolean fieldAnnotationPresent = field.isAnnotationPresent(MyAutoWire.class);
                    if (fieldAnnotationPresent) {
                        needAutoWireBeanNames.add(simpleName);
                    }
                }

            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        //3、实例化、
        for (String needAutoWireBeanName : needAutoWireBeanNames) {
            //被注入的bean
            BeanRelation beanRelation = allBeanMap.get(needAutoWireBeanName);
            Class<?> aClass = beanRelation.getClassType();
            Field[] declaredFields = aClass.getDeclaredFields();
            for (Field field : declaredFields) {
                //被注入的属性
                if (field.isAnnotationPresent(MyAutoWire.class)) {
                    String fieldName = field.getName();
                    //注入的bean
                    BeanRelation wireBean = allBeanMap.get(fieldName);
                    try {
                        field.setAccessible(true);
                        field.set(beanRelation.getBean(), wireBean.getBean());
                    } catch (Exception e) {
                        System.out.println("注入属性错误:" + e.toString());
                    }
                }
            }
        }
    }

    /**
     * 扫描
     */
    private void doScan() {
        try {
            //获取根路径
            String rootPath = this.getClass().getResource("/").getPath();
            System.out.println("mySpring's root path is :" + rootPath);

            //扫描根路径下需要bean初始化的类
            File root = new File(rootPath);
            String[] list = root.list();
            for (String fileName : list) {
                doScan(rootPath, fileName);
            }
            ListUtil.printList(classPath.toArray());
        } catch (Exception e) {

        }
    }

    private void doScan(String basePath, String path) {
        boolean isDirectory = new File(basePath + path).isDirectory();
        if (isDirectory) {
            File file = new File(basePath + "/" + path);
            for (String fileName : file.list()) {
                doScan(basePath, path + "/" + fileName);
            }
        } else {
            classPath.add(path);
        }
    }

    private void dispatch(HttpServletRequest request, HttpServletResponse response) {
        String originalPath = request.getRequestURI();
        String substring = originalPath.substring(1);
        String path = substring.substring(substring.indexOf("/"));
        System.out.println("realPath:" + path);
        ClassFunction classFunction = controllerPath.get(path);
        if (null == classFunction) {
            return;
        }
        Method method = classFunction.getMethod();
        BeanRelation beanRelation = allBeanMap.get(classFunction.getSimpleClassName());
        //todo 注入参数 not work
        try {
            method.invoke(beanRelation.getBean());
        } catch (Exception e) {

        }
    }

    @Test
    public void doScanTest() {
        doScan();
    }

    @Test
    public void initTest() {
        try {
            init();
        } catch (Exception e) {

        }
    }

}
