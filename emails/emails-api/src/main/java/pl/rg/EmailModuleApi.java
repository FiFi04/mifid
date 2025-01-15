package pl.rg;

import java.util.Map;
import pl.rg.utils.pageAndSort.PageFilter;

public interface EmailModuleApi extends PageFilter<Email> {

  void sendEmail(Email email);

  void resendEmail(int emailID);

  void sendNotification(String emailTemplate, String recipient,
      Map<String, String> placeholders);

  Map<String, String> loadTemplates();

  void updateTemplate(String newTemplateText, String fileName);
}
