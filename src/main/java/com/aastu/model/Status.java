package com.aastu.model;

public enum Status {
  PENDING,
  APPROVED,
  DECLINED;

  public static Status fromString(String status) {
    return status.equals("PENDING") ? Status.PENDING : status.equals("APPROVED") ? Status.APPROVED : Status.DECLINED;
  }
}
