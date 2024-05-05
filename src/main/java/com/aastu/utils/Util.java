package com.aastu.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Date;
import java.util.Properties;

import org.apache.commons.codec.binary.Hex;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;

public class Util {
  public static Properties getEnv() {
    Properties properties = new Properties();
    try (InputStream input = new FileInputStream("http-server/src/main/resources/config.properties")) {
      properties.load(input);
      return properties;
    } catch (IOException e) {
      return null;
    }
  }

  public static String generateSalt() {
    return generateSalt(16);
  }

  public static String generateSalt(int byteSize) {
    SecureRandom secureRandom;
    try {
      secureRandom = SecureRandom.getInstanceStrong();
    } catch (NoSuchAlgorithmException e) {
      return "";
    }
    byte[] salt = new byte[byteSize];
    secureRandom.nextBytes(salt);
    return Hex.encodeHexString(salt);
  }

  public static String signUser(String payLoad) {
    String secretKey = Util.getEnv().getProperty("ACCESS_TOKEN_SECRET_KEY");
    Algorithm algorithm = Algorithm.HMAC256(secretKey);
    return JWT.create().withSubject(payLoad).withIssuedAt(new Date()).sign(algorithm);
  }

  public static boolean verifyUser(String token) {
    String secretKey = Util.getEnv().getProperty("ACCESS_TOKEN_SECRET_KEY");
    DecodedJWT decodedJWT;
    try {
      Algorithm algorithm = Algorithm.HMAC256(secretKey);
      JWTVerifier verifier = JWT.require(algorithm).build();

      decodedJWT = verifier.verify(token);
    } catch (JWTVerificationException exception) {
      return false;
    }

    System.out.println(decodedJWT.getSignature());
    return true;
  }

  public static void main(String[] args) {
    Util.getEnv();
  }

}
