package pl.rg.users.impl;

import org.mapstruct.Mapper;
import org.mapstruct.ObjectFactory;
import org.mapstruct.factory.Mappers;
import pl.rg.users.User;
import pl.rg.users.model.UserModel;

@Mapper
public interface UserMapper {

  UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

  UserModel domainToUserModel(User user);

  User userModelToDomain(UserModel userModel);

  @ObjectFactory
  default User createUser() {
    return new UserImpl();
  }
}
