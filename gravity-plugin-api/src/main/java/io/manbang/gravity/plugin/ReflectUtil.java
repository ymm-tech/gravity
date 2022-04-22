package io.manbang.gravity.plugin;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * @author weilong.hu
 * @since 2022/01/22 14:14
 */
public class ReflectUtil {
    public static Class[] EMPTY_CLASS_ARRAY = new Class[0];

    private ReflectUtil() {
    }

    /**
     * copy from org.springframework.cglib.core.ReflectUtils#newInstance(java.lang.Class)
     */
    public static <T> T newInstance(Class type) {
        return newInstance(type, EMPTY_CLASS_ARRAY, null);
    }

    public static <T> T newInstance(Class type, Class[] parameterTypes, Object[] args) {
        return newInstance(getConstructor(type, parameterTypes), args);
    }

    public static Constructor getConstructor(Class type, Class[] parameterTypes) {
        try {
            Constructor constructor = type.getDeclaredConstructor(parameterTypes);
            if (System.getSecurityManager() != null) {
                AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
                    constructor.setAccessible(true);
                    return null;
                });
            } else {
                constructor.setAccessible(true);
            }
            return constructor;
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T newInstance(final Constructor cstruct, final Object[] args) {
        boolean flag = cstruct.isAccessible();
        try {
            if (!flag) {
                cstruct.setAccessible(true);
            }
            Object result = cstruct.newInstance(args);
            return (T) result;
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e.getTargetException());
        } finally {
            if (!flag) {
                cstruct.setAccessible(flag);
            }
        }
    }
}
