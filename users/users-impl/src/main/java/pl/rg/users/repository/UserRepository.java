package pl.rg.users.repository;

import pl.rg.users.model.UserModel;
import pl.rg.utils.annotation.Repository;
import pl.rg.utils.repository.MifidRepository;

@Repository
public class UserRepository extends MifidRepository<UserModel, Integer> {

}
