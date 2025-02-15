package pl.rg.main;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import pl.rg.utils.annotation.Autowire;
import pl.rg.utils.annotation.Controller;
import pl.rg.utils.annotation.Repository;
import pl.rg.utils.annotation.Service;
import pl.rg.utils.exception.RepositoryException;
import pl.rg.utils.logger.LogLevel;
import pl.rg.utils.logger.Logger;
import pl.rg.utils.logger.LoggerImpl;

public class AppContainer {

  static Logger logger = LoggerImpl.getInstance();

  static Map<String, Object> container;

  static {
    container = getContainer();
  }

  public static Map<String, Object> getContainer() {
    if (container == null || container.isEmpty()) {
      container = new HashMap<>();
      Set<Class<?>> annotatedClasses = getAllAnnotatedClasses();
      initializeContainer(annotatedClasses, container);
      initializeFields(container);
    }

    return container;
  }

  private static Set<Class<?>> getAllAnnotatedClasses() {
    Reflections reflections = new Reflections(new ConfigurationBuilder()
        .setUrls(ClasspathHelper.forJavaClassPath())
        .setScanners(Scanners.TypesAnnotated));
    Set<Class<?>> repositories = reflections.getTypesAnnotatedWith(Repository.class);
    Set<Class<?>> services = reflections.getTypesAnnotatedWith(Service.class);
    Set<Class<?>> controllers = reflections.getTypesAnnotatedWith(Controller.class);
    return Stream.of(repositories, services, controllers).flatMap(Set::stream)
        .collect(Collectors.toSet());
  }

  private static void initializeContainer(Set<Class<?>> annotatedClasses,
      Map<String, Object> container) {
    Class<?> aClass = null;
    try {
      for (Class<?> annotatedClass : annotatedClasses) {
        aClass = annotatedClass;
        String className = annotatedClass.getSimpleName();
        Class<?>[] implementedInterfaces = annotatedClass.getInterfaces();
        if (implementedInterfaces.length > 0) {
          className = implementedInterfaces[0].getSimpleName();
        }
        String lowerCase = Character.toLowerCase(className.charAt(0)) + className.substring(1);
        Constructor<?> constructor = annotatedClass.getConstructor();
        Object instance = constructor.newInstance();
        container.put(lowerCase, instance);
      }
    } catch (InvocationTargetException e) {
      throw logger.logAndThrowRepositoryException(LogLevel.DEBUG,
          new RepositoryException("Błąd wywołania metody: " + aClass));
    } catch (NoSuchMethodException e) {
      throw logger.logAndThrowRepositoryException(LogLevel.DEBUG,
          new RepositoryException("Brak metody o podanej sygnaturze"));
    } catch (InstantiationException e) {
      throw logger.logAndThrowRepositoryException(LogLevel.DEBUG,
          new RepositoryException("Nie można utworzyć obiektu"));
    } catch (IllegalAccessException e) {
      throw logger.logAndThrowRepositoryException(LogLevel.DEBUG,
          new RepositoryException("Brak dostepu do metody"));
    }
  }

  private static void initializeFields(Map<String, Object> container) {
    try {
      for (Object classInstance : container.values()) {
        List<Field> fields = Arrays.stream(classInstance.getClass().getDeclaredFields())
            .filter(field -> field.isAnnotationPresent(Autowire.class))
            .toList();
        for (Field field : fields) {
          Iterator<Entry<String, Object>> iterator = container.entrySet().iterator();
          while (iterator.hasNext()) {
            field.setAccessible(true);
            Entry<String, Object> next = iterator.next();
            String currentKey = next.getKey();
            Object currentValue = next.getValue();
            if (field.getType().isAssignableFrom(currentValue.getClass())) {
              field.set(classInstance, container.get(currentKey));
            }
          }
        }
      }
    } catch (IllegalAccessException e) {
      throw logger.logAndThrowRepositoryException(LogLevel.DEBUG,
          new RepositoryException("Brak dostepu do metody"));
    }
  }
}