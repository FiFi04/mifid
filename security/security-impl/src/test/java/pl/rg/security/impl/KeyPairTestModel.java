package pl.rg.security.impl;

import java.security.KeyPair;
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

}
