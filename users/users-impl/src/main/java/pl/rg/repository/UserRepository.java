package pl.rg.repository;

import pl.rg.annotation.Repository;
import pl.rg.model.UserModel;

@Repository
public class UserRepository extends MifidRepository<UserModel, Integer> {
}
