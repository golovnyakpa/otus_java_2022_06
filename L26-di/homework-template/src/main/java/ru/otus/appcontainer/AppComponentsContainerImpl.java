package ru.otus.appcontainer;

import ru.otus.appcontainer.api.AppComponent;
import ru.otus.appcontainer.api.AppComponentsContainer;
import ru.otus.appcontainer.api.AppComponentsContainerConfig;
import ru.otus.utils.AnnotationUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;

public class AppComponentsContainerImpl implements AppComponentsContainer {

    private final List<Object> appComponents = new ArrayList<>();
    private final Map<String, Object> appComponentsByName = new HashMap<>();

    public AppComponentsContainerImpl(Class<?> initialConfigClass) {
        processConfig(initialConfigClass);
    }

    private void processConfig(Class<?> configClass) {
        checkConfigClass(configClass);
        try {
            Object configInstance = configClass.getConstructor().newInstance();
            List<Method> sortedAnnotatedMethods = AnnotationUtils
                    .findAnnotatedMethods(configClass, AppComponent.class).stream()
                    .sorted(Comparator.comparingInt(x -> x.getAnnotation(AppComponent.class).order()))
                    .toList();
            sortedAnnotatedMethods.forEach(x -> callConfigMethod(configInstance, x));
        } catch (Exception e) {
            throw new RuntimeException("Exception while config processing", e);
        }
    }

    private void checkConfigClass(Class<?> configClass) {
        if (!configClass.isAnnotationPresent(AppComponentsContainerConfig.class)) {
            throw new IllegalArgumentException(String.format("Given class is not config %s", configClass.getName()));
        }
    }

    @Override
    public <C> C getAppComponent(Class<C> componentClass) {
        return (C) appComponents.stream()
                .filter(x -> componentClass.isAssignableFrom(x.getClass()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException(
                                ("No component present of type %s".formatted(componentClass.getName()))
                        )
                );
    }

    @Override
    public <C> C getAppComponent(String componentName) {
        return (C) appComponentsByName.get(componentName);
    }

    private void callConfigMethod(Object instance, Method m) {
        Parameter[] params = m.getParameters();
        Object[] methodArgs = getMethodArgumentsArr(params);
        try {
            var component = m.invoke(instance, methodArgs);
            appComponents.add(component);
            appComponentsByName.put(m.getAnnotation(AppComponent.class).name(), component);
        } catch (Exception e) {
            throw new RuntimeException(
                    String.format("Exception while calling method %s in config class", m.getName()), e
            );
        }
    }

    private Object[] getMethodArgumentsArr(Parameter[] params) {
        return Arrays.stream(params).map(x -> getAppComponent(x.getType())).toArray();
    }
}
