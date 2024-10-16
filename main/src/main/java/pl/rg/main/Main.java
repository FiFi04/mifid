package pl.rg.main;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import pl.rg.users.UserModuleApi;
import pl.rg.users.UserModuleController;
import pl.rg.users.impl.UserDtoImpl;
import pl.rg.users.impl.UserModuleApiImpl;
import pl.rg.users.impl.UserModuleControllerImpl;

public class Main {

  public static void main(String[] args)
      throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {

    AppContainer appContainer = new AppContainer();

    Map<String, Object> container = appContainer.getContainer();

    for (Map.Entry<String, Object> entry : container.entrySet()) {
      System.out.println(entry.getKey() + ":" + entry.getValue());
    }

//    SecurityModuleImpl securityModule = (SecurityModuleImpl) container.get(
//        "securityModuleImpl");
//    Optional<String> encryptedPassword = securityModule.encryptPassword("Admin123!");
//    System.out.println(encryptedPassword.get());
//    Optional<String> decryptedPassword = securityModule.decryptPassword(
//        encryptedPassword.get());
//    System.out.println(decryptedPassword.get());

    UserModuleApi userModuleApiImpl = (UserModuleApi) container.get(
        "userModuleApi");
    UserModuleController userControllerImpl = (UserModuleController) container.get(
        "userModuleController");
    UserDtoImpl userDto = userControllerImpl.createUser("Tomasz", "Kowalski", "t.kowalski@gmail.com");
    System.out.println(userDto);
    userControllerImpl.addUser(userDto);
  }
}