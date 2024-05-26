package com.aastu.model;

public class ClearanceApp {
  private int applicationId;
  private String applicantId;
  private String reason;
  private Status status;
  private String date;
  public int getApplicationId() {
    return applicationId;
  }
  public void setApplicationId(int applicationId) {
    this.applicationId = applicationId;
  }
  public String getApplicantId() {
    return applicantId;
  }
  public void setApplicantId(String applicantId) {
    this.applicantId = applicantId;
  }
  public String getReason() {
    return reason;
  }
  public void setReason(String reason) {
    this.reason = reason;
  }
  public Status getStatus() {
    return status;
  }
  public void setStatus(Status status) {
    this.status = status;
  }
  public String getDate() {
    return date;
  }
  public void setDate(String date) {
    this.date = date;
  }
}
