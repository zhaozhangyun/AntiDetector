package com.z.zz.zzz.antidetector.fakecamera;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class Reflection {

    public static Class forName(String className) throws ClassNotFoundException {
        return Class.forName(className);
    }

    public static Object createDefaultInstance(String className) throws Exception {
        Class classObject = forName(className);
        return createDefaultInstance(classObject);
    }

    public static Object createDefaultInstance(Class classObject) throws Exception {
        return classObject.newInstance();
    }

    public static Object createInstance(String className, Class[] cArgs, Object... args) throws
            Exception {
        Class classObject = Class.forName(className);
        Constructor constructor = classObject.getConstructor(cArgs);
        return constructor.newInstance(args);
    }

    public static Object invokeStaticMethod(String className, String methodName, Class[] cArgs,
                                            Object... args) throws Exception {
        Class classObject = Class.forName(className);
        return invokeMethod(classObject, methodName, null, cArgs, args);
    }

    public static Object invokeInstanceMethod(Object instance, String methodName, Class[] cArgs,
                                              Object... args) throws Exception {
        Class classObject = instance.getClass();
        return invokeMethod(classObject, methodName, instance, cArgs, args);
    }

    public static Object invokeMethod(Class classObject, String methodName, Object instance,
                                      Class[] cArgs, Object... args) throws Exception {
        Method methodObject = classObject.getDeclaredMethod(methodName, cArgs);
        return methodObject.invoke(instance, args);
    }

    public static Object readField(String className, String fieldName) throws Exception {
        return readField(className, fieldName, null);
    }

    public static Object readField(String className, String fieldName, Object instance) throws
            Exception {
        Class classObject = forName(className);
        Field fieldObject = classObject.getField(fieldName);
        return fieldObject.get(instance);
    }
}
