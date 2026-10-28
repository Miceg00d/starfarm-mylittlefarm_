package ru.starfarm.mylittlefarm.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
public class ReflectionUtil {
    public static <T> T getValue(Object obj, String fieldName) {
        T result = null;
        try {
            Field field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            result = getFieldValue(obj, field);
            field.setAccessible(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static <T> T getFieldValue(Object obj, Field field) {
        T value = null;
        try {
            field.setAccessible(true);
            value = (T) field.get(obj);
            field.setAccessible(true);
        } catch (Exception e) {
            e.printStackTrace();
            return value;
        }
        return value;
    }

    public static <T> boolean setValue(Object obj, String fieldName, T value) {
        try {
            Field field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(obj, value);
            field.setAccessible(false);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static <T> boolean setValue(Object obj, Field field, T value) {
        try {
            field.setAccessible(true);
            field.set(obj, value);
            field.setAccessible(false);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static List<Field> getFields(Object obj) {
        return getFields(obj.getClass());
    }

    public static List<Field> getFields(Class<?> clazz) {
        return new ArrayList<>(Arrays.asList(clazz.getDeclaredFields()));
    }

    public static Map<Field, Object> getFieldsValues(Object obj) {
        Map<Field, Object> fields = new HashMap<>();
        try {
            for (Field field : getFields(obj)) {
                field.setAccessible(true);
                fields.put(field, getFieldValue(obj, field));
                field.setAccessible(false);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return fields;
        }
        return fields;
    }

    public static Class<?> getMethodReturn(Method method) {
        return method.getReturnType();
    }

    public static Method getMethod(Object obj, String method, Class<?>... params) {
        return getMethod(obj.getClass(), method, params);
    }

    public static Method getMethod(Class<?> clazz, String methodName, Class<?>... params) {
        Method method = null;
        try {
            method = clazz.getDeclaredMethod(methodName, params);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return method;
    }

    public static <T> T invokeMethod(Object obj, Method method, Object... params) {
        T value = null;
        try {
            if (getMethodReturn(method) == Void.class) {
                method.invoke(obj, params);
            } else {
                value = (T) method.invoke(obj, params);
            }
        } catch (IllegalAccessException | java.lang.reflect.InvocationTargetException e) {
            e.printStackTrace();
        }
        return value;
    }

    public static List<Class<?>> getClasses(Class<?> clazz) {
        return Arrays.asList(clazz.getDeclaredClasses());
    }

    public static List<Class<?>> getClasses(Object obj) {
        return getClasses(obj.getClass());
    }

    public static Field findField(Class<?> clazz, Class<?> fieldType) {
        for (Field f : getFields(clazz)) {
            if (f.getType() == fieldType)
                return f;
        }
        return null;
    }

    public static <T> FieldAccessor<T> fieldAccessor(final Field f) {
        if (f == null)
            return null;
        return new FieldAccessor<T>() {
            private final Field field = f;

            public T get(Object obj) {
                if (hasField(obj))
                    return ReflectionUtil.getFieldValue(obj, this.field);
                return null;
            }

            public void set(Object obj, Object value) {
                if (hasField(obj))
                    ReflectionUtil.setValue(obj, this.field, value);
            }

            public boolean hasField(Object obj) {
                return Arrays.asList(obj.getClass().getDeclaredFields()).contains(this.field);
            }
        };
    }

    public interface FieldAccessor<T> {
        T get(Object param1Object);

        void set(Object param1Object1, Object param1Object2);

        boolean hasField(Object param1Object);
    }
}