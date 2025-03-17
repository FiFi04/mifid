package pl.rg.main;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
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

public class AppContainer {

  private static Logger logger = LoggerImpl.getInstance();

  private static Map<String, Object> container;

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
    for (Class<?> annotatedClass : annotatedClasses) {
      Class<?>[] interfaces = getInterfaces(annotatedClass);
      List<Class<?>[]> implementations = getImplementations(interfaces);
      for (Class<?>[] implementationClasses : implementations) {
        for (Class<?> aClass : implementationClasses) {
//          String annotationName = getAnnotationName(aClass);
//          if (annotationName.isBlank() && implementationClasses.length > 1
//              && !pl.rg.utils.repository.Repository.class.isAssignableFrom(annotatedClass)) {
//            throw new ValidationException(
//                "Interfejsy posiadające więcej niż 1 implementację powinny posiadać nazwy tych implementacji: "
//                    + aClass);
//          }
          if (interfaces.length > 0) {
            addToContainer(container, annotatedClass, interfaces[0]);
          }
          if (!aClass.getSimpleName().equals(annotatedClass.getSimpleName())) {
            addToContainer(container, annotatedClass, null);
          }
        }
      }
    }
  }
// todo Adnotacje nad klasami service, controller, repository nie są brane pod uwagę w tej chwili
//  private static String getAnnotationName(Class<?> aClass) {
//    Annotation[] annotations = aClass.getAnnotations();
//    for (Annotation annotation : annotations) {
//      if (annotation.annotationType().equals(Controller.class)) {
//        return ((Controller) annotation).name();
//      } else if (annotation.annotationType().equals(Service.class)) {
//        return ((Service) annotation).name();
//      } else if (annotation.annotationType().equals(Repository.class)) {
//        return ((Repository) annotation).name();
//      }
//    }
//    return "";
//  }

  private static List<Class<?>[]> getImplementations(Class<?>[] interfaces) {
    Set<Class<?>> allClasses = getAllAnnotatedClasses();
    return Arrays.stream(interfaces)
        .map(aInterface -> allClasses.stream()
            .filter(aInterface::isAssignableFrom)
            .toArray(Class<?>[]::new))
        .collect(Collectors.toList());
  }

  private static Class<?>[] getInterfaces(Class<?> annotatedClass) {
    Class<?>[] implementedInterfaces = annotatedClass.getInterfaces();
    Class<?> superclass = annotatedClass.getSuperclass();
    List<Class<?>> allInterfaces = new ArrayList<>();
    if (implementedInterfaces.length > 0) {
      return implementedInterfaces;
    } else if (Modifier.isAbstract(superclass.getModifiers())) {
      allInterfaces.addAll(List.of(getInterfaces(superclass)));
    }
    return allInterfaces.toArray(new Class<?>[0]);
  }

  private static void addToContainer(Map<String, Object> container, Class<?> annotatedClass,
      Class<?> implementedInterface) {
    try {
      String className = Optional.ofNullable(implementedInterface)
          .map(Class::getSimpleName)
          .orElse(annotatedClass.getSimpleName());
      String lowerCase =
          Character.toLowerCase(className.charAt(0)) + className.substring(1);
      Constructor<?> constructor = annotatedClass.getConstructor();
      Object instance = constructor.newInstance();
      container.put(lowerCase, instance);
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

  private static void initializeFields(Map<String, Object> container) {
    Object currentValue = null;
    try {
      for (Object classInstance : container.values()) {
        Class<?> currentClass = classInstance.getClass();
        while (currentClass != null && currentClass != Object.class) {
          List<Field> fields = Arrays.stream(currentClass.getDeclaredFields())
              .filter(field -> field.isAnnotationPresent(Autowire.class))
              .toList();
          for (Field field : fields) {
            String autowireName = field.getAnnotation(Autowire.class).name();
            for (Entry<String, Object> nextObject : container.entrySet()) {
              field.setAccessible(true);
              String key = nextObject.getKey();
              currentValue = nextObject.getValue();
              if (autowireName.equals(key)) {
                Method instanceMethod = currentValue.getClass()
                    .getMethod(EmailModuleFactory.INSTANCE_METHOD_NAME);
                Object createdInstance = instanceMethod.invoke(currentValue);
                field.set(classInstance, createdInstance);
                break;
              }
              if (field.getType().isAssignableFrom(currentValue.getClass())
                  && autowireName.isBlank()) {
                field.set(classInstance, currentValue);
                break;
              }
            }
          }
          currentClass = currentClass.getSuperclass();
        }
      }
    } catch (IllegalAccessException e) {
      throw logger.logAndThrowRepositoryException(LogLevel.DEBUG,
          new RepositoryException("Brak dostepu do metody"));
    } catch (InvocationTargetException e) {
      throw logger.logAndThrowRepositoryException(LogLevel.DEBUG,
          new RepositoryException(INVOCATION_EXCEPTION + currentValue));
    } catch (NoSuchMethodException e) {
      throw logger.logAndThrowRepositoryException(LogLevel.DEBUG,
          new RepositoryException(NO_SUCH_METHOD));
    }
  }
}