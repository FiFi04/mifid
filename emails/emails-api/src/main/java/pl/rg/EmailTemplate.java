package pl.rg;

import pl.rg.utils.enums.EmailTemplateType;

public interface EmailTemplate {

  Integer getId();

  void setId(Integer id);

  EmailTemplateType getName();

  void setName(EmailTemplateType name);

  String getTitle();

  void setTitle(String title);

  String getTemplateBody();

  void setTemplateBody(String templateBody);
}
