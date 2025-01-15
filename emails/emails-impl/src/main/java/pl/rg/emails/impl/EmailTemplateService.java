package pl.rg.emails.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import lombok.Data;
import pl.rg.emails.model.EmailTemplateModel;
import pl.rg.emails.repository.EmailTemplateRepository;
import pl.rg.utils.annotation.Autowire;
import pl.rg.utils.annotation.Service;
import pl.rg.utils.logger.LogLevel;
import pl.rg.utils.logger.Logger;
import pl.rg.utils.logger.LoggerImpl;

@Data
@Service
public class EmailTemplateService {

  @Autowire
  private EmailTemplateRepository emailTemplateRepository;

  private Map<String, String> templates;

  private Logger logger = LoggerImpl.getInstance();

  public String prepareEmailBody(String templateName, Map<String, String> placeholders) {
    String templateBody = "";
    try {
      templateBody = loadTemplates().get(templateName);
      for (Entry<String, String> entry : placeholders.entrySet()) {
        templateBody = templateBody.replace(entry.getKey(), entry.getValue());
      }
    } catch (NullPointerException e) {
      logger.logAndThrowRuntimeException(LogLevel.ERROR,
          new RuntimeException("Brak szablanu o podanej nazwie: " + templateName));
    }
    return templateBody;
  }

    public void saveTemplateChanges(String newTemplateText, String templateName) {
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
        this.templates.put(templateName, newTemplateText);
        emailTemplateRepository.save(template);
        logger.log(LogLevel.INFO, "Szablon został zaktualizowany: " + templateName);
      }
    }
  }

  public Map<String, String> loadTemplates() {
    templates = new HashMap<>();
    List<EmailTemplateModel> templatesModels = emailTemplateRepository.findAll();
    for (EmailTemplateModel model : templatesModels) {
      templates.put(model.getName().getWindowColumnName(), model.getTemplateBody());
    }
    return templates;
  }
}