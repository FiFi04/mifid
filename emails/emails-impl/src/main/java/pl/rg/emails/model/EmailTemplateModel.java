package pl.rg.emails.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import pl.rg.utils.enums.EmailTemplateType;
import pl.rg.utils.annotation.FieldCategory;
import pl.rg.utils.repository.MifidGeneral;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class EmailTemplateModel extends MifidGeneral<Integer> {

  @FieldCategory(dbColumn = true)
  public static final String NAME = "name";

  @FieldCategory(dbColumn = true)
  public static final String SUBJECT = "subject";

  @FieldCategory(dbColumn = true)
  public static final String TEMPLATE_BODY = "template_body";

  public static final String TABLE_NAME = "email_template";

  @FieldCategory(dbField = true)
  private EmailTemplateType name;

  @FieldCategory(dbField = true)
  private String subject;

  @FieldCategory(dbField = true)
  private String templateBody;

  @Override
  public String getTableName() {
    return TABLE_NAME;
  }
}

