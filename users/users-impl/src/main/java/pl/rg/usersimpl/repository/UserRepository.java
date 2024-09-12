package pl.rg.usersimpl.repository;

import pl.rg.utils.annotation.Repository;
import pl.rg.utils.repository.MifidRepository;
import pl.rg.usersimpl.repository.model.UserModel;

@Repository
public class UserRepository extends MifidRepository<UserModel, Integer> {
}
