package pl.rg.repository;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public abstract class Mifid<E> {
    @FieldCategory(dbColumn = true)
    public final static String ID = "id";

    @FieldCategory(dbField = true)
    protected E id;

    public abstract String getTableName();

}
