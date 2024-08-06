package pl.rg.repository;

import lombok.Data;
import pl.rg.annotation.FieldCategory;
import pl.rg.logger.Logger;

import java.lang.reflect.*;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Data
public abstract class MifidRepository<T extends MifidGeneral<E>, E> implements Repository<T, E> {

    private T t;

    private E lastSavedObjectId;

    private boolean rollback = false;

    private int innerLevel = 0;

//    TODO MIFID 5
//    @Autowire
    protected Logger logger;

    @Override
    public List<T> findAll() {
        String query = "SELECT * FROM %s";
        List<T> bankAccObjects = new ArrayList<>();
        try (Statement statement = dbConnector.getConnection().createStatement()) {
            Class<T> tClass = getTypeClass();
            Field[] allFields = getClassFields(tClass);
            Field[] objectFields = getObjectFields(allFields);
            Field[] columnsNames = getColumnsNames(allFields);
            t = createInstance(tClass);
            String completeQuery = String.format(query, t.getTableName());
            logger.log(completeQuery);
            ResultSet resultSet = statement.executeQuery(completeQuery);
            while (resultSet.next()) {
                T bankAccObject = getObjectFromDB(tClass, objectFields, columnsNames, resultSet, false);
                bankAccObjects.add(bankAccObject);
            }
        } catch (NoSuchMethodException e) {
            logAndThrowException("Brak metody o podanej sygnaturze: ", e);
        } catch (InvocationTargetException e) {
            logAndThrowException("Wystąpił wyjątek podczas wykonywania metody: ", e);
        } catch (InstantiationException e) {
            logAndThrowException("Nie można utworzyć obiektu: ", e);
        } catch (IllegalAccessException e) {
            logAndThrowException("Brak dostepu do metody: ", e);
        } catch (ClassNotFoundException e) {
            logAndThrowException("Brak klasy o podanej nazwie: ", e);
        } catch (SQLException e) {
            logAndThrowException("Błąd odczytu z bazy danych: ", e);
        } catch (Throwable e) {
            logAndThrowException("Wystąpił niespodziewany błąd: ", e);
        }
        return bankAccObjects;
    }

    @Override
    public Optional<T> findById(E id) {
        String query = "SELECT * FROM %s WHERE id = " + id;
        try (Statement statement = dbConnector.getConnection().createStatement()) {
            Class<T> tClass = getTypeClass();
            t = createInstance(tClass);
            String completeQuery = String.format(query, t.getTableName());
            logger.log(completeQuery);
            ResultSet resultSet = statement.executeQuery(completeQuery);
            if (!resultSet.next()) {
                return Optional.empty();
            }
            Field[] allFields = getClassFields(tClass);
            Field[] objectFields = getObjectFields(allFields);
            Field[] columnsNames = getColumnsNames(allFields);
            T bankAccObject = getObjectFromDB(tClass, objectFields, columnsNames, resultSet, false);
            return Optional.of(bankAccObject);
        } catch (NoSuchMethodException e) {
            logAndThrowException("Brak metody o podanej sygnaturze: ", e);
        } catch (InvocationTargetException e) {
            logAndThrowException("Wystąpił wyjątek podczas wykonywania metody: ", e);
        } catch (InstantiationException e) {
            logAndThrowException("Nie można utworzyć obiektu: ", e);
        } catch (IllegalAccessException e) {
            logAndThrowException("Brak dostepu do metody: ", e);
        } catch (ClassNotFoundException e) {
            logAndThrowException("Brak klasy o podanej nazwie: ", e);
        } catch (SQLException e) {
            logAndThrowException("Błąd odczytu z bazy danych: ", e);
        } catch (Throwable e) {
            logAndThrowException("Wystąpił niespodziewany błąd: ", e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<T> findFetchById(E id) {
        String query = "SELECT * FROM %s WHERE id = " + id;
        try (Statement statement = dbConnector.getConnection().createStatement()) {
            Class<T> tClass = getTypeClass();
            Constructor<T> constructor = tClass.getConstructor();
            constructor.setAccessible(true);
            t = constructor.newInstance();
            String completeQuery = String.format(query, t.getTableName());
            logger.log(completeQuery);
            ResultSet resultSet = statement.executeQuery(completeQuery);
            if (!resultSet.next()) {
                return Optional.empty();
            }
            Field[] allFields = getClassFields(tClass);
            Field[] objectFields = getObjectFields(allFields);
            Field[] columnsNames = getColumnsNames(allFields);
            T bankAccObject = getObjectFromDB(tClass, objectFields, columnsNames, resultSet, true);
            return Optional.of(bankAccObject);
        } catch (NoSuchMethodException e) {
            logAndThrowException("Brak metody o podanej sygnaturze: ", e);
        } catch (InvocationTargetException e) {
            logAndThrowException("Wystąpił wyjątek podczas wykonywania metody: ", e);
        } catch (InstantiationException e) {
            logAndThrowException("Nie można utworzyć obiektu: ", e);
        } catch (IllegalAccessException e) {
            logAndThrowException("Brak dostepu do metody: ", e);
        } catch (ClassNotFoundException e) {
            logAndThrowException("Brak klasy o podanej nazwie: ", e);
        } catch (SQLException e) {
            logAndThrowException("Błąd odczytu z bazy danych: ", e);
        } catch (Throwable e) {
            logAndThrowException("Wystąpił niespodziewany błąd: ", e);
        }
        return Optional.empty();
    }

    @Override
    public void deleteAll(Iterable<T> objects) {
        String query = "DELETE FROM %s WHERE id = %d";
        Connection connection = dbConnector.getConnection();
        try (Statement statement = connection.createStatement()) {
            connection.setAutoCommit(false);
            Class<T> tClass = getTypeClass();
            t = createInstance(tClass);
            for (T object : objects) {
                E id = object.getId();
                String completeQuery = String.format(query, t.getTableName(), id);
                logger.log(completeQuery);
                statement.executeUpdate(completeQuery);
            }
            connection.commit();
            connection.setAutoCommit(true);
        } catch (InvocationTargetException e) {
            logAndThrowException("Wystąpił wyjątek podczas wykonywania metody: ", e);
        } catch (NoSuchMethodException e) {
            logAndThrowException("Brak metody o podanej sygnaturze: ", e);
        } catch (InstantiationException e) {
            logAndThrowException("Nie można utworzyć obiektu: ", e);
        } catch (IllegalAccessException e) {
            logAndThrowException("Brak dostepu do metody: ", e);
        } catch (SQLException e) {
            logAndThrowException("Błąd podczas usuwania obiektu z bazy danych: ", e);
        } catch (Throwable e) {
            logAndThrowException("Wystąpił niespodziewany błąd: ", e);
        } finally {
            try {
                if (!connection.getAutoCommit()) {
                    connection.rollback();
                    logger.log("Wycofano zmiany z bazy danych");
                }
            } catch (SQLException e) {
                logAndThrowException("Błąd podczas wycowywania zmian z bazy danych", e);
            }
        }
    }

    @Override
    public void deleteById(E id) {
        String query = "DELETE FROM %s WHERE id = " + id;
        Connection connection = dbConnector.getConnection();
        try (Statement statement = connection.createStatement()) {
            connection.setAutoCommit(false);
            Class<T> tClass = getTypeClass();
            t = createInstance(tClass);
            String completeQuery = String.format(query, t.getTableName());
            logger.log(completeQuery);
            statement.executeUpdate(completeQuery);
            connection.commit();
            connection.setAutoCommit(true);
        } catch (InvocationTargetException e) {
            logAndThrowException("Wystąpił wyjątek podczas wykonywania metody: ", e);
        } catch (NoSuchMethodException e) {
            logAndThrowException("Brak metody o podanej sygnaturze: ", e);
        } catch (InstantiationException e) {
            logAndThrowException("Nie można utworzyć obiektu: ", e);
        } catch (IllegalAccessException e) {
            logAndThrowException("Brak dostepu do metody: ", e);
        } catch (SQLException e) {
            logAndThrowException("Błąd podczas usuwania obiektu z bazy danych: ", e);
        } catch (Throwable e) {
            logAndThrowException("Wystąpił niespodziewany błąd: ", e);
        } finally {
            try {
                if (!connection.getAutoCommit()) {
                    connection.rollback();
                    logger.log("Wycofano zmiany z bazy danych");
                }
            } catch (SQLException e) {
                logAndThrowException("Błąd podczas wycowywania zmian z bazy danych", e);
            }
        }
    }

    @Override
    public void save(T object) {
        String insertQuery = "INSERT INTO %s (%s) VALUES (%s)";
        String idQuery = "SELECT id FROM %s";
        Connection connection = dbConnector.getConnection();
        try (Statement statement = connection.createStatement()) {
            connection.setAutoCommit(false);
            Class<T> aClass = (Class<T>) object.getClass();
            t = createInstance(aClass);
            String tableName = object.getTableName();
            Field[] allFields = getClassFields(aClass);
            Field[] objectFields = getObjectFields(allFields);
            Field[] columnsNames = getColumnsNames(allFields);
            String insertColumns = "";
            String insertValues = "";
            for (Field objectField : objectFields) {
                for (Field columnsName : columnsNames) {
                    if (equalFieldsNames(objectField, columnsName, object)) {
                        if (objectField.getName().equals("id")) {
                            continue;
                        }
                        String insertValue = getInsertValue(objectField, object);
                        if (insertValue == null) {
                            insertValues += insertValue + ",";
                        } else {
                            insertValues += ("'" + insertValue + "',");
                        }
                        insertColumns += (columnsName.get(object) + ",");
                        break;
                    }
                }
            }
            insertColumns = insertColumns.substring(0, insertColumns.length() - 1);
            insertValues = insertValues.substring(0, insertValues.length() - 1);
            ResultSet idSet = statement.executeQuery(String.format(idQuery, tableName));
            while (idSet.next()) {
                if (object.getId() != null && object.getId().equals(idSet.getObject(MifidGeneral.ID))) {
                    updateObject(object, insertValues, insertColumns, statement, tableName);
                    if (innerLevel == 0) {
                        connection.commit();
                        logger.log("Zapisano w bazie danych");
                        connection.setAutoCommit(true);
                    }
                    return;
                }
            }
            String completeQuery = String.format(insertQuery, tableName, insertColumns, insertValues);
            logger.log(completeQuery);
            statement.executeUpdate(completeQuery, Statement.RETURN_GENERATED_KEYS);
            ResultSet generatedKeys = statement.getGeneratedKeys();
            if (generatedKeys.next()) {
                lastSavedObjectId = (E) generatedKeys.getObject(1);
            }
            if (innerLevel == 0) {
                connection.commit();
                logger.log("Zapisano w bazie danych");
                connection.setAutoCommit(true);
            }
        } catch (NoSuchMethodException e) {
            rollback = true;
            logAndThrowException("Brak metody o podanej sygnaturze: ", e);
        } catch (InvocationTargetException e) {
            rollback = true;
            logAndThrowException("Wystąpił wyjątek podczas wykonywania metody: ", e);
        } catch (InstantiationException e) {
            rollback = true;
            logAndThrowException("Nie można utworzyć obiektu: ", e);
        } catch (IllegalAccessException e) {
            rollback = true;
            logAndThrowException("Brak dostepu do metody: ", e);
        } catch (ClassNotFoundException e) {
            rollback = true;
            logAndThrowException("Brak klasy o podanej nazwie: ", e);
        } catch (SQLException e) {
            rollback = true;
            logAndThrowException("Błąd zapisu do bazy danych: ", e);
        } catch (Throwable e) {
            rollback = true;
            logger.logAnException(e, e.getMessage());
            logAndThrowException("Wystąpił niespodziewany błąd: ", e);
        } finally {
            try {
                if (rollback && innerLevel == 0) {
                    innerLevel = 0;
                    rollback = false;
                    connection.rollback();
                    connection.setAutoCommit(true);
                    logger.log("Wycofano zmiany z bazy danych");
                }
            } catch (SQLException e) {
                logAndThrowException("Błąd podczas wycowywania zmian z bazy danych:", e);
            }
            innerLevel--;
        }
    }

    @Override
    public T saveAndFlush(T object) {
        save(object);
        return findById(lastSavedObjectId).get();
    }

    //    TODO MIFID-4
    protected void logAndThrowException(String message, Throwable exception) {
//        RepositoryException repositoryException = new RepositoryException(message + exception.getMessage(), exception);
//        logger.logAnException(repositoryException, repositoryException.getMessage());
//        throw repositoryException;
    }

    private String getInsertValue(Field objectField, T object) throws IllegalAccessException, ClassNotFoundException,
            NoSuchMethodException, InvocationTargetException, InstantiationException {
        String className = objectField.getType().getName();
        if (className.startsWith("pl.rg") && !objectField.getType().isEnum()) {
            MifidGeneral fieldObject = (MifidGeneral) objectField.get(object);
            Object id = fieldObject.getId();
            if (id != null) {
                return id.toString();
            } else {
                Class<?> newRepository = getNewRepository(objectField);
                Constructor<?> repositoryConstructor = newRepository.getConstructor();
                Method saveAndFlush = newRepository.getMethod("saveAndFlush", fieldObject.getClass().getSuperclass());
                saveAndFlush.setAccessible(true);
                MifidRepository repository = (MifidRepository) repositoryConstructor.newInstance();
                repository.innerLevel = innerLevel + 1;
                T savedObject = (T) saveAndFlush.invoke(repository, fieldObject);
                rollback = repository.rollback;
                return savedObject.getId().toString();
            }
        } else {
            return objectField.get(object).toString();
        }
    }

    private T createInstance(Class<T> tClass) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Constructor<T> constructor = tClass.getConstructor();
        constructor.setAccessible(true);
        return constructor.newInstance();
    }

    private <T extends MifidGeneral<E>> void updateObject(T object, String insertValues, String insertColumns, Statement statement, String tableName) throws SQLException {
        String updateQuery = "UPDATE %s SET %s WHERE id = %d";
        String setFields = "";
        String[] values = insertValues.split(",");
        String[] columns = insertColumns.split(",");
        for (int i = 0; i < columns.length; i++) {
            for (int j = i; j < values.length; ) {
                setFields += (columns[i] + "=" + values[j] + ",");
                break;
            }
        }
        setFields = setFields.substring(0, setFields.length() - 1);
        String completeQuery = String.format(updateQuery, tableName, setFields, object.getId());
        logger.log(completeQuery);
        statement.executeUpdate(completeQuery);
        lastSavedObjectId = object.getId();
    }

    private Field[] getObjectFields(Field[] allFields) {
        return Arrays.stream(allFields)
                .filter(field -> field.isAnnotationPresent(FieldCategory.class))
                .filter(field -> field.getAnnotation(FieldCategory.class).dbField())
                .toArray(Field[]::new);
    }

    private Field[] getColumnsNames(Field[] allFields) {
        return Arrays.stream(allFields)
                .filter(field -> field.isAnnotationPresent(FieldCategory.class))
                .filter(field -> field.getAnnotation(FieldCategory.class).dbColumn())
                .toArray(Field[]::new);
    }

    private T getObjectFromDB(Class<T> tClass, Field[] objectFields, Field[] columnsNames, ResultSet resultSet, boolean fetch)
            throws InstantiationException, IllegalAccessException, InvocationTargetException, SQLException, ClassNotFoundException, NoSuchMethodException {
        Constructor<T> constructor = tClass.getConstructor();
        constructor.setAccessible(true);
        T bankAccObject = constructor.newInstance();
        for (Field field : objectFields) {
            for (Field columnName : columnsNames) {
                if (equalFieldsNames(field, columnName, bankAccObject)) {
                    Object object = resultSet.getObject(columnName.getName());
                    if (fetch) {
                        if (field.getAnnotation(FieldCategory.class).fetchField()) {
                            Object newObject = fetchObject(field, (E) object);
                            field.set(bankAccObject, newObject);
                            break;
                        }
                    }
                    if (object == null || columnName.getName().endsWith("_ID")) {
                        continue;
                    }
                    if (field.getType().isEnum()) {
                        Class<Enum> enumType = (Class<Enum>) field.getType();
                        Enum enumValue = Enum.valueOf(enumType, object.toString());
                        field.set(bankAccObject, enumValue);
                        break;
                    }
                    if (field.getType().isAssignableFrom(LocalDate.class) & !field.getType().isAssignableFrom(Object.class)) {
                        Date date = (Date) object;
                        field.set(bankAccObject, date.toLocalDate());
                        break;
                    }
                    field.set(bankAccObject, object);
                    break;
                }
            }
        }
        return bankAccObject;
    }

    private Object fetchObject(Field field, E object) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Class<?> newRepository = getNewRepository(field);
        Constructor<?> repositoryConstructor = newRepository.getConstructor();
        Method findFetchById = newRepository.getMethod("findFetchById", Object.class);
        findFetchById.setAccessible(true);
        Optional newObjectOptional = (Optional) findFetchById.invoke(repositoryConstructor.newInstance(), object);
        return Optional.of(newObjectOptional).orElse(null).get();
    }

    private Class<?> getNewRepository(Field field) throws ClassNotFoundException {
        String[] simpleClassName = field.getType().getSimpleName().split("(?=\\p{Lu})");
        String model = Arrays.stream(simpleClassName)
                .filter(text -> !text.matches("Model"))
                .collect(Collectors.joining(""));
        String repositoryName = model + "Repository";
        String[] pathArray = field.getType().getPackageName().split("\\.");
        String packageName = Arrays.stream(pathArray)
                .limit(pathArray.length - 1)
                .collect(Collectors.joining("."));
        return Class.forName(packageName + "." + repositoryName);
    }

    private Field[] getClassFields(Class<T> tClass) {
        Field[] declaredFields = tClass.getDeclaredFields();
        Field[] superClassFields = new Field[0];
        if (tClass.getSuperclass() != null) {
            superClassFields = getClassFields((Class<T>) tClass.getSuperclass());
        }
        return Stream
                .concat(Arrays.stream(declaredFields), Arrays.stream(superClassFields))
                .peek(field -> field.setAccessible(true))
                .toArray(Field[]::new);
    }

    private Class<T> getTypeClass() {
        Type superclass = getClass().getGenericSuperclass();
        ParameterizedType parameterizedType = (ParameterizedType) superclass;
        Type[] typeArguments = parameterizedType.getActualTypeArguments();
        return (Class<T>) typeArguments[0];
    }

    private boolean equalFieldsNames(Field classField, Field columnField, T classObject) throws IllegalAccessException {
        String classFieldName = classField.getName();
        String columnFieldName = (String) columnField.get(classObject);
        if (classFieldName.equals(columnFieldName)) {
            return true;
        }
        String[] splitName = columnFieldName.split("_");
        for (int i = 0; i < splitName.length; i++) {
            if (i > 0) {
                String upperCase = splitName[i].substring(0, 1).toUpperCase() + splitName[i].substring(1);
                splitName[i] = upperCase;
            }
        }
        String fieldMatchingName = Arrays.stream(splitName)
                .filter(text -> !text.equals("Id"))
                .collect(Collectors.joining(""));
        return classFieldName.equals(fieldMatchingName);
    }
}