package pl.rg.security.repository;

import static pl.rg.security.impl.SecurityModuleImpl.ALGORITHM_TYPE;

import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import pl.rg.security.exception.SecurityException;
import pl.rg.security.model.PublicKeyHashModel;
import pl.rg.utils.annotation.Repository;
import pl.rg.utils.repository.MifidRepository;

@Repository
public class PublicKeyHashRepository extends MifidRepository<PublicKeyHashModel, Integer> {

  public Optional<PublicKey> getPublicKey() {
    try {
      Optional<PublicKeyHashModel> keyHashModel = findById(1);
      if (keyHashModel.isPresent()) {
        String publicKeyHash = keyHashModel.get().getKeyHash();
        byte[] publicKeyBytes = Base64.getDecoder().decode(publicKeyHash);
        KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM_TYPE);
        X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);
        return Optional.of(keyFactory.generatePublic(publicKeySpec));
      } else {
        throw logger.logAndThrowRuntimeException(new SecurityException("Brak klucza publicznego!"));
      }
    } catch (GeneralSecurityException e) {
      throw logger.logAndThrowRuntimeException(
          new SecurityException("Błąd odszyfrowania klucza z bazy",
              e));
    }
  }

  public boolean savePublicKey(PublicKey publicKey) {
    List<PublicKeyHashModel> allKeys = findAll();
    if (!allKeys.isEmpty()) {
      deleteAll(allKeys);
    }

    String publicKeyHash = Base64.getEncoder().encodeToString(publicKey.getEncoded());
    PublicKeyHashModel publicKeyHashModel = new PublicKeyHashModel(1, publicKeyHash);
    save(publicKeyHashModel);
    return true;
  }
}
