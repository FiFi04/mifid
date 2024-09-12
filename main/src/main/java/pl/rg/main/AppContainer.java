package pl.rg.main;

import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import pl.rg.utils.annotation.Autowire;
import pl.rg.utils.annotation.Controller;
import pl.rg.utils.annotation.Repository;
import pl.rg.utils.annotation.Service;
import pl.rg.utils.logger.Logger;
import pl.rg.utils.logger.LoggerImpl;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AppContainer {

    Logger logger = LoggerImpl.getInstance();

    public Map<String, Object> getContainer() {
        Map<String, Object> container = new HashMap<>();
        Set<Class<?>> annotatedClasses = getAllAnnotatedClasses();
        initializeContainer(annotatedClasses, container);
        initializeFields(container);
        return container;
    }

    private Set<Class<?>> getAllAnnotatedClasses() {
        Reflections reflections = new Reflections(new ConfigurationBuilder()
                .setUrls(ClasspathHelper.forJavaClassPath())
                .setScanners(Scanners.TypesAnnotated));
        Set<Class<?>> repositories = reflections.getTypesAnnotatedWith(Repository.class);
        Set<Class<?>> services = reflections.getTypesAnnotatedWith(Service.class);
        Set<Class<?>> controllers = reflections.getTypesAnnotatedWith(Controller.class);
        return Stream.of(repositories, services, controllers).flatMap(Set::stream).collect(Collectors.toSet());
    }

    private void initializeContainer(Set<Class<?>> annotatedClasses, Map<String, Object> container) {
        try {
            for (Class<?> annotatedClass : annotatedClasses) {
                String className = annotatedClass.getSimpleName().toLowerCase();
                Constructor<?> constructor = annotatedClass.getConstructor();
                Object instance = constructor.newInstance();
                container.put(className, instance);
            }
        } catch (InvocationTargetException e) {
            logger.logAndThrowException("Błąd wywołania metody: ", e);
        } catch (NoSuchMethodException e) {
            logger.logAndThrowException("Brak metody o podanej sygnaturze: ", e);
        } catch (InstantiationException e) {
            logger.logAndThrowException("Nie można utworzyć obiektu: ", e);
        } catch (IllegalAccessException e) {
            logger.logAndThrowException("Brak dostepu do metody: ", e);
        }
    }

    private void initializeFields(Map<String, Object> container) {
        try {
            for (Object classInstance : container.values()) {
                List<Field> fields = Arrays.stream(classInstance.getClass().getDeclaredFields())
                        .filter(field -> field.isAnnotationPresent(Autowire.class))
                        .toList();
                for (Field field : fields) {
                    Iterator<String> containerKeyClassName = container.keySet().iterator();
                    if (containerKeyClassName.hasNext()) {
                        String currentKey = containerKeyClassName.next();
                        field.setAccessible(true);
                        if (currentKey.equals(field.getName().toLowerCase())) {
                            field.set(classInstance, container.get(currentKey));
                        }
                    }
                }
            }
        } catch (IllegalAccessException e) {
            logger.logAndThrowException("Brak dostepu do metody: ", e);
        }
    }
}
