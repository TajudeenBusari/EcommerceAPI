/*
 *Copyright © 2025
 * @Author = TJTechy (Tajudeen Busari)
 * @Version = 1.0
 * This file is part of the user-service module of the Ecommerce Microservices project.
 */
package com.tjtechy.user_service.keygenerator;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;

/**
 * This class is just used to generate the public and private keys for the application.
 * It is not an integral part of the application and is not used in the actual code.
 */
public class KeyGenerator {
  static void main(String[] args) throws NoSuchAlgorithmException, IOException {
    KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
    keyPairGenerator.initialize(2048);
    KeyPair keyPair = keyPairGenerator.generateKeyPair();

    RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
    RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
    writePublicKey(publicKey);
    writePrivateKey(privateKey);

    System.out.println("=====Keys generated successfully.=========");
  }

  private static void writePrivateKey(RSAPrivateKey privateKey) throws IOException {
    String pem = "-----BEGIN PRIVATE KEY-----\n"
            + Base64.getMimeEncoder(64, "\n".getBytes())
            .encodeToString(privateKey.getEncoded())
            + "\n-----END PRIVATE KEY-----";
    Files.writeString(Path.of("private.pem"), pem);
  }

  private static void writePublicKey(RSAPublicKey publicKey) throws IOException {
    String pem = "-----BEGIN PUBLIC KEY-----\n"
            + Base64.getMimeEncoder(64, "\n".getBytes())
            .encodeToString(publicKey.getEncoded())
            + "\n-----END PUBLIC KEY-----";
    Files.writeString(Path.of("public.pem"), pem);
  }
}
