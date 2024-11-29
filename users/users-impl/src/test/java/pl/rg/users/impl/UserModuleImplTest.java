package pl.rg.users.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import pl.rg.security.SecurityModuleApi;
import pl.rg.users.User;
import pl.rg.users.model.UserModel;
import pl.rg.users.repository.UserRepository;
import pl.rg.utils.exception.ApplicationException;
import pl.rg.utils.logger.LoggerImpl;
import pl.rg.utils.repository.MifidPage;
import pl.rg.utils.repository.filter.Filter;
import pl.rg.utils.repository.filter.FilterConditionType;
import pl.rg.utils.repository.filter.FilterSearchType;
import pl.rg.utils.repository.paging.Order;
import pl.rg.utils.repository.paging.OrderType;
import pl.rg.utils.repository.paging.Page;

class UserModuleImplTest {

  protected static final String GENERATED_PASSWORD = "Password123!";

  protected static final String ENCRYPTED_PASSWORD = "EncryptedPassword123!";

  @Mock
  UserRepository userRepository;

  @Mock
  private SecurityModuleApi securityModuleApi;

  @Mock
  private LoggerImpl logger;

  @InjectMocks
  UserModuleImpl userModule;

  @BeforeEach
  public void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  public void whenAddingUserWithValidData_thenShouldSaveNewUser() {
    //given
    User user = new UserImpl();
    user.setFirstName("Jan");
    user.setLastName("Kowalski");
    user.setEmail("j.kowalski@email.com");

    try (MockedStatic<LoggerImpl> loggerMockedStatic = mockStatic(LoggerImpl.class)) {

      when(userRepository.containsUsername(anyString()))
          .thenReturn(true)
          .thenReturn(true)
          .thenReturn(false);
      loggerMockedStatic.when(LoggerImpl::getInstance).thenReturn(logger);
      when(securityModuleApi.generatePassword()).thenReturn(GENERATED_PASSWORD);
      when(securityModuleApi.encryptPassword(GENERATED_PASSWORD)).thenReturn(
          Optional.of(ENCRYPTED_PASSWORD));

      //when
      userModule.addUser(user);

      //then
      assertEquals("jankow2", user.getUserName());
      verify(userRepository, times(1)).save(any());
    }
  }

  @Test
  public void whenFoundExistingUser_ThenShouldReturnUser() {
    //given
    UserModel userModel = new UserModel("jankow", UserModuleImplTest.ENCRYPTED_PASSWORD, "Jan",
        "Kowalski", "jan.kowalski@email.com");
    userModel.setId(1);
    when(userRepository.findById(any())).thenReturn(Optional.of(userModel));

    //when
    Optional<User> user = userModule.find(1);

    //then
    assertTrue(user.isPresent());
    assertEquals("jankow", user.get().getUserName());
    assertEquals(1, user.get().getId());
  }

  @Test
  public void whenSearchForNonExistingUser_ThenShouldThrowApplicationException() {
    //given
    try (MockedStatic<LoggerImpl> loggerMockedStatic = mockStatic(LoggerImpl.class)) {
      loggerMockedStatic.when(LoggerImpl::getInstance).thenReturn(logger);
      when(logger.logAndThrowRuntimeException(any(), any())).thenReturn(new ApplicationException());

      //when
      when(userRepository.findById(any())).thenReturn(Optional.empty());

      //then
      assertThrows(ApplicationException.class, () -> userModule.find(1));
      verify(logger, times(1)).logAndThrowRuntimeException(any(), any());
    }
  }

  @Test
  public void whenUpdateUser_ThenShouldSaveUpdatedUser() {
    //given
    User user = new UserImpl("jankow", UserModuleImplTest.ENCRYPTED_PASSWORD, "Jan",
        "Kowalski", "jan.kowalski@email.com");
    user.setLastName("Nowak");

    //when
    userModule.update(user);

    //then
    verify(userRepository, times(1)).save(any());
    assertEquals("Nowak", user.getLastName());
  }

  @Test
  public void whenSearchUsersWithFilters_thenShouldReturnFilteredUsers() {
    //given
    List<Filter> filters = List.of(
        new Filter("firstName", new Object[]{"Jan"}, FilterSearchType.EQUAL),
        new Filter("lastName", new Object[]{"Nowak"}, FilterSearchType.MATCH,
            FilterConditionType.OR));
    List<UserModel> userModels = List.of(
        new UserModel("jankow", "password1", "Jan", "Kowalski", "jan.kowalski@example.com"),
        new UserModel("tomnow", "password2", "Tomasz", "Nowak", "tomasz.nowak@example.com"));
    userModels.get(0).setId(1);
    userModels.get(1).setId(2);
    List<User> users = List.of(
        new UserImpl(1, "jankow", "password1", "Jan", "Kowalski", "jan.kowalski@example.com"),
        new UserImpl(2, "tomnow", "password2", "Tomasz", "Nowak", "tomasz.nowak@example.com"));
    when(userRepository.findAll(filters)).thenReturn(userModels);

    //when
    List<User> filteredUsers = userModule.getFiltered(filters);

    //then
    verify(userRepository, times(1)).findAll(filters);
    assertEquals(users, filteredUsers);
  }

  @Test
  public void whenSearchUsersByPage_thenShouldReturnMifidUsersPage() {
    //given
    List<Filter> filters = List.of(
        new Filter("firstName", new Object[]{"Jan"}, FilterSearchType.EQUAL));
    new Filter("lastName", new Object[]{"Nowak"}, FilterSearchType.MATCH, FilterConditionType.OR);
    Order order = new Order("last_name", OrderType.ASC);
    Order order2 = new Order("first_name", OrderType.DESC);
    Page page = new Page(0, 2, List.of(order, order2));
    List<UserModel> userModels = List.of(
        new UserModel("jankow", "password1", "Jan", "Kowalski", "jan.kowalski@example.com"),
        new UserModel("tomnow", "password2", "Tomasz", "Nowak", "tomasz.nowak@example.com"));
    userModels.get(0).setId(1);
    userModels.get(1).setId(2);
    List<User> users = List.of(
        new UserImpl(1, "jankow", "password1", "Jan", "Kowalski", "jan.kowalski@example.com"),
        new UserImpl(2, "tomnow", "password2", "Tomasz", "Nowak", "tomasz.nowak@example.com"));
    when(userRepository.findAll(filters, page)).thenReturn(new MifidPage<>(2, 1, 0, 2, userModels));

    //when
    MifidPage usersPage = userModule.getPage(filters, page);

    //then
    verify(userRepository, times(1)).findAll(filters, page);
    assertEquals(users, usersPage.getLimitedObjects());
  }
}