package com.kmdc.mdu.oaid;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Reflection {

    public static Class forName(String className) throws ClassNotFoundException {
        return Class.forName(className);
    }

    public static Object createDefaultInstance(String className) throws ClassNotFoundException,
            IllegalAccessException, InstantiationException {
        Class classObject = forName(className);
        return createDefaultInstance(classObject);
    }

    public static Object createDefaultInstance(Class classObject) throws InstantiationException,
            IllegalAccessException {
        return classObject.newInstance();
    }

    public static Object createInstance(String className, Class[] cArgs, Object... args) throws
            ClassNotFoundException, NoSuchMethodException, IllegalAccessException,
            InvocationTargetException, InstantiationException {
        Class classObject = Class.forName(className);
        Constructor constructor = classObject.getConstructor(cArgs);
        return constructor.newInstance(args);
    }

    public static Object invokeStaticMethod(String className, String methodName, Class[] cArgs,
                                            Object... args) throws ClassNotFoundException,
            NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Class classObject = Class.forName(className);
        return invokeMethod(classObject, methodName, null, cArgs, args);
    }

    public static Object invokeInstanceMethod(Object instance, String methodName, Class[] cArgs,
                                              Object... args) throws NoSuchMethodException,
            IllegalAccessException, InvocationTargetException {
        Class classObject = instance.getClass();
        return invokeMethod(classObject, methodName, instance, cArgs, args);
    }

    public static Object invokeMethod(Class classObject, String methodName, Object instance,
                                      Class[] cArgs, Object... args) throws NoSuchMethodException,
            InvocationTargetException, IllegalAccessException {
        Method methodObject = classObject.getDeclaredMethod(methodName, cArgs);
        return methodObject.invoke(instance, args);
    }

    public static Object readField(String className, String fieldName) throws
            IllegalAccessException, NoSuchFieldException, ClassNotFoundException {
        return readField(className, fieldName, null);
    }

    public static Object readField(String className, String fieldName, Object instance) throws
            ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
        Class classObject = forName(className);
        Field fieldObject = classObject.getField(fieldName);
        return fieldObject.get(instance);
    }
}
