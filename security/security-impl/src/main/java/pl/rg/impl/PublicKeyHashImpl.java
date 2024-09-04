package pl.rg.impl;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import pl.rg.PublicKeyHash;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class PublicKeyHashImpl implements PublicKeyHash {

    private Integer id;

    private String keyHash;

    public PublicKeyHashImpl(String keyHash) {
        this.keyHash = keyHash;
    }
}
