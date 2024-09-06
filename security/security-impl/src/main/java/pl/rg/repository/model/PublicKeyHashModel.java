package pl.rg.repository.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import pl.rg.annotation.FieldCategory;
import pl.rg.repository.MifidGeneral;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class PublicKeyHashModel extends MifidGeneral<Integer> {

    @FieldCategory(dbColumn = true)
    public final static String KEY_HASH = "key_hash";

    @FieldCategory(dbField = true)
    private String keyHash;

    public final static String TABLE_NAME = "public_key_hash";

    public PublicKeyHashModel(Integer id, String keyHash) {
        super(id);
        this.keyHash = keyHash;
    }

    @Override
    public String getTableName() {
        return TABLE_NAME;
    }
}
