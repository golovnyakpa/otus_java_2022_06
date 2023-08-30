package ru.otus.jdbc.mapper;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EntityClassMetaDataImpl<T> implements EntityClassMetaData<T> {

    private final Class<T> entityClass;

    public EntityClassMetaDataImpl(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    @Override
    public String getName() {
        return entityClass.getSimpleName().toLowerCase();
    }

    @Override
    public Constructor<T> getConstructor() {
        try {
            return entityClass.getDeclaredConstructor();
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("No default constructor found for " + entityClass.getSimpleName());
        }
    }

    @Override
    public Field getIdField() {
        List<Field> idFields = Arrays.stream(entityClass.getDeclaredFields())
                .filter(f -> f.isAnnotationPresent(Id.class))
                .toList();
        if (idFields.size() > 1) {
            throw new RuntimeException("More than one field is declared as id");
        } else if (idFields.isEmpty()) {
            throw new RuntimeException("No Id field found in entity");
        } else {
            return idFields.get(0);
        }
    }

    @Override
    public List<Field> getAllFields() {
        return new ArrayList<>(Arrays.asList(entityClass.getDeclaredFields()));
    }

    @Override
    public List<Field> getFieldsWithoutId() {
        var allFields = getAllFields();
        allFields.remove(getIdField());
        return allFields;
    }
}
