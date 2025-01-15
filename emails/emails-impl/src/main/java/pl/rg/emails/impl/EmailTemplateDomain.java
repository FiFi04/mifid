package pl.rg.emails.impl;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import pl.rg.EmailTemplate;
import pl.rg.utils.enums.EmailTemplateType;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class EmailTemplateDomain implements EmailTemplate {

  private Integer id;

  private EmailTemplateType name;

  private String title;

  private String templateBody;

  public EmailTemplateDomain(EmailTemplateType name, String title, String templateBody) {
    this.name = name;
    this.title = title;
    this.templateBody = templateBody;
  }
}
