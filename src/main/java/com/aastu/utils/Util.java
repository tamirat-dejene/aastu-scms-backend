package com.aastu.utils;

import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Date;
import java.util.Properties;
import java.util.Random;
import java.util.UUID;

import org.apache.commons.codec.binary.Hex;

import com.aastu.model.Login;
import com.aastu.model.Payload;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;

public class Util {
  public static Properties getEnv() {
    // Properties properties = new Properties();
    // try (InputStream input = new
    // FileInputStream("http-server/src/main/resources/config.properties")) {
    // properties.load(input);
    // return properties;
    // } catch (IOException e) {
    // return null;
    // }

    Properties properties = new Properties();
    try (InputStream input = ClassLoader.getSystemResourceAsStream("config.properties")) {
      if (input != null) {
        properties.load(input);
        return properties;
      } else {
        System.err.println("Unable to find config.properties file.");
        return null;
      }
    } catch (IOException e) {
      e.printStackTrace();
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
    decodedJWT.getPayload();
    return true;
  }

  public static String getDecodedPayload(String token) {
    DecodedJWT decodedJWT = JWT.decode(token);
    byte[] payLoad = Base64.getDecoder().decode(decodedJWT.getPayload());

    // The payload parts sub and iat(issued at) and we need the sub part
    Payload payload2 = new Payload();
    payload2 = (Payload) ReqRes.makeModelFromJson(new String(payLoad), Payload.class);

    return payload2.getSub();
  }

  public static UUID generateUniqueId(String name) {
    UUID uuid = UUID.fromString("00000000-0000-0000-0000-000000000000");
    return UUID.nameUUIDFromBytes((uuid.toString() + name).getBytes());
  }
 
  public static String generateOTP() {
    String NUMBERS = "0123456789";
    int OTP_LENGTH = 6;
    Random random = new Random();
    StringBuilder otp = new StringBuilder(OTP_LENGTH);
    for (int i = 0; i < OTP_LENGTH; i++)
      otp.append(NUMBERS.charAt(random.nextInt(NUMBERS.length())));
    
    return otp.toString();
  }

  public static void main(String[] args) {
    var login = new Login();
    login.setIdNumber("ETS1518/14");
    login.setPassword("abcdef");

    String token = Util.signUser(ReqRes.makeJsonString(login));

    System.out.println(Util.getDecodedPayload(token));

    UUID id = Util.generateUniqueId("ETS1518/14");

    String base64Id = Base64.getEncoder().encodeToString(id.toString().getBytes());
    System.out.println(base64Id);
    System.out.println(Base64.getDecoder().decode(base64Id));

    System.out.println(generateOTP());
  }

}
