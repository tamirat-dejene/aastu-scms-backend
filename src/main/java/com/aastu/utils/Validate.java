package com.aastu.utils;

import java.util.regex.*;

public class Validate {
  public static boolean name(String name) {
    var isTrue = Pattern.compile("^[a-zA-Z]+").matcher(name).matches();
    if(isTrue) return isTrue;
    throw new Error("Invalid name");
  }

  public static boolean email(String email) {
    String regex = "^[a-zA-Z]+\\.[a-zA-Z]+@aastustudent\\.edu\\.et$";
    Pattern pattern = Pattern.compile(regex);
    Matcher matcher = pattern.matcher(email);
    if(matcher.matches()) return true;
    throw new Error("Invalid email address");
  }

  public static boolean idNumber(String idNumber) {
    var isTrue = Pattern.compile("^(ets){1}[0-9]{4}/[0-9]{1,2}", Pattern.CASE_INSENSITIVE).matcher(idNumber).matches();
    if(isTrue) return isTrue;
    throw new Error("Invalid id#");
  }

  public static boolean password(String password) {
    if (password.length() < 6)
      throw new Error("length must be > 6");
    return true;
  }

  public static boolean section(String section) {
    var isTrue = Pattern.compile("^[a-z0-9]{1,2}", Pattern.CASE_INSENSITIVE).matcher(section).matches();
    if (isTrue)
      return isTrue;
    throw new Error("Invalid section");
  }
  
  public static boolean classYear(String year) {
    try {
      int y = Integer.parseInt(year);
      if (y < 1 || y > 10)
        throw new Error("Invalid class year");
      return true;
    } catch (Exception e) {
      throw new Error(e.getMessage());
    }
  }

  public static void main(String[] args) {
    
  }
}
