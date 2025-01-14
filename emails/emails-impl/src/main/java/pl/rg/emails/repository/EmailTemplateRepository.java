package pl.rg.emails.repository;

import pl.rg.emails.model.EmailTemplateModel;
import pl.rg.utils.annotation.Repository;
import pl.rg.utils.repository.MifidRepository;

@Repository
public class EmailTemplateRepository extends MifidRepository<EmailTemplateModel, Integer> {

}
