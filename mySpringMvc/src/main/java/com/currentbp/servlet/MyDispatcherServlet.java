package com.currentbp.servlet;


import com.alibaba.fastjson.JSON;
import com.currentbp.annotation.*;
import com.currentbp.entity.BeanRelation;
import com.currentbp.entity.ClassFunction;
import com.currentbp.entity.MethodAndType;
import com.currentbp.util.all.Assert;
import com.currentbp.util.all.ListUtil;
import com.currentbp.util.all.StringUtil;
import jdk.internal.org.objectweb.asm.*;
import jdk.internal.org.objectweb.asm.Type;
import org.apache.commons.collections.CollectionUtils;
import org.junit.Test;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Collectors;

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
        dispatch(req, resp, "get");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        dispatch(req, resp, "post");
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
                    classFunction.setParams(getMethodAndType(beanRelation.getClassType(), method));
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
            System.out.println(e);
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

    private void dispatch(HttpServletRequest request, HttpServletResponse response, String type) {
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
        List<MethodAndType> methodAndTypes = classFunction.getParams();
        Object bean = beanRelation.getBean();
        List<Object> paramValues = new ArrayList<>();
        if (CollectionUtils.isEmpty(methodAndTypes)) {

        } else if ("get".equals(type)) {
            List<String> paramNames = methodAndTypes.stream().map(MethodAndType::getName).collect(Collectors.toList());
            Map<String, MethodAndType> paramMap = methodAndTypes.stream().collect(Collectors.toMap(MethodAndType::getName, e -> e));

            for (String paramName : paramNames) {
                String paramValue = request.getParameter(paramName);
                Object object = JSON.parseObject(paramValue, paramMap.get(paramName).getMethodType());
                paramValues.add(object);
            }
        } else if ("post".equals(type)) {
            //post请求时，注入参数只能是一个对象
            List<String> paramNames = methodAndTypes.stream().map(MethodAndType::getName).collect(Collectors.toList());
            Map<String, MethodAndType> paramMap = methodAndTypes.stream().collect(Collectors.toMap(MethodAndType::getName, e -> e));
            String requestBody = getRequestBody(request);
            Object object = JSON.parseObject(requestBody, paramMap.get(paramNames.get(0)).getMethodType());
            paramValues.add(object);
        }
        invokeMethod(method, bean, paramValues, response);
    }

    private void invokeMethod(Method method, Object bean, List<Object> paramValues, HttpServletResponse response) {
        try {
            if (CollectionUtils.isEmpty(paramValues)) {
                method.invoke(bean);
            } else {
                method.invoke(bean, paramValues.toArray());
            }
        } catch (Exception e) {
            System.out.println("invoke is error:" + e.getMessage() + "  st:" + e.getStackTrace());
        }
    }


    private List<MethodAndType> getMethodAndType(Class<?> clazz, final Method method) {
        List<MethodAndType> result = new ArrayList<>();
        final Class<?>[] parameterTypes = method.getParameterTypes();
        String[] methodParameterNamesByAsm4 = getMethodParameterNamesByAsm4(clazz, method);
        if (null == methodParameterNamesByAsm4 || 0 == methodParameterNamesByAsm4.length) {
            return null;
        }
        for (int i = 0; i < methodParameterNamesByAsm4.length; i++) {
            MethodAndType methodAndType = new MethodAndType(methodParameterNamesByAsm4[i], parameterTypes[i]);
            result.add(methodAndType);
        }
        return result;
    }

    private String[] getMethodParameterNamesByAsm4(Class<?> clazz, final Method method) {
        final Class<?>[] parameterTypes = method.getParameterTypes();
        if (parameterTypes == null || parameterTypes.length == 0) {
            return null;
        }
        final Type[] types = new Type[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; i++) {
            types[i] = Type.getType(parameterTypes[i]);
        }
        final String[] parameterNames = new String[parameterTypes.length];

        String className = clazz.getName();
        int lastDotIndex = className.lastIndexOf(".");
        className = className.substring(lastDotIndex + 1) + ".class";
        InputStream is = clazz.getResourceAsStream(className);
        try {
            ClassReader classReader = new ClassReader(is);
            classReader.accept(new ClassVisitor(Opcodes.ASM4) {
                @Override
                public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
                    // 只处理指定的方法
                    Type[] argumentTypes = Type.getArgumentTypes(desc);
                    if (!method.getName().equals(name) || !Arrays.equals(argumentTypes, types)) {
                        return null;
                    }
                    return new MethodVisitor(Opcodes.ASM4) {
                        @Override
                        public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index) {
                            // 静态方法第一个参数就是方法的参数，如果是实例方法，第一个参数是this
                            if (Modifier.isStatic(method.getModifiers())) {
                                parameterNames[index] = name;
                            } else if (index > 0) {
                                parameterNames[index - 1] = name;
                            }
                        }
                    };

                }
            }, 0);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return parameterNames;
    }


    private String getRequestBody(HttpServletRequest request) {
        InputStream is = null;
        String result = null;
        try {
            is = request.getInputStream();
            StringBuilder sb = new StringBuilder();
            byte[] b = new byte[4096];
            for (int n; (n = is.read(b)) != -1; ) {
                sb.append(new String(b, 0, n));
            }
            result = sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != is) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
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
