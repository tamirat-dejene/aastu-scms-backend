package com.aastu.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Notification {
  private String title;
  private String message;
  private String date;

  public Notification(String title, String message, String date) {
    this.title = title;
    this.message = message;
    this.date = date;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public String getDate() {
    return date;
  }

  public void setDate(String date) {
    this.date = date;
  }

  @Override
  public String toString() {
    return title + "           " + date.split("(,\\s[0-9]{4})")[0];
  }

  public static void main(String[] args) throws ParseException {
    Date currentDate = new Date();
    // new SimpleDateFormat("E, MMM dd, yyyy, h:mm a z");
    SimpleDateFormat dateFormat = new SimpleDateFormat("h:mm a, E MMM dd, yyyy");
    System.out.println(dateFormat.format(currentDate));
    System.out.println(new Date());

    Notification n = new Notification("bhasg", "smdnbj", dateFormat.format(currentDate));
    System.out.println(n);
  }
}
