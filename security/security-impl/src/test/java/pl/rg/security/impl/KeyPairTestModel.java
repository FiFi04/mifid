package pl.rg.security.impl;

import static pl.rg.security.impl.SecurityModuleImpl.ALGORITHM_TYPE;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;

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
      return keyPairGenerator.generateKeyPair();
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    }
  }

}
