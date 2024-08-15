package pl.rg;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class Main {
    public static void main(String[] args) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {

        AppContainer appContainer = new AppContainer();

        Map<String, Object> container = appContainer.getContainer();

        for (Map.Entry<String, Object> entry : container.entrySet()) {
            System.out.println(entry.getKey() + ":" + entry.getValue());
        }
    }
}