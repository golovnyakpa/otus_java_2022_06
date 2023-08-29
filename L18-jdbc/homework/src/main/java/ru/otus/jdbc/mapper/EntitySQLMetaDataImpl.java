package ru.otus.jdbc.mapper;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class EntitySQLMetaDataImpl<T> implements EntitySQLMetaData {

    EntityClassMetaData<T> entityClassMetaData;

    public EntitySQLMetaDataImpl(EntityClassMetaData<T> entityClassMetaData) {
        this.entityClassMetaData = entityClassMetaData;
    }

    @Override
    public String getSelectAllSql() {
        String tableName = entityClassMetaData.getName();
        return String.format("SELECT * FROM %s", tableName);
    }

    @Override
    public String getSelectByIdSql() {
        String tableName = entityClassMetaData.getName();
        String idColumnName = entityClassMetaData.getIdField().getName();
        return String.format("SELECT * FROM %s WHERE %s = ?", tableName, idColumnName);
    }

    @Override
    public String getInsertSql() {
        String tableName = entityClassMetaData.getName();
        List<Field> fieldsWithoutId = entityClassMetaData.getFieldsWithoutId();
        String columnNames = fieldsWithoutId.stream()
                .map(Field::getName)
                .collect(Collectors.joining(", "));
        String placeholders = String.join(", ", Collections.nCopies(fieldsWithoutId.size(), "?"));
        return String.format("INSERT INTO %s (%s) VALUES (%s)", tableName, columnNames, placeholders);
    }

    @Override
    public String getUpdateSql() {
        String tableName = entityClassMetaData.getName();
        String idColumnName = entityClassMetaData.getIdField().getName();
        List<Field> fieldsWithoutId = entityClassMetaData.getFieldsWithoutId();
        String columnNames = fieldsWithoutId.stream()
                .map(field -> field.getName() + " = ?")
                .collect(Collectors.joining(", "));
        return String.format("UPDATE %s SET %s WHERE %s = ?", tableName, columnNames, idColumnName);
    }
}
