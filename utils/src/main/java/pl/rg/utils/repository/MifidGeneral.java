package pl.rg.utils.repository;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import pl.rg.utils.annotation.FieldCategory;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public abstract class MifidGeneral<E> {

  @FieldCategory(dbColumn = true)
  public final static String ID = "id";

  @FieldCategory(dbField = true)
  protected E id;

  public abstract String getTableName();
}