package pl.rg.main;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Optional;
import pl.rg.security.impl.PasswordServiceImpl;
import pl.rg.users.User;
import pl.rg.users.impl.UserModuleImpl;

public class Main {

  public static void main(String[] args)
      throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {

    AppContainer appContainer = new AppContainer();

    Map<String, Object> container = appContainer.getContainer();

    for (Map.Entry<String, Object> entry : container.entrySet()) {
      System.out.println(entry.getKey() + ":" + entry.getValue());
    }

//    PasswordServiceImpl passwordserviceimpl = (PasswordServiceImpl) container.get(
//        "passwordserviceimpl");
//    Optional<String> encryptedPassword = passwordserviceimpl.encryptPassword("Admin123!");
//    System.out.println(encryptedPassword.get());
//    Optional<String> decryptedPassword = passwordserviceimpl.decryptPassword(
//        encryptedPassword.get());
//    System.out.println(decryptedPassword.get());

    UserModuleImpl usermoduleimpl = (UserModuleImpl) container.get(
        "usermoduleimpl");
    User user = usermoduleimpl.createUser("USER1", "Tomasz", "Kowalski", "t.kowalski@gmail.com");
    System.out.println(user);
  }
}