package ru.otus.jdbc.mapper;

import ru.otus.core.repository.DataTemplate;
import ru.otus.core.repository.executor.DbExecutor;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DataTemplateJdbc<T> implements DataTemplate<T> {

    private final DbExecutor dbExecutor;
    private final EntitySQLMetaData entitySQLMetaData;
    private final EntityClassMetaData<T> entityClassMetaData;

    public DataTemplateJdbc(
            DbExecutor dbExecutor,
            EntitySQLMetaData entitySQLMetaData,
            EntityClassMetaData<T> entityClassMetaData) {
        this.dbExecutor = dbExecutor;
        this.entitySQLMetaData = entitySQLMetaData;
        this.entityClassMetaData = entityClassMetaData;
    }

    @Override
    public Optional<T> findById(Connection connection, long id) {
        String selectByIdSql = entitySQLMetaData.getSelectByIdSql();
        return dbExecutor
                .executeSelect(connection, selectByIdSql, List.of(id), this::mapRowList)
                .map(x -> {
                    if (x.isEmpty()) {
                        return null;
                    } else {
                        return x.get(0);
                    }
                });
    }

    @Override
    public List<T> findAll(Connection connection) {
        String selectAllSql = entitySQLMetaData.getSelectAllSql();
        return dbExecutor
                .executeSelect(connection, selectAllSql, List.of(), this::mapRowList)
                .orElse(List.of());
    }

    @Override
    public long insert(Connection connection, T object) {
        String insertSql = entitySQLMetaData.getInsertSql();
        List<Object> params = extractFieldValues(object);
        return dbExecutor.executeStatement(connection, insertSql, params);
    }

    @Override
    public void update(Connection connection, T object) {
        String updateSql = entitySQLMetaData.getUpdateSql();
        List<Object> params = extractFieldValues(object);
        params.add(entityClassMetaData.getIdField().getName());
        dbExecutor.executeStatement(connection, updateSql, params);
    }

    private T mapRow(ResultSet resultSet) {
        try {
            Constructor<T> constructor = entityClassMetaData.getConstructor();
            T instance = constructor.newInstance();

            List<Field> fields = entityClassMetaData.getAllFields();
            for (Field field : fields) {
                field.setAccessible(true);
                field.set(instance, resultSet.getObject(field.getName()));
            }

            return instance;
        } catch (Exception e) {
            throw new RuntimeException(
                    "Error mapping sql row to class " + entityClassMetaData.getName(), e
            );
        }
    }

    private List<T> mapRowList(ResultSet resultSet) {
        List<T> entities = new ArrayList<>();
        try {
            while (resultSet.next()) {
                T entity = mapRow(resultSet);
                entities.add(entity);
            }
            return entities;
        } catch (Exception e) {
            throw new RuntimeException(
                    "Error mapping sql row to class " + entityClassMetaData.getName(), e
            );
        }
    }

    private List<Object> extractFieldValues(T entity) {
        List<Object> values = new ArrayList<>();
        try {
            List<Field> fields = entityClassMetaData.getFieldsWithoutId();
            for (Field field : fields) {
                field.setAccessible(true);
                values.add(field.get(entity));
            }
            return values;
        } catch (Exception e) {
            throw new RuntimeException(
                    "Error mapping sql row to class " + entityClassMetaData.getName(), e
            );
        }
    }

}
