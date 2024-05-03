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


public class Util {
  public static Properties getEnv() {
    Properties properties = new Properties();
    try (InputStream input = new FileInputStream("src/main/resources/config.properties")) {
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
  public static void main(String[] args) {
    Util.getEnv();
  }


}
