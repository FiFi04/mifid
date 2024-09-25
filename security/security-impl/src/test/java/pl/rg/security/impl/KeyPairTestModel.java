package pl.rg.security.impl;

import static pl.rg.security.impl.SecurityModuleImpl.ALGORITHM_TYPE;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;

public class KeyPairTestModel {

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
    try {
      KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(
          ALGORITHM_TYPE);
      keyPairGenerator.initialize(2048);
      KeyPair keyPair = keyPairGenerator.generateKeyPair();
      String publicKeyHash = Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());
      String privateKeyHash = Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded());

      File keyFile = new File("src/test/resources/test.keys");
      BufferedWriter writer = new BufferedWriter(new FileWriter(keyFile));
      writer.write(publicKeyHash);
      writer.newLine();
      writer.write(privateKeyHash);
      writer.close();
      return keyPair;
    } catch (NoSuchAlgorithmException | IOException e) {
      throw new RuntimeException(e);
    }
  }
}
