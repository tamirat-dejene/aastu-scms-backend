package com.aastu.model;

public class ClearanceApplication {
  private String studentId;
  private String reason;
  private Status status;
  public ClearanceApplication(String studentId, String reason, Status status) {
    this.studentId = studentId;
    this.reason = reason;
    this.status = status;
  }
  public String getStudentId() {
    return studentId;
  }
  public void setStudentId(String studentId) {
    this.studentId = studentId;
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
}
