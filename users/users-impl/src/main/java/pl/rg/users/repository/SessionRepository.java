package pl.rg.users.repository;

import pl.rg.users.model.SessionModel;
import pl.rg.utils.annotation.Repository;
import pl.rg.utils.repository.MifidRepository;

@Repository
public class SessionRepository extends MifidRepository<SessionModel, Integer> {

}
