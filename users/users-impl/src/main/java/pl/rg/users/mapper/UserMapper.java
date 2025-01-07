package pl.rg.users.mapper;

import java.time.LocalDateTime;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ObjectFactory;
import org.mapstruct.factory.Mappers;
import pl.rg.users.User;
import pl.rg.users.UserDto;
import pl.rg.users.impl.UserImpl;
import pl.rg.users.model.UserModel;
import pl.rg.utils.pageAndSort.MifidPageMapper;
import pl.rg.utils.repository.MifidPage;

@Mapper
public interface UserMapper extends MifidPageMapper {

  UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

  UserModel domainToUserModel(User user);

  User userModelToDomain(UserModel userModel);

  User dtoToDomain(UserDto userDto);

  @Mapping(source = "blockedTime", target = "blocked", qualifiedByName = "dateToValue")
  UserDto domainToDto(User userModel);

  @Named("dateToValue")
  default String dateToValue(LocalDateTime blockedTime) {
    return blockedTime == null ? "NIE" : "TAK";
  }

  @ObjectFactory
  default User createUserImpl() {
    return new UserImpl();
  }

  MifidPage<User> userModelPageToUserPage(MifidPage<UserModel> userModelPage);

  MifidPage<UserDto> userPageToUserDtoPage(MifidPage<User> userPage);
}
