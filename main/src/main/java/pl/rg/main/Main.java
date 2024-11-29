package pl.rg.main;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import pl.rg.users.UserModuleApi;
import pl.rg.users.UserModuleController;
import pl.rg.utils.logger.LogLevel;
import pl.rg.utils.logger.Logger;
import pl.rg.utils.logger.LoggerImpl;
import pl.rg.utils.repository.MifidPage;
import pl.rg.utils.repository.filter.Filter;
import pl.rg.utils.repository.filter.FilterConditionType;
import pl.rg.utils.repository.filter.FilterDataType;
import pl.rg.utils.repository.filter.FilterSearchType;
import pl.rg.utils.repository.paging.Order;
import pl.rg.utils.repository.paging.OrderType;
import pl.rg.utils.repository.paging.Page;

public class Main {

  public static void main(String[] args)
      throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, InterruptedException {

    AppContainer appContainer = new AppContainer();

    Map<String, Object> container = appContainer.getContainer();

    for (Map.Entry<String, Object> entry : container.entrySet()) {
      System.out.println(entry.getKey() + ":" + entry.getValue());
    }

    Logger logger = LoggerImpl.getInstance();

//    SecurityModuleImpl securityModule = (SecurityModuleImpl) container.get(
//        "securityModuleImpl");
//    Optional<String> encryptedPassword = securityModule.encryptPassword("Admin123!");
//    System.out.println(encryptedPassword.get());
//    Optional<String> decryptedPassword = securityModule.decryptPassword(encryptedPassword.get());
//    System.ou  t.println(decryptedPassword.get());

    UserModuleApi userModuleApiImpl = (UserModuleApi) container.get(
        "userModuleApi");
    UserModuleController userControllerImpl = (UserModuleController) container.get(
        "userModuleController");

    Filter filter = new Filter("first_name", new Object[]{"Jan"}, FilterSearchType.MATCH);
    Filter filter2 = new Filter("last_name", new Object[]{"Nowak"},
        FilterDataType.STRING, FilterSearchType.EQUAL, FilterConditionType.OR);
    Order order = new Order("last_name", OrderType.ASC);
    Order order2 = new Order("first_name", OrderType.DESC);
    Page page = new Page(0, 2, List.of(order, order2));
    MifidPage mifidPage = userControllerImpl.getPage(List.of(filter, filter2), page);
    System.out.println("Ilosc stron: " + mifidPage.getTotalPage());
    System.out.println("Obiekty od: " + mifidPage.getObjectFrom());
    System.out.println("Obiekty do: " + mifidPage.getObjectTo());
    System.out.println("Ilosc wszystkich obiektów: " + mifidPage.getTotalObjects());
    for (Object limitedObject : mifidPage.getLimitedObjects()) {
      System.out.println(limitedObject.toString());
    }

    logger.log(LogLevel.INFO, "INFO LEVEL");
    logger.log(LogLevel.DEBUG, "DEBUG LEVEL");
    logger.log(LogLevel.ERROR, "ERROR LEVEL");
    logger.logSql(LogLevel.INFO, "SQL");

//    Filter filter = new Filter("login_time", new Object[]{LocalDate.of(2024,10,30)},
//        FilterDataType.INTEGER, FilterSearchType.MATCH);
//    List<SessionModel> all = new SessionRepository().findAll(List.of(filter));
//    System.out.println(all);

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