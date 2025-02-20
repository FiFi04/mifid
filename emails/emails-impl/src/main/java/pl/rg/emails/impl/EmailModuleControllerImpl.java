package pl.rg.emails.impl;

import java.util.List;
import java.util.Map;
import lombok.Data;
import pl.rg.Email;
import pl.rg.EmailDto;
import pl.rg.EmailModuleApi;
import pl.rg.EmailModuleController;
import pl.rg.emails.mapper.EmailMapper;
import pl.rg.utils.annotation.Autowire;
import pl.rg.utils.annotation.Controller;
import pl.rg.utils.exception.ValidationException;
import pl.rg.utils.logger.LogLevel;
import pl.rg.utils.logger.Logger;
import pl.rg.utils.logger.LoggerImpl;
import pl.rg.utils.repository.MifidPage;
import pl.rg.utils.repository.filter.Filter;
import pl.rg.utils.repository.paging.Page;
import pl.rg.utils.validator.api.ValidatorService;

@Controller
@Data
public class EmailModuleControllerImpl implements EmailModuleController {

  @Autowire
  private EmailModuleApi emailModuleApi;

  @Autowire
  private ValidatorService validatorService;

  private EmailMapper emailMapper = EmailMapper.INSTANCE;

  private Logger logger = LoggerImpl.getInstance();

  @Override
  public void sendEmail(String subject, String body, String[] recipient, String... recipientCC) {
    EmailDto emailDto = new EmailDto(subject, body, recipient, recipientCC);
    Map<String, String> constraints = validatorService.validateFields(emailDto);
    if (constraints.isEmpty()) {
      Email email = emailMapper.dtoToDomain(emailDto);
      emailModuleApi.sendEmail(email);
    } else {
      throw logger.logAndThrowRuntimeException(LogLevel.DEBUG,
          new ValidationException("Błędne dane podczas tworzenia maila: ", constraints));
    }
  }

  @Override
  public void resendEmail(int emailID) {
    emailModuleApi.resendEmail(emailID);
  }

  @Override
  public List<EmailDto> getFiltered(List<Filter> filters) {
    return emailModuleApi.getFiltered(filters).stream()
        .map(emailMapper::domainToDto)
        .toList();
  }

  @Override
  public MifidPage getPage(List<Filter> filters, Page page) {
    MifidPage<Email> emailPage = emailModuleApi.getPage(filters, page);
    return emailMapper.emailPageToEmailDtoPage(emailPage);
  }

  @Override
  public Map<String, String> loadTemplates() {
    return emailModuleApi.loadTemplates();
  }

  @Override
  public void updateTemplate(String newTemplateText, String fileName) {
    emailModuleApi.updateTemplate(newTemplateText, fileName);
  }
}