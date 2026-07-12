package com.tjtechy.test_helper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;


public class TestKeyGenerator {
  static void main(String[] args) throws Exception {

    KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
    generator.initialize(2048);

    KeyPair keyPair = generator.generateKeyPair();
    writePrivateKey((RSAPrivateKey) keyPair.getPrivate());
    writePublicKey((RSAPublicKey) keyPair.getPublic());

    System.out.println("Test RSA Keys generated successfully");

  }

  private static void writePublicKey(RSAPublicKey aPublic) throws IOException {
    String pem = "-----BEGIN PUBLIC KEY-----\n"
            + Base64.getMimeEncoder(64, "\n".getBytes())
            .encodeToString(aPublic.getEncoded())
            + "\n-----END PUBLIC KEY-----";
    Files.writeString(Path.of("public-test.pem"), pem);
  }

  private static void writePrivateKey(RSAPrivateKey aPrivate) throws IOException {

    String pem = "-----BEGIN PRIVATE KEY-----\n"
            + Base64.getMimeEncoder(64, "\n".getBytes())
            .encodeToString(aPrivate.getEncoded())
            + "\n-----END PRIVATE KEY-----";
    Files.writeString(Path.of("private-test.pem"), pem);
  }
}
