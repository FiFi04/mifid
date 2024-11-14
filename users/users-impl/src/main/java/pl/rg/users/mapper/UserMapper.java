package pl.rg.users.mapper;

import java.util.ArrayList;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.ObjectFactory;
import org.mapstruct.factory.Mappers;
import pl.rg.users.User;
import pl.rg.users.UserDto;
import pl.rg.users.impl.UserImpl;
import pl.rg.users.model.UserModel;
import pl.rg.utils.repository.MifidPage;

@Mapper
public interface UserMapper {

  UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

  UserModel domainToUserModel(User user);

  User userModelToDomain(UserModel userModel);

  User dtoToDomain(UserDto userDto);

  UserDto domainToDto(User userModel);

  @ObjectFactory
  default User createUserImpl() {
    return new UserImpl();
  }

  default MifidPage<User> userModelPageToUserPage(MifidPage<UserModel> userModelPage) {
    MifidPage<User> userPage = new MifidPage<>(userModelPage.getTotalObjects(),
        userModelPage.getTotalPage(), userModelPage.getObjectFrom(), userModelPage.getObjectTo(),
        new ArrayList<>());
    List<User> mappedUsers = userModelPage.getLimitedObjects().stream()
        .map(this::userModelToDomain)
        .toList();
    userPage.setLimitedObjects(mappedUsers);
    return userPage;
  }

  default MifidPage<UserDto> userPageToUserDtoPage(MifidPage<User> userPage) {
    MifidPage<UserDto> userDtoPage = new MifidPage<>(userPage.getTotalObjects(),
        userPage.getTotalPage(), userPage.getObjectFrom(), userPage.getObjectTo(),
        new ArrayList<>());
    List<UserDto> mappedUsers = userPage.getLimitedObjects().stream()
        .map(this::domainToDto)
        .toList();
    userDtoPage.setLimitedObjects(mappedUsers);
    return userDtoPage;
  }

  default MifidPage<UserModel> userPageToUserModelPage(MifidPage<User> userPage) {
    MifidPage<UserModel> userModelPage = new MifidPage<>(userPage.getTotalObjects(),
        userPage.getTotalPage(), userPage.getObjectFrom(), userPage.getObjectTo(),
        new ArrayList<>());
    List<UserModel> mappedUsers = userPage.getLimitedObjects().stream()
        .map(this::domainToUserModel)
        .toList();
    userModelPage.setLimitedObjects(mappedUsers);
    return userModelPage;
  }

  default MifidPage<User> userDtoPageToUserPage(MifidPage<UserDto> userDtoPage) {
    MifidPage<User> userPage = new MifidPage<>(userDtoPage.getTotalObjects(),
        userDtoPage.getTotalPage(), userDtoPage.getObjectFrom(), userDtoPage.getObjectTo(),
        new ArrayList<>());
    List<User> mappedUsers = userDtoPage.getLimitedObjects().stream()
        .map(this::dtoToDomain)
        .toList();
    userPage.setLimitedObjects(mappedUsers);
    return userPage;
  }
}
