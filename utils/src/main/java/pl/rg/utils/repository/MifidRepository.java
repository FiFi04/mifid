package pl.rg.utils.repository;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Data;
import pl.rg.utils.annotation.FieldCategory;
import pl.rg.utils.exception.RepositoryException;
import pl.rg.utils.logger.Logger;
import pl.rg.utils.logger.LoggerImpl;
import pl.rg.utils.repository.filter.Filter;
import pl.rg.utils.repository.paging.Page;

@Data
public abstract class MifidRepository<T extends MifidGeneral<E>, E> implements Repository<T, E> {

  private T t;

  private E lastSavedObjectId;

  private boolean rollback = false;

  private int innerLevel = 0;

  protected Logger logger = LoggerImpl.getInstance();

  @Override
  public List<T> findAll(List<Filter> filters) {
    // todo implement
    return null;
  }

  @Override
  public MifidPage findAll(List<Filter> filters, Page page) {
    // todo implement
    return null;
  }

  @Override
  public List<T> findAll() {
    String query = "SELECT * FROM %s";
    List<T> mifidObjects = new ArrayList<>();
    try (Statement statement = getDBConnector().getConnection().createStatement()) {
      Class<T> tClass = getTypeClass();
      Field[] allFields = getClassFields(tClass);
      Field[] objectFields = getObjectFields(allFields);
      Field[] columnsNames = getColumnsNames(allFields);
      t = createInstance(tClass);
      String completeQuery = String.format(query, t.getTableName());
      logger.log(completeQuery);
      ResultSet resultSet = statement.executeQuery(completeQuery);
      while (resultSet.next()) {
        T mifidObject = getObjectFromDB(tClass, objectFields, columnsNames, resultSet, false);
        mifidObjects.add(mifidObject);
      }
    } catch (NoSuchMethodException e) {
      throw logger.logAndThrowRepositoryException(
          new RepositoryException("Brak metody o podanej sygnaturze"));
    } catch (InvocationTargetException e) {
      throw logger.logAndThrowRepositoryException(
          new RepositoryException("Wystąpił wyjątek podczas wykonywania metody"));
    } catch (InstantiationException e) {
      throw logger.logAndThrowRepositoryException(
          new RepositoryException("Nie można utworzyć obiektu"));
    } catch (IllegalAccessException e) {
      throw logger.logAndThrowRepositoryException(
          new RepositoryException("Brak dostepu do metody"));
    } catch (ClassNotFoundException e) {
      throw logger.logAndThrowRepositoryException(
          new RepositoryException("Brak klasy o podanej nazwie"));
    } catch (SQLException e) {
      throw logger.logAndThrowRepositoryException(
          new RepositoryException("Błąd odczytu z bazy danych"));
    } catch (Throwable e) {
      throw logger.logAndThrowRepositoryException(
          new RepositoryException("Wystąpił niespodziewany błąd"));
    }
    return mifidObjects;
  }

  @Override
  public Optional<T> findById(E id) {
    String query = "SELECT * FROM %s WHERE id = " + id;
    try (Statement statement = getDBConnector().getConnection().createStatement()) {
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
      T mifidObject = getObjectFromDB(tClass, objectFields, columnsNames, resultSet, false);
      return Optional.of(mifidObject);
    } catch (NoSuchMethodException e) {
      throw logger.logAndThrowRepositoryException(
          new RepositoryException("Brak metody o podanej sygnaturze"));
    } catch (InvocationTargetException e) {
      throw logger.logAndThrowRepositoryException(
          new RepositoryException("Wystąpił wyjątek podczas wykonywania metody"));
    } catch (InstantiationException e) {
      throw logger.logAndThrowRepositoryException(
          new RepositoryException("Nie można utworzyć obiektu"));
    } catch (IllegalAccessException e) {
      throw logger.logAndThrowRepositoryException(
          new RepositoryException("Brak dostepu do metody"));
    } catch (ClassNotFoundException e) {
      throw logger.logAndThrowRepositoryException(
          new RepositoryException("Brak klasy o podanej nazwie"));
    } catch (SQLException e) {
      throw logger.logAndThrowRepositoryException(
          new RepositoryException("Błąd odczytu z bazy danych"));
    } catch (Throwable e) {
      throw logger.logAndThrowRepositoryException(
          new RepositoryException("Wystąpił niespodziewany błąd"));
    }
  }

  @Override
  public Optional<T> findFetchById(E id) {
    String query = "SELECT * FROM %s WHERE id = " + id;
    try (Statement statement = getDBConnector().getConnection().createStatement()) {
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
      T mifidObject = getObjectFromDB(tClass, objectFields, columnsNames, resultSet, true);
      return Optional.of(mifidObject);
    } catch (NoSuchMethodException e) {
      throw logger.logAndThrowRepositoryException(
          new RepositoryException("Brak metody o podanej sygnaturze"));
    } catch (InvocationTargetException e) {
      throw logger.logAndThrowRepositoryException(
          new RepositoryException("Wystąpił wyjątek podczas wykonywania metody"));
    } catch (InstantiationException e) {
      throw logger.logAndThrowRepositoryException(
          new RepositoryException("Nie można utworzyć obiektu"));
    } catch (IllegalAccessException e) {
      throw logger.logAndThrowRepositoryException(
          new RepositoryException("Brak dostepu do metody"));
    } catch (ClassNotFoundException e) {
      throw logger.logAndThrowRepositoryException(
          new RepositoryException("Brak klasy o podanej nazwie"));
    } catch (SQLException e) {
      throw logger.logAndThrowRepositoryException(
          new RepositoryException("Błąd odczytu z bazy danych"));
    } catch (Throwable e) {
      throw logger.logAndThrowRepositoryException(
          new RepositoryException("Wystąpił niespodziewany błąd"));
    }
  }

  @Override
  public void deleteAll(Iterable<T> objects) {
    String query = "DELETE FROM %s WHERE id = %d";
    Connection connection = getDBConnector().getConnection();
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
      throw logger.logAndThrowRepositoryException(
          new RepositoryException("Wystąpił wyjątek podczas wykonywania metody"));
    } catch (NoSuchMethodException e) {
      throw logger.logAndThrowRepositoryException(
          new RepositoryException("Brak metody o podanej sygnaturze"));
    } catch (InstantiationException e) {
      throw logger.logAndThrowRepositoryException(
          new RepositoryException("Nie można utworzyć obiektu"));
    } catch (IllegalAccessException e) {
      throw logger.logAndThrowRepositoryException(
          new RepositoryException("Brak dostepu do metody"));
    } catch (SQLException e) {
      throw logger.logAndThrowRepositoryException(
          new RepositoryException("Błąd podczas usuwania obiektu z bazy danych"));
    } catch (Throwable e) {
      throw logger.logAndThrowRepositoryException(
          new RepositoryException("Wystąpił niespodziewany błąd"));
    } finally {
      try {
        if (!connection.getAutoCommit()) {
          connection.rollback();
          logger.log("Wycofano zmiany z bazy danych");
        }
      } catch (SQLException e) {
        throw logger.logAndThrowRepositoryException(
            new RepositoryException("Błąd podczas wycowywania zmian z bazy danych"));
      }
    }
  }

  @Override
  public void deleteById(E id) {
    String query = "DELETE FROM %s WHERE id = " + id;
    Connection connection = getDBConnector().getConnection();
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
      throw logger.logAndThrowRepositoryException(
          new RepositoryException("Wystąpił wyjątek podczas wykonywania metody"));
    } catch (NoSuchMethodException e) {
      throw logger.logAndThrowRepositoryException(
          new RepositoryException("Brak metody o podanej sygnaturze"));
    } catch (InstantiationException e) {
      throw logger.logAndThrowRepositoryException(
          new RepositoryException("Nie można utworzyć obiektu"));
    } catch (IllegalAccessException e) {
      throw logger.logAndThrowRepositoryException(
          new RepositoryException("Brak dostepu do metody"));
    } catch (SQLException e) {
      throw logger.logAndThrowRepositoryException(
          new RepositoryException("Błąd podczas usuwania obiektu z bazy danych"));
    } catch (Throwable e) {
      throw logger.logAndThrowRepositoryException(
          new RepositoryException("Wystąpił niespodziewany błąd"));
    } finally {
      try {
        if (!connection.getAutoCommit()) {
          connection.rollback();
          logger.log("Wycofano zmiany z bazy danych");
        }
      } catch (SQLException e) {
        throw logger.logAndThrowRepositoryException(
            new RepositoryException("Błąd podczas wycowywania zmian z bazy danych"));
      }
    }
  }

  @Override
  public void save(T object) {
    String insertQuery = "INSERT INTO %s (%s) VALUES (%s)";
    String idQuery = "SELECT id FROM %s";
    Connection connection = getDBConnector().getConnection();
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
      throw logger.logAndThrowRepositoryException(
          new RepositoryException("Brak metody o podanej sygnaturze"));
    } catch (InvocationTargetException e) {
      rollback = true;
      throw logger.logAndThrowRepositoryException(
          new RepositoryException("Wystąpił wyjątek podczas wykonywania metody"));
    } catch (InstantiationException e) {
      rollback = true;
      throw logger.logAndThrowRepositoryException(
          new RepositoryException("Nie można utworzyć obiektu"));
    } catch (IllegalAccessException e) {
      rollback = true;
      throw logger.logAndThrowRepositoryException(
          new RepositoryException("Brak dostepu do metody"));
    } catch (ClassNotFoundException e) {
      rollback = true;
      throw logger.logAndThrowRepositoryException(
          new RepositoryException("Brak klasy o podanej nazwie"));
    } catch (SQLException e) {
      rollback = true;
      throw logger.logAndThrowRepositoryException(
          new RepositoryException("Błąd zapisu do bazy danych"));
    } catch (Throwable e) {
      rollback = true;
      throw logger.logAndThrowRepositoryException(
          new RepositoryException("Wystąpił niespodziewany błąd"));
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
        throw logger.logAndThrowRepositoryException(
            new RepositoryException("Błąd podczas wycowywania zmian z bazy danych"));
      }
      innerLevel--;
    }
  }

  @Override
  public T saveAndFlush(T object) {
    save(object);
    return findById(lastSavedObjectId).get();
  }

  private String getInsertValue(Field objectField, T object)
      throws IllegalAccessException, ClassNotFoundException,
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
        Method saveAndFlush = newRepository.getMethod("saveAndFlush",
            fieldObject.getClass().getSuperclass());
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

  private T createInstance(Class<T> tClass)
      throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
    Constructor<T> constructor = tClass.getConstructor();
    constructor.setAccessible(true);
    return constructor.newInstance();
  }

  private <T extends MifidGeneral<E>> void updateObject(T object, String insertValues,
      String insertColumns, Statement statement, String tableName) throws SQLException {
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

  private T getObjectFromDB(Class<T> tClass, Field[] objectFields, Field[] columnsNames,
      ResultSet resultSet, boolean fetch)
      throws InstantiationException, IllegalAccessException, InvocationTargetException, SQLException, ClassNotFoundException, NoSuchMethodException {
    Constructor<T> constructor = tClass.getConstructor();
    constructor.setAccessible(true);
    T mifidObject = constructor.newInstance();
    for (Field field : objectFields) {
      for (Field columnName : columnsNames) {
        if (equalFieldsNames(field, columnName, mifidObject)) {
          Object object = resultSet.getObject(columnName.getName());
          if (fetch) {
            if (field.getAnnotation(FieldCategory.class).fetchField()) {
              Object newObject = fetchObject(field, (E) object);
              field.set(mifidObject, newObject);
              break;
            }
          }
          if (object == null || columnName.getName().endsWith("_ID")) {
            continue;
          }
          if (field.getType().isEnum()) {
            Class<Enum> enumType = (Class<Enum>) field.getType();
            Enum enumValue = Enum.valueOf(enumType, object.toString());
            field.set(mifidObject, enumValue);
            break;
          }
          if (field.getType().isAssignableFrom(LocalDate.class) & !field.getType()
              .isAssignableFrom(Object.class)) {
            Date date = (Date) object;
            field.set(mifidObject, date.toLocalDate());
            break;
          }
          field.set(mifidObject, object);
          break;
        }
      }
    }
    return mifidObject;
  }

  private Object fetchObject(Field field, E object)
      throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
    Class<?> newRepository = getNewRepository(field);
    Constructor<?> repositoryConstructor = newRepository.getConstructor();
    Method findFetchById = newRepository.getMethod("findFetchById", Object.class);
    findFetchById.setAccessible(true);
    Optional newObjectOptional = (Optional) findFetchById.invoke(
        repositoryConstructor.newInstance(), object);
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

  private boolean equalFieldsNames(Field classField, Field columnField, T classObject)
      throws IllegalAccessException {
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