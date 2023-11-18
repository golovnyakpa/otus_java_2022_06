package ru.otus.utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class AnnotationUtils {

    public static List<Method> findAnnotatedMethods(Class<?> cls, Class<? extends Annotation> annotation) {
        Method[] clsMethods = cls.getDeclaredMethods();

        return Arrays.stream(clsMethods)
                .filter(x -> x.isAnnotationPresent(annotation))
                .collect(Collectors.toList());
    }

}
