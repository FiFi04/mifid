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
import pl.rg.utils.annotation.Autowire;
import pl.rg.utils.annotation.Service;
import pl.rg.utils.logger.LogLevel;
import pl.rg.utils.logger.Logger;
import pl.rg.utils.logger.LoggerImpl;
import pl.rg.utils.repository.MifidPage;
import pl.rg.utils.repository.filter.Filter;
import pl.rg.utils.repository.paging.Page;

@Service
public class EmailModuleMockImpl implements EmailModuleApi {

  @Autowire
  private EmailRepository emailRepository;

  @Autowire
  private EmailTemplateRepository emailTemplateRepository;

  private EmailMapper emailMapper = EmailMapper.INSTANCE;

  private Logger logger = LoggerImpl.getInstance();

  private Map<String, String> templates;

  @Override
  public void sendEmail(Email email) {
    logger.log(LogLevel.INFO, "EmailMock: wysyłka maila");
  }

  @Override
  public void resendEmail(int emailID) {
    logger.log(LogLevel.INFO, "EmailMock: ponowna wysyłka maila");
  }

  @Override
  public void sendNotification(String emailTemplate, String recipient,
      Map<String, String> placeholders) {
    logger.log(LogLevel.INFO, "EmailMock: wysłanie powiadomenia mailowego");
  }

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