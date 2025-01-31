package pl.rg;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import pl.rg.utils.enums.EmailTemplateType;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class EmailTemplateDto {

  private Integer id;

  private EmailTemplateType name;

  private String subject;

  private String templateBody;

  public EmailTemplateDto(EmailTemplateType name, String subject, String templateBody) {
    this.name = name;
    this.subject = subject;
    this.templateBody = templateBody;
  }
}