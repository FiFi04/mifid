package pl.rg.security.impl;

import static pl.rg.security.impl.SecurityModuleApiImpl.ALGORITHM_TYPE;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class KeyPairTestModel {

  public final static String PASSWORD = "Password123!";

  public KeyPair getEmptyKeyPar() {
    PublicKey publicKey = new PublicKey() {
      @Override
      public String getAlgorithm() {
        return null;
      }

      @Override
      public String getFormat() {
        return null;
      }

      @Override
      public byte[] getEncoded() {
        return new byte[0];
      }
    };

    PrivateKey privateKey = new PrivateKey() {
      @Override
      public String getAlgorithm() {
        return null;
      }

      @Override
      public String getFormat() {
        return null;
      }

      @Override
      public byte[] getEncoded() {
        return new byte[0];
      }
    };
    return new KeyPair(publicKey, privateKey);
  }

  public KeyPair getValidKeyPair() {
    File privateKey = new File("src/test/resources/private.key");
    File publicKey = new File("src/test/resources/public.key");
    try {
      byte[] privateKeyBytes = Files.readAllBytes(privateKey.toPath());
      byte[] publicKeyBytes = Files.readAllBytes(publicKey.toPath());
      byte[] decodedPrivateKey = Base64.getDecoder().decode(privateKeyBytes);
      byte[] decodedPublicKey = Base64.getDecoder().decode(publicKeyBytes);
      KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM_TYPE);
      PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(decodedPrivateKey);
      X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(decodedPublicKey);
      return new KeyPair(keyFactory.generatePublic(publicKeySpec),
          keyFactory.generatePrivate(privateKeySpec));
    } catch (NoSuchAlgorithmException | IOException | InvalidKeySpecException e) {
      throw new RuntimeException(e);
    }
  }
}
