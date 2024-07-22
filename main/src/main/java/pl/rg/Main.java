package pl.rg;

import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import pl.rg.annotations.Autowire;
import pl.rg.annotations.Controller;
import pl.rg.annotations.Repository;
import pl.rg.annotations.Service;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {
    public static void main(String[] args) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        System.out.println("Hello world!");

        Map<String, Object> container = new HashMap<>();
        Set<Class<?>> annotatedClasses = getAllAnnotatedClasses();
        initializeContainer(annotatedClasses, container);
        initializeFields(container);

//        Stream.of(System.getProperty("java.class.path").split(System.getProperty("path.separator")))
//                .map(Paths::get)
//                .forEach(System.out::println);

        for (Map.Entry<String, Object> entry : container.entrySet()) {
            System.out.println(entry.getKey() + ":" + entry.getValue());
        }
    }

    private static Set<Class<?>> getAllAnnotatedClasses() {
        Reflections reflections = new Reflections(new ConfigurationBuilder()
                .setUrls(ClasspathHelper.forJavaClassPath())
                .setScanners(Scanners.TypesAnnotated));
        Set<Class<?>> repositories = reflections.getTypesAnnotatedWith(Repository.class);
        Set<Class<?>> services = reflections.getTypesAnnotatedWith(Service.class);
        Set<Class<?>> controllers = reflections.getTypesAnnotatedWith(Controller.class);
        return Stream.of(repositories, services, controllers).flatMap(Set::stream).collect(Collectors.toSet());
    }

    private static void initializeContainer(Set<Class<?>> annotatedClasses, Map<String, Object> container) throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        for (Class<?> annotatedClass : annotatedClasses) {
            String className = annotatedClass.getSimpleName().toLowerCase();
            Constructor<?> constructor = annotatedClass.getConstructor();
            Object instance = constructor.newInstance();
            container.put(className, instance);
        }
    }

    private static void initializeFields(Map<String, Object> container) throws IllegalAccessException {
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
    }
}