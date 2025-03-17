package pl.rg.emails.impl;

import java.util.Map;
import pl.rg.Email;
import pl.rg.utils.annotation.Service;
import pl.rg.utils.logger.LogLevel;

@Service
public class EmailModuleMockImpl extends EmailModule {

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
}