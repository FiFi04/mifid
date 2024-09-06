package pl.rg.repository;

import pl.rg.annotation.Repository;
import pl.rg.repository.model.PublicKeyHashModel;

import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Optional;

@Repository
public class PublicKeyHashRepository extends MifidRepository<PublicKeyHashModel, Integer> {

    public Optional<PublicKey> getPublicKey() {
        try {
            Optional<PublicKeyHashModel> keyHashModel = findById(1);
            if (keyHashModel.isPresent()) {
                String publicKeyHash = keyHashModel.get().getKeyHash();
                byte[] publicKeyBytes = Base64.getDecoder().decode(publicKeyHash);
                KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);
                return Optional.of(keyFactory.generatePublic(publicKeySpec));
            }
        } catch (GeneralSecurityException e) {
            logger.logAndThrowException("Błąd odszyfrowania klucza z bazy : ", e);
        }
        return Optional.empty();
    }

    public boolean savePublicKey(PublicKey publicKey) {
            String publicKeyHash = Base64.getEncoder().encodeToString(publicKey.getEncoded());
            PublicKeyHashModel publicKeyHashModel = new PublicKeyHashModel(1, publicKeyHash);
            save(publicKeyHashModel);
            return true;
    }
}
