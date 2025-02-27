package pl.rg.emails.repository;

import pl.rg.emails.model.EmailModel;
import pl.rg.utils.annotation.Repository;
import pl.rg.utils.repository.MifidRepository;

@Repository
public class EmailRepository extends MifidRepository<EmailModel, Integer> {

}