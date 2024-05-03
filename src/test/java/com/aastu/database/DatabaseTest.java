package com.aastu.database;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.sql.SQLException;

import org.junit.jupiter.api.Test;

import com.aastu.model.AdmissionType;
import com.aastu.model.ClearanceApplication;
import com.aastu.model.College;
import com.aastu.model.Degree;
import com.aastu.model.Department;
import com.aastu.model.Status;
import com.aastu.model.Student;

public class DatabaseTest {
  @Test
  void testCreateStudentAccount() {
    assertThrows(SQLException.class, () -> {
      Database
          .saveStudent(new Student("Tamirat", "Dejenie", "Wondimu", "ETS1518/14", "tamirat.dejenie@aastustudent.edu.et",
              "E", 3, College.ENGINEERING, Department.SOFTWARE_ENGINEERING, Degree.UNDERGRADUATE,
              AdmissionType.REGULAR),
              "-some-hashed-password");

    }, "Testing createAccount");


  }

  @Test
  void testDeleteApplication() {

  }

  @Test
  void testDeleteStudent() {
    assertDoesNotThrow(() -> {
      Database.deleteStudent("ETS1518/14");
    }, "Testing delete student");
  }

  @Test
  void testGetStatusString() {

  }

  @Test
  void testMain() {

  }

  @Test
  void testSaveApplication() {
    assertDoesNotThrow(() -> {
          Database.saveApplication(new ClearanceApplication("ETS1518/14", "Withdrawal Personal Reason", Status.PENDING));
    }, "Test save application database");
  }

  @Test
  void testSaveStudent() {

  }

  @Test
  void testUpdateApplicationReason() {

  }

  @Test
  void testUpdateStatus() {

  }
}
