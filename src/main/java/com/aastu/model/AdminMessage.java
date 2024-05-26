package com.aastu.model;

public class AdminMessage {
  private String receiverId;
  private String message;
  public AdminMessage(String receiverId, String message) {
    this.receiverId = receiverId;
    this.message = message;
  }
  public String getReceiverId() {
    return receiverId;
  }
  public String getMessage() {
    return message;
  }
  public void setReceiverId(String receiverId) {
    this.receiverId = receiverId;
  }
  public void setMessage(String message) {
    this.message = message;
  }
}
