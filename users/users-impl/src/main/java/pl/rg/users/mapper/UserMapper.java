package pl.rg.users.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ObjectFactory;
import org.mapstruct.factory.Mappers;
import pl.rg.users.User;
import pl.rg.users.UserDto;
import pl.rg.users.impl.UserImpl;
import pl.rg.users.model.UserModel;

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

}
