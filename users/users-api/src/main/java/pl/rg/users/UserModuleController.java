package pl.rg.users;

import java.util.List;
import java.util.Optional;
import pl.rg.utils.repository.MifidPage;
import pl.rg.utils.repository.filter.Filter;
import pl.rg.utils.repository.paging.Page;

public interface UserModuleController {

  boolean createUser(String firstName, String lastName, String email);

  Optional<UserDto> getUser(Integer userID);

  void updateUser(UserDto userDto);

  void deleteUser(Integer userId);

  boolean logIn(String username, String password);

  void logOut();

  void resetLoginAttempts(String username);

  List<UserDto> getFiltered(List<Filter> filters);

  MifidPage getPage(List<Filter> filters, Page page);
}
