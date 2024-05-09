package com.aastu.model;

public class UpdatePassword {
  String oldPassword;
  String newPassword;
  String confirm;
  public String getOldPassword() {
    return oldPassword;
  }
  public void setOldPassword(String oldPassword) {
    this.oldPassword = oldPassword;
  }
  public String getNewPassword() {
    return newPassword;
  }
  public void setNewPassword(String newPassword) {
    this.newPassword = newPassword;
  }
  public String getConfirm() {
    return confirm;
  }
  public void setConfirm(String confirm) {
    this.confirm = confirm;
  }
}
