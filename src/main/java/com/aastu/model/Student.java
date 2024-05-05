package com.aastu.model;

public class Student {
  private String firstName, middleName, lastName, idNumber, section;
  private String emailAddress;
  private int classYear;
  private College college;
  private Department department;
  private Degree degree;
  private AdmissionType admissionType;

  public Student(String firstName, String middleName, String lastName, String idNumber, String emailAddress,
      String section, int classYear, College college, Department department, Degree degree,
      AdmissionType admissionType) {
    this.firstName = firstName;
    this.middleName = middleName;
    this.lastName = lastName;
    this.idNumber = idNumber;
    this.emailAddress = emailAddress;
    this.section = section;
    this.classYear = classYear;
    this.college = college;
    this.department = department;
    this.degree = degree;
    this.admissionType = admissionType;
  }

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public String getMiddleName() {
    return middleName;
  }

  public void setMiddleName(String middleName) {
    this.middleName = middleName;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public String getIdNumber() {
    return idNumber;
  }

  public void setIdNumber(String idNumber) {
    this.idNumber = idNumber;
  }

  public String getEmailAddress() {
    return emailAddress;
  }

  public void setEmailAddress(String emailAddress) {
    this.emailAddress = idNumber;
  }

  public String getSection() {
    return section;
  }

  public void setSection(String section) {
    this.section = section;
  }

  public int getClassYear() {
    return classYear;
  }

  public void setClassYear(int classYear) {
    this.classYear = classYear;
  }

  public College getCollege() {
    return college;
  }

  public void setCollege(College college) {
    this.college = college;
  }

  public Department getDepartment() {
    return department;
  }

  public void setDepartment(Department department) {
    this.department = department;
  }

  public Degree getDegree() {
    return degree;
  }

  public void setDegree(Degree degree) {
    this.degree = degree;
  }

  public AdmissionType getAdmissionType() {
    return admissionType;
  }

  public void setAdmissionType(AdmissionType admissionType) {
    this.admissionType = admissionType;
  }

}