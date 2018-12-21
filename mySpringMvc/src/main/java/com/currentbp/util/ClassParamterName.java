package com.currentbp.util;

import com.currentbp.javassist.ParamterNameTestClass;
import com.currentbp.servlet.MyDispatcherServlet;
import com.currentbp.util.all.ListUtil;
import javassist.*;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.LocalVariableAttribute;
import javassist.bytecode.MethodInfo;
import org.junit.Test;

import java.lang.reflect.Method;

/**
 * @author baopan
 * @createTime 20181220
 */
public class ClassParamterName {

    @Test
    public void getClassParamterName() {
        Method[] methods = MyDispatcherServlet.class.getDeclaredMethods();
        for (Method method : methods) {
            String[] names = getParameterName(MyDispatcherServlet.class, method.getName());
//            ListUtil.printList(names);
        }
    }

    public static String[] getParameterName(Class clazz,String method){
        try {
            ClassPool pool = new ClassPool(true);
            CtClass cc = pool.get(clazz.getName());
            CtMethod cm = cc.getDeclaredMethod(method);
            MethodInfo methodInfo = cm.getMethodInfo();
            CodeAttribute codeAttribute = methodInfo.getCodeAttribute();
            LocalVariableAttribute attr = (LocalVariableAttribute) codeAttribute.getAttribute(LocalVariableAttribute.tag);
            if (attr != null) {
                String[] paramNames = new String[cm.getParameterTypes().length];
                int pos = Modifier.isStatic(cm.getModifiers()) ? 0 : 1;
                for (int i = 0; i < paramNames.length; i++) {
                    paramNames[i] = attr.variableName(i + pos);
                }
                System.out.print("method:"+method+" params:");
                for (String paramName : paramNames) {
                    System.out.print(" "+paramName+", ");
                }
                System.out.println();
                return paramNames;
            }
        } catch (NotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}
