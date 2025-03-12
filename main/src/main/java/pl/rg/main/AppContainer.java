package pl.rg.main;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
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
import pl.rg.EmailModuleFactory;
import pl.rg.utils.annotation.Autowire;
import pl.rg.utils.annotation.Controller;
import pl.rg.utils.annotation.Repository;
import pl.rg.utils.annotation.Service;
import pl.rg.utils.exception.RepositoryException;
import pl.rg.utils.logger.LogLevel;
import pl.rg.utils.logger.Logger;
import pl.rg.utils.logger.LoggerImpl;
import pl.rg.utils.repository.MifidRepository;

public class AppContainer {

  private static Logger logger = LoggerImpl.getInstance();

  private static Map<String, Object> container;

  private static Map<String, String> multipleInstances = new HashMap<>();

  private static final String ILLEGAL_ACCESS = "Brak dostepu do metody";

  private static final String NO_SUCH_METHOD = "Brak metody o podanej sygnaturze";

  private static final String INVOCATION_EXCEPTION = "Błąd wywołania metody: ";

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
    Reflections reflections = new Reflections(new ConfigurationBuilder()
        .setUrls(ClasspathHelper.forPackage("pl.rg"))
        .setScanners(Scanners.SubTypes)
    );
    for (Class<?> annotatedClass : annotatedClasses) {
      Class<?>[] inheritedInterfaces = getApiInterface(annotatedClass);
      for (Class<?> implementedInterface : inheritedInterfaces) {
        addToContainer(container, annotatedClass, reflections, implementedInterface);
      }
      Class<?>[] directInterfaces = annotatedClass.getInterfaces();
      if (directInterfaces.length > 0) {
        for (Class<?> directInterface : directInterfaces) {
          addToContainer(container, annotatedClass, reflections, directInterface);
        }
      } else {
        addToContainer(container, annotatedClass, reflections, null);
      }
    }
    sortContainer(container);
  }

  private static void addToContainer(Map<String, Object> container, Class<?> annotatedClass,
      Reflections reflections, Class<?> implementedInterface) {
    try {
      String className = (implementedInterface != null) ? implementedInterface.getSimpleName()
          : annotatedClass.getSimpleName();
      String lowerCase = Character.toLowerCase(className.charAt(0)) + className.substring(1);
      Constructor<?> constructor = annotatedClass.getConstructor();
      Object instance = constructor.newInstance();
      container.put(lowerCase, instance);
      if (className.toLowerCase().contains("api")) {
        Set<Class<?>> subTypesOf = (Set<Class<?>>) reflections.getSubTypesOf(implementedInterface);
        if (subTypesOf.size() > 1) {
          String factory = lowerCase.replace("Api", "Factory");
          multipleInstances.put(lowerCase, factory);
        }
      }
    } catch (InvocationTargetException e) {
      throw logger.logAndThrowRepositoryException(LogLevel.DEBUG,
          new RepositoryException(INVOCATION_EXCEPTION + annotatedClass));
    } catch (NoSuchMethodException e) {
      throw logger.logAndThrowRepositoryException(LogLevel.DEBUG,
          new RepositoryException(NO_SUCH_METHOD));
    } catch (InstantiationException e) {
      throw logger.logAndThrowRepositoryException(LogLevel.DEBUG,
          new RepositoryException("Nie można utworzyć obiektu"));
    } catch (IllegalAccessException e) {
      throw logger.logAndThrowRepositoryException(LogLevel.DEBUG,
          new RepositoryException(ILLEGAL_ACCESS));
    }
  }

  private static Class<?>[] getApiInterface(Class<?> annotatedClass) {
    Class<?>[] implementedInterfaces = annotatedClass.getInterfaces();
    Class<?> superclass = annotatedClass.getSuperclass();
    if (implementedInterfaces.length > 0) {
      return implementedInterfaces;
    } else if (Modifier.isAbstract(superclass.getModifiers()) && !superclass.isAssignableFrom(
        MifidRepository.class)) {
      implementedInterfaces = getApiInterface(superclass);
    }
    return implementedInterfaces;
  }

  private static void sortContainer(Map<String, Object> appContainer) {
    container = appContainer.entrySet().stream()
        .sorted(Comparator.comparing(entry -> !entry.getKey().toLowerCase().contains("factory")))
        .collect(Collectors.toMap(
            Entry::getKey,
            Entry::getValue,
            (oldValue, newValue) -> oldValue,
            LinkedHashMap::new
        ));
  }

  private static void initializeFields(Map<String, Object> container) {
    try {
      for (Object classInstance : container.values()) {
        Class<?> currentClass = classInstance.getClass();
        while (currentClass != null && currentClass != Object.class) {
          List<Field> fields = Arrays.stream(currentClass.getDeclaredFields())
              .filter(field -> field.isAnnotationPresent(Autowire.class))
              .toList();
          for (Field field : fields) {
            field.setAccessible(true);
            for (Entry<String, Object> entry : container.entrySet()) {
              Object currentValue = entry.getValue();
              if (field.getType().isAssignableFrom(currentValue.getClass())) {
                field.set(classInstance, currentValue);
                break;
              }
            }
          }
          currentClass = currentClass.getSuperclass();
        }
        if (!classInstance.toString().toLowerCase().contains("factory")) {
          updateContainerValue();
        }
      }
    } catch (IllegalAccessException e) {
      throw logger.logAndThrowRepositoryException(LogLevel.DEBUG,
          new RepositoryException(ILLEGAL_ACCESS));
    }
  }

  private static void updateContainerValue() {
    Method createMethod = null;
    for (Entry<String, String> entry : multipleInstances.entrySet()) {
      String interfaceName = entry.getKey();
      String factoryName = entry.getValue();
      Object factoryInstance = container.get(factoryName);
      try {
        createMethod = factoryInstance.getClass()
            .getMethod(EmailModuleFactory.INSTANCE_METHOD_NAME);
        Object createdInstance = createMethod.invoke(factoryInstance);
        container.put(interfaceName, createdInstance);
      } catch (NoSuchMethodException e) {
        throw logger.logAndThrowRepositoryException(LogLevel.DEBUG,
            new RepositoryException(NO_SUCH_METHOD));
      } catch (InvocationTargetException e) {
        throw logger.logAndThrowRepositoryException(LogLevel.DEBUG,
            new RepositoryException(INVOCATION_EXCEPTION + createMethod));
      } catch (IllegalAccessException e) {
        throw logger.logAndThrowRepositoryException(LogLevel.DEBUG,
            new RepositoryException(ILLEGAL_ACCESS));
      }
    }
  }
}