package pl.rg.main;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import pl.rg.users.UserModuleApi;
import pl.rg.users.UserModuleController;

public class Main {

  public static void main(String[] args)
      throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, InterruptedException {

    AppContainer appContainer = new AppContainer();

    Map<String, Object> container = appContainer.getContainer();

    for (Map.Entry<String, Object> entry : container.entrySet()) {
      System.out.println(entry.getKey() + ":" + entry.getValue());
    }

//    SecurityModuleImpl securityModule = (SecurityModuleImpl) container.get(
//        "securityModuleImpl");
//    Optional<String> encryptedPassword = securityModule.encryptPassword("Admin123!");
//    System.out.println(encryptedPassword.get());
//    Optional<String> decryptedPassword = securityModule.decryptPassword(encryptedPassword.get());
//    System.out.println(decryptedPassword.get());

    UserModuleApi userModuleApiImpl = (UserModuleApi) container.get(
        "userModuleApi");
    UserModuleController userControllerImpl = (UserModuleController) container.get(
        "userModuleController");
//    userControllerImpl.logIn("jankow", "IDIO0X%9jf+UCX3+=i");
    userControllerImpl.createUser("Admin", "Admin",
        "Admin@email.com");
//    Optional<UserDto> user = userControllerImpl.getUser(31);
//    System.out.println(user.get());
//    user.get().setLastName("Nowak");
////    Thread.sleep(7000);
//    userControllerImpl.updateUser(user.get());
//    Optional<UserDto> updatedUser = userControllerImpl.getUser(31);
//    System.out.println(updatedUser.get());
//    userControllerImpl.logOut();
//    userControllerImpl.deleteUser(19);
  }
}