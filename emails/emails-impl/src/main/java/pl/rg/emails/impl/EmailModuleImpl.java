package pl.rg.emails.impl;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import javax.mail.Authenticator;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import pl.rg.Email;
import pl.rg.EmailModuleApi;
import pl.rg.emails.mapper.EmailMapper;
import pl.rg.emails.model.EmailModel;
import pl.rg.emails.model.EmailTemplateModel;
import pl.rg.emails.repository.EmailRepository;
import pl.rg.users.UserModuleApi;
import pl.rg.utils.annotation.Autowire;
import pl.rg.utils.annotation.Service;
import pl.rg.utils.db.PropertiesUtils;
import pl.rg.utils.enums.EmailStatus;
import pl.rg.utils.exception.ApplicationException;
import pl.rg.utils.logger.LogLevel;
import pl.rg.utils.logger.Logger;
import pl.rg.utils.logger.LoggerImpl;
import pl.rg.utils.repository.MifidPage;
import pl.rg.utils.repository.filter.Filter;
import pl.rg.utils.repository.paging.Page;

@Service
public class EmailModuleImpl implements EmailModuleApi {

  @Autowire
  UserModuleApi userModuleApi;

  @Autowire
  EmailRepository emailRepository;

  @Autowire
  EmailTemplateService emailTemplateService;

  private final String EMAIL_MESSAGE_EXCEPTION = "Błąd podczas wysyłki maila. Wiadomość nie została wysłana";

  private EmailMapper emailMapper = EmailMapper.INSTANCE;

  private Logger logger = LoggerImpl.getInstance();

  private String errorMessageDB;

  @Override
  public void sendEmail(Email email) {
    EmailStatus emailStatus = null;
    errorMessageDB = null;
    try {
      Session session = getSession();
      MimeMessage msg = prepareMessage(email.getSubject(), email.getBody(),
          email.getRecipient(), session, email.getRecipientCc());
      Transport.send(msg);
      emailStatus = EmailStatus.SENT;
      logger.log(LogLevel.INFO, "Email wysłany pomyślnie. Temat maila: " + email.getSubject());
    } catch (MessagingException e) {
      errorMessageDB = EMAIL_MESSAGE_EXCEPTION;
      throw logger.logAndThrowRuntimeException(LogLevel.ERROR, new ApplicationException("E12NS",
          EMAIL_MESSAGE_EXCEPTION));
    } finally {
      if (emailStatus == null) {
        emailStatus = EmailStatus.ERROR;
      }
      email.setStatus(emailStatus);
      saveEmailToDB(email);
    }
  }

  @Override
  public void resendEmail(int emailID) {
    EmailModel email = emailRepository.findById(emailID).orElseThrow(
        () -> logger.logAndThrowRuntimeException(LogLevel.ERROR,
            new ApplicationException("E11NF", "Nie odnaleziono maila o id: " + emailID)));
    try {
      Session session = getSession();
      MimeMessage msg = prepareMessage(email.getSubject(), email.getBody(), email.getRecipient(),
          session, email.getRecipientCc());
      Transport.send(msg);
      email.setStatus(EmailStatus.RESENT);
      email.setErrorMessage(null);
      logger.log(LogLevel.INFO, "Wiadomość email została prawidłowo wysłana. ID :" + emailID);
    } catch (MessagingException e) {
      throw logger.logAndThrowRuntimeException(LogLevel.ERROR,
          new ApplicationException("E12NS", EMAIL_MESSAGE_EXCEPTION));
    } finally {
      email.setSentTime(LocalDateTime.now());
      email.setSentAttempts(email.getSentAttempts() + 1);
      emailRepository.save(email);
    }
  }

  @Override
  public void sendNotification(String templateName, String recipient,
      Map<String, String> placeholders) {
    List<EmailTemplateModel> templatesList = emailTemplateService.getEmailTemplateRepository()
        .findAll();
    Optional<EmailTemplateModel> templateDB = templatesList.stream()
        .filter(template -> template.getName().getWindowColumnName().equals(templateName))
        .findFirst();
    if (templateDB.isEmpty()) {
      throw logger.logAndThrowRuntimeException(LogLevel.DEBUG,
          new RuntimeException("Brak szablonu o podanej nazwie w bazie danych: " + templateName));
    } else {
      EmailTemplateModel template = templateDB.get();
      String templateBody = emailTemplateService.prepareEmailBody(template.getName().getWindowColumnName(),
          placeholders);
      Email email = new EmailDomain(template.getSubject(), templateBody, new String[]{recipient});
      sendEmail(email);
    }
  }

  @Override
  public Map<String, String> loadTemplates() {
    return emailTemplateService.loadTemplates();
  }

  @Override
  public void updateTemplate(String newTemplateText, String fileName) {
    emailTemplateService.saveTemplateChanges(newTemplateText, fileName);
  }

  private void saveEmailToDB(Email email) {
    String currentUser = userModuleApi.getCurrentUserSession().getCurrentSessionUsername();
    email.setSender(currentUser);
    email.setSentAttempts(1);
    email.setSentTime(LocalDateTime.now());
    email.setErrorMessage(errorMessageDB);
    EmailModel emailModel = emailMapper.domainToEmailModel(email);
    emailRepository.save(emailModel);
  }

  private MimeMessage prepareMessage(String subject, String body, String[] recipient,
      Session session, String... recipientCC) throws MessagingException {
    MimeMessage msg = new MimeMessage(session);
    msg.addHeader("Content-type", "text/HTML; charset=UTF-8");
    msg.addHeader("format", "flowed");
    msg.addHeader("Content-Transfer-Encoding", "8bit");

    msg.setFrom(new InternetAddress(PropertiesUtils.getProperty(PropertiesUtils.EMAIL_USERNAME)));
    msg.setSubject(subject, "UTF-8");
    msg.setText(body, "UTF-8");
    msg.setSentDate(new Date());
    InternetAddress[] recipientAddresses = getAddresses(recipient);
    msg.setRecipients(RecipientType.TO, recipientAddresses);
    if (recipientCC != null && recipientCC.length > 0 && !recipientCC[0].isBlank()) {
      InternetAddress[] recipientCCAddresses = getAddresses(recipientCC);
      msg.setRecipients(RecipientType.CC, recipientCCAddresses);
    }
    return msg;
  }

  private Session getSession() {
    Properties props = new Properties();
    props.put("mail.smtp.host", PropertiesUtils.getProperty(PropertiesUtils.EMAIL_SERVER));
    props.put("mail.smtp.port", PropertiesUtils.getProperty(PropertiesUtils.EMAIL_PORT));
    props.put("mail.smtp.auth", PropertiesUtils.getProperty(PropertiesUtils.EMAIL_AUTH));
    props.put("mail.smtp.starttls.enable",
        PropertiesUtils.getProperty(PropertiesUtils.EMAIL_STARTTLS));
    Authenticator authenticator = new Authenticator() {
      @Override
      protected PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(
            PropertiesUtils.getProperty(PropertiesUtils.EMAIL_USERNAME),
            PropertiesUtils.getProperty(PropertiesUtils.EMAIL_PASSWORD));
      }
    };
    return Session.getInstance(props, authenticator);
  }

  private InternetAddress[] getAddresses(String[] recipients) {
    String message = "Nieprawidłowy adres email: ";
    return Arrays.stream(recipients)
        .map(recipient -> {
          try {
            return new InternetAddress(recipient);
          } catch (AddressException e) {
            errorMessageDB = message + recipient;
            throw logger.logAndThrowRuntimeException(LogLevel.ERROR,
                new ApplicationException("E10SE", message + recipient));
          }
        })
        .toArray(InternetAddress[]::new);
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