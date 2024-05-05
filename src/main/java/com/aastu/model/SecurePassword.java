package com.aastu.model;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.codec.binary.Hex;

import com.aastu.utils.Util;

public class SecurePassword {
  private String hashedPassword;

  public SecurePassword(String hashedPassword) {
    this.hashedPassword = hashedPassword;
  }

  public String getHashedPassword() {
    return hashedPassword;
  }

  public void setHashedPassword(String hashedPassword) {
    this.hashedPassword = hashedPassword;
  }

  @Override
  public String toString() {
    return hashedPassword;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    SecurePassword other = (SecurePassword) obj;
    if (hashedPassword == null) {
      if (other.hashedPassword != null)
        return false;
    } else if (!hashedPassword.equals(other.hashedPassword))
      return false;
    return true;
  }

  public static SecurePassword saltAndHashPassword(String password) {
    return saltAndHashPassword(16, password, "SHA-256");
  }

  public static SecurePassword saltAndHashPassword(int saltByteSize, String password) {
    return saltAndHashPassword(saltByteSize, password, "SHA-256");
  }

  public static SecurePassword saltAndHashPassword(int saltByteSize, String password, String algorithm) {
    String salt = Util.generateSalt(saltByteSize);
    String saltedPasword = salt + password;

    MessageDigest passwordDigest;
    try {
      passwordDigest = MessageDigest.getInstance(algorithm);
      byte[] hashedPasswordByteArray = passwordDigest.digest(saltedPasword.getBytes());
      String saltHash = salt + ":" + Hex.encodeHexString(hashedPasswordByteArray);
      return new SecurePassword(saltHash);
    } catch (NoSuchAlgorithmException e) {
      System.out.println(e.getMessage());
    }
    return null;
  }

  public static SecurePassword saltAndHashPassword(String salt, String password) {
    return saltAndHashPassword(salt, password, "SHA-256");
  }

  static SecurePassword saltAndHashPassword(String salt, String password, String algorithm) {
    String saltedPasword = salt + password;
    MessageDigest passwordDigest;
    try {
      passwordDigest = MessageDigest.getInstance(algorithm);
      byte[] hashedPasswordByteArray = passwordDigest.digest(saltedPasword.getBytes());
      String saltHash = salt + ":" + Hex.encodeHexString(hashedPasswordByteArray);
      return new SecurePassword(saltHash);
    } catch (NoSuchAlgorithmException e) {
      System.out.println(e.getMessage());
    }
    return null;
  }

  public static boolean authenticatePassword(String inputPassword, String storedPassword) {
    return authenticatePassword(inputPassword, storedPassword, "SHA-256");
  }

  public static boolean authenticatePassword(String inputPassword, String storedPassword, String algorithm) {
    var saltandHash = storedPassword.split(":");
    if (saltandHash.length <= 1)
      return false;
    var salt = saltandHash[0];
    return storedPassword.equals(saltAndHashPassword(salt, inputPassword, algorithm).toString());
  }
}
