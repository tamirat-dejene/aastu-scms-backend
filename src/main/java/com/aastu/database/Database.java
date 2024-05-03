package com.aastu.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.aastu.model.ClearanceApplication;
import com.aastu.model.Status;
import com.aastu.model.Student;
import com.aastu.utils.Util;

public class Database {

  private static String driver = Util.getEnv().getProperty("JDBC_DRIVER");

  public static void saveStudent(Student newStudent, String password) throws SQLException {
    String url = Util.getEnv().getProperty("JDBC_DRIVER");
    try (Connection conn = DriverManager.getConnection(url)) {
      PreparedStatement prprdstmt = conn
          .prepareStatement(
              "INSERT INTO Student(studentID, firstName, middleName, lastName, emailAddress, section, classYear, college, department, degree, admissionType) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
      prprdstmt.setString(1, newStudent.getIdNumber());
      prprdstmt.setString(2, newStudent.getFirstName());
      prprdstmt.setString(3, newStudent.getMiddleName());
      prprdstmt.setString(4, newStudent.getLastName());
      prprdstmt.setString(5, newStudent.getEmailAddress());
      prprdstmt.setString(6, newStudent.getSection());
      prprdstmt.setInt(7, newStudent.getClassYear());
      prprdstmt.setString(8, newStudent.getCollege().toString());
      prprdstmt.setString(9, newStudent.getDepartment().toString());
      prprdstmt.setString(10, newStudent.getDegree().toString());
      prprdstmt.setString(11, newStudent.getAdmissionType().toString());

      prprdstmt.executeUpdate();
      createStudentAccount(newStudent.getIdNumber(), password);
      prprdstmt.close();
      conn.close();
    } catch (SQLException e) {
      throw e;
    }
  }

  public static void deleteStudent(String idNumber) throws SQLException {
    try (Connection conn = DriverManager.getConnection(driver)) {
      PreparedStatement prpstmt = conn.prepareStatement("DELETE FROM Student WHERE studentId = ?");
      prpstmt.setString(1, idNumber);
      prpstmt.executeUpdate();

      prpstmt.close();
      conn.close();
    } catch (SQLException e) {
      throw e;
    }
  }

  public static int createStudentAccount(String studentId, String password) throws SQLException {
    try (Connection conn = DriverManager.getConnection(driver)) {
      PreparedStatement prprdStmt = conn
          .prepareStatement("INSERT INTO StudentAccount(studentId, userPassword) VALUES(?, ?)",
              Statement.RETURN_GENERATED_KEYS);
      prprdStmt.setString(1, studentId);
      prprdStmt.setString(2, password);
      int generatedKey = prprdStmt.executeUpdate();

      prprdStmt.clearParameters();
      prprdStmt.close();
      conn.close();

      return generatedKey;
    } catch (SQLException e) {
      throw e;
    }
  }

  public static int saveApplication(ClearanceApplication application) throws SQLException {
    try (Connection conn = DriverManager.getConnection(driver)) {
      PreparedStatement prprdStmt = conn
          .prepareStatement("INSERT INTO ClearanceApplication(studentId, reason, applicationStatus) VALUES(?, ?, ?)",
              Statement.RETURN_GENERATED_KEYS);
      prprdStmt.setString(1, application.getStudentId());
      prprdStmt.setString(2, application.getReason());
      prprdStmt.setString(3, application.getStatus().toString());

      int appNumber = prprdStmt.executeUpdate();

      prprdStmt.clearParameters();
      prprdStmt.close();
      conn.close();

      return appNumber;
    } catch (SQLException e) {
      throw e;
    }
  }

  public static void deleteApplication(int applicationNumber) throws SQLException {
    try (Connection conn = DriverManager.getConnection(driver)) {
      PreparedStatement prprdStmt = conn.prepareStatement("DELETE FROM ClearanceApplication WHERE applicationId = ?");
      prprdStmt.setInt(1, applicationNumber);

      prprdStmt.executeUpdate();
      prprdStmt.clearParameters();
      prprdStmt.close();
      conn.close();
    } catch (SQLException e) {
      throw e;
    }
  }

  public static void updateApplicationReason(int appNumber, String newReason) throws SQLException {
    try (Connection conn = DriverManager.getConnection(driver)) {
      PreparedStatement prpdStmt = conn
          .prepareStatement("UPDATE ClearanceApplication SET reason = ? WHERE applicationId = ?");
      prpdStmt.setString(1, newReason);
      prpdStmt.setInt(2, appNumber);
      prpdStmt.executeUpdate();

      prpdStmt.close();
      conn.close();
    } catch (SQLException e) {
      throw e;
    }
  }

  public static String getStatusString(int applicationNumber) throws SQLException {
    try (Connection conn = DriverManager.getConnection(driver)) {
      PreparedStatement prprdStmt = conn
          .prepareStatement("SELECT applicationStatus FROM ClearanceApplication WHERE applicationId = ?");
      prprdStmt.setInt(1, applicationNumber);
      ResultSet result = prprdStmt.executeQuery();

      while (result.next()) {
        return result.getString("applicationStatus");
      }
      return null;
    } catch (SQLException e) {
      throw e;
    }
  }

  public static void updateStatus(int appNumber, Status newStatus) throws SQLException {
    try (Connection conn = DriverManager.getConnection(driver)) {
      PreparedStatement prprdStmt = conn
          .prepareStatement("UPDATE ClearanceApplication SET applicationStatus = ? WHERE applicationId = ?");
      prprdStmt.setString(1, newStatus.toString());
      prprdStmt.setInt(2, appNumber);

      prprdStmt.executeUpdate();

      prprdStmt.clearParameters();
      prprdStmt.close();
      conn.close();
    } catch (SQLException e) {
      throw e;
    }
  }

  public static String getUserPassword(String userId) throws SQLException {
    try (Connection conn = DriverManager.getConnection(driver)) {
      PreparedStatement prpdStmt = conn.prepareStatement("SELECT userPassword FROM StudentAccount WHERE studentId = ?");
      prpdStmt.setString(1, userId);
      ResultSet resut = prpdStmt.executeQuery();
      while (resut.next()) {
        return resut.getString("userPassword");
      }
      return null;
    } catch (SQLException e) {
      throw e;
    }
  }

  public static void main(String[] args) {
  }
}