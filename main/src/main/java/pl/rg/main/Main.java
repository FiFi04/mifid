package pl.rg.main;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Optional;
import pl.rg.security.impl.PasswordServiceImpl;

public class Main {

  public static void main(String[] args)
      throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {

    AppContainer appContainer = new AppContainer();

    Map<String, Object> container = appContainer.getContainer();

    for (Map.Entry<String, Object> entry : container.entrySet()) {
      System.out.println(entry.getKey() + ":" + entry.getValue());
    }

    PasswordServiceImpl passwordserviceimpl = (PasswordServiceImpl) container.get(
        "passwordserviceimpl");
    Optional<String> encryptedPassword = passwordserviceimpl.encryptPassword("Admin123!");
    System.out.println(encryptedPassword.get());
    Optional<String> decryptedPassword = passwordserviceimpl.decryptPassword(
        encryptedPassword.get());
    System.out.println(decryptedPassword.get());
  }
}