package pl.rg.emails.impl;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import pl.rg.Email;
import pl.rg.EmailModuleApi;
import pl.rg.emails.mapper.EmailMapper;
import pl.rg.emails.model.EmailModel;
import pl.rg.emails.model.EmailTemplateModel;
import pl.rg.emails.repository.EmailRepository;
import pl.rg.emails.repository.EmailTemplateRepository;
import pl.rg.users.UserModuleApi;
import pl.rg.utils.annotation.Autowire;
import pl.rg.utils.logger.LogLevel;
import pl.rg.utils.logger.Logger;
import pl.rg.utils.logger.LoggerImpl;
import pl.rg.utils.repository.MifidPage;
import pl.rg.utils.repository.filter.Filter;
import pl.rg.utils.repository.paging.Page;

public abstract class EmailModule implements EmailModuleApi {

  public static final String EMAIL_MESSAGE_EXCEPTION = "Błąd podczas wysyłki maila. Wiadomość nie została wysłana";

  @Autowire
  protected UserModuleApi userModuleApi;

  @Autowire
  protected EmailRepository emailRepository;

  @Autowire
  protected EmailTemplateRepository emailTemplateRepository;

  protected EmailMapper emailMapper = EmailMapper.INSTANCE;

  protected Logger logger = LoggerImpl.getInstance();

  protected String errorMessageDB;

  protected Map<String, String> templates;

  @Override
  public Map<String, String> loadTemplates() {
    return templates = emailTemplateRepository.findAll().stream()
        .collect(Collectors.toMap(
            templateModel -> templateModel.getName().getWindowColumnName(),
            EmailTemplateModel::getTemplateBody
        ));
  }

  @Override
  public void updateTemplate(String newTemplateText, String templateName) {
    templates = loadTemplates();
    if (templates.containsKey(templateName)) {
      List<EmailTemplateModel> templatesList = emailTemplateRepository.findAll();
      Optional<EmailTemplateModel> templateDB = templatesList.stream()
          .filter(template -> template.getName().getWindowColumnName().equals(templateName))
          .findFirst();
      if (templateDB.isEmpty()) {
        logger.log(LogLevel.INFO, "Brak szablonu o podanej nazwie w bazie danych: " + templateName);
      } else {
        EmailTemplateModel template = templateDB.get();
        template.setTemplateBody(newTemplateText);
        templates.put(templateName, newTemplateText);
        emailTemplateRepository.save(template);
        logger.log(LogLevel.INFO, "Szablon został zaktualizowany: " + templateName);
      }
    }
  }

  @Override
  public List<Email> getFiltered(List<Filter> filters) {
    return emailRepository.findAll(filters).stream()
        .map(emailMapper::emailModelToDomain)
        .toList();
  }

  @Override
  public MifidPage<Email> getPage(List<Filter> filters, Page page) {
    MifidPage<EmailModel> emailModelPage = emailRepository.findAll(filters, page);
    return emailMapper.emailModelPageToEmailPage(emailModelPage);
  }
}