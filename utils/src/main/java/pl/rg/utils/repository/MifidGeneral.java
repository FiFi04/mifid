package pl.rg.utils.repository;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public abstract class MifidGeneral<E> {

  public final static String ID = "id";

  protected E id;

  public abstract String getTableName();

}
