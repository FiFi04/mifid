package pl.rg.emails.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import pl.rg.utils.annotation.FieldCategory;
import pl.rg.utils.repository.MifidGeneral;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class EmailTemplateModel extends MifidGeneral<Integer> {

  @FieldCategory(dbColumn = true)
  public static final String TITLE = "title";

  @FieldCategory(dbColumn = true)
  public static final String BODY = "body";

  public static final String TABLE_NAME = "email_template";

  @FieldCategory(dbField = true)
  private String title;

  @FieldCategory(dbField = true)
  private String body;

  @Override
  public String getTableName() {
    return TABLE_NAME;
  }
}

