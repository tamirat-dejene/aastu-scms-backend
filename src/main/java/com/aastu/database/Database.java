package com.aastu.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import com.aastu.model.ClearanceApplication;
import com.aastu.model.Notification;
import com.aastu.model.SecurePassword;
import com.aastu.model.Status;
import com.aastu.model.Student;
import com.aastu.model.ClearanceApp;
import com.aastu.utils.Util;

@SuppressWarnings("unused")
public class Database {

  private static String driver = Util.getEnv().getProperty("JDBC_DRIVER");

  public static void saveStudent(Student newStudent, String password) throws SQLException {
    try (Connection conn = DriverManager.getConnection(driver)) {
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
      createNotification(newStudent.getIdNumber(), new Notification("Successfully Signedup",
          "You have successfully signed up with student id, " + newStudent.getIdNumber(), Util.getDateString()));
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

  public static ArrayList<String> getStudentIds() throws SQLException {
    try (Connection conn = DriverManager.getConnection(driver)) {
      var stmt = conn.createStatement();

      var result = stmt.executeQuery("SELECT studentId FROM StudentAccount");

      var ids = new ArrayList<String>();
      while (result.next()) {
        ids.add(result.getString("studentId"));
      }
      return ids;
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

      prprdStmt.executeUpdate();

      // Retrieve the generated keys
      ResultSet generatedKeys = prprdStmt.getGeneratedKeys();
      int generatedId = 0;
      if (generatedKeys.next())
        generatedId = generatedKeys.getInt(1);

      prprdStmt.clearParameters();
      prprdStmt.close();
      conn.close();
      createDepratmentClearanceRequest(generatedId, application.getStudentId(), Status.PENDING);
      createNotification(application.getStudentId(), new Notification("Application submitted",
          "You have successfully submitted new clearance application with app#, AASTUSCMS-" + generatedId,
          Util.getDateString()));
      return generatedId;
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

  public static String getApplicantId(int applicationNumber) throws SQLException {
    try (Connection conn = DriverManager.getConnection(driver)) {
      PreparedStatement prprdStmt = conn
          .prepareStatement("SELECT studentId FROM ClearanceApplication WHERE applicationId = ?");
      prprdStmt.setInt(1, applicationNumber);
      ResultSet result = prprdStmt.executeQuery();

      while (result.next()) {
        return result.getString("studentId");
      }
      return null;
    } catch (SQLException e) {
      throw e;
    }
  }

  public static String getUserEmail(String idNumber) throws SQLException {
    try (Connection conn = DriverManager.getConnection(driver)) {
      PreparedStatement prprdStmt = conn
          .prepareStatement("SELECT emailAddress FROM student WHERE studentID = ?");
      prprdStmt.setString(1, idNumber);
      ResultSet result = prprdStmt.executeQuery();

      while (result.next()) {
        return result.getString("emailAddress");
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

      String applicantId = getApplicantId(appNumber);
      createNotification(applicantId, new Notification("Final application result",
          "Your application status is with id " + appNumber + " is updated to, " + newStatus.name(),
          Util.getDateString()));
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

  public static int updatePassword(String userId, String hashedPwd) throws SQLException {
    try (Connection conn = DriverManager.getConnection(driver)) {
      var prprdStmt = conn.prepareStatement("UPDATE studentaccount SET userPassword = ? WHERE studentId = ?");
      prprdStmt.setString(1, hashedPwd);
      prprdStmt.setString(2, userId);
      createNotification(userId, new Notification("Password changed successfully",
          "You have successfully updated your password. Good job.", Util.getDateString()));
      return prprdStmt.executeUpdate();
    } catch (SQLException e) {
      throw e;
    }
  }

  private static ArrayList<ClearanceApplication> getApplications(String query) {
    try (Connection conn = DriverManager.getConnection(driver)) {
      Statement stmt = conn.createStatement();
      ResultSet result = stmt.executeQuery(query);

      var clearanceList = new ArrayList<ClearanceApplication>();
      while (result.next()) {
        String studid = result.getString("studentId");
        String reason = result.getString("reason");
        Status status = Status.fromString(result.getString("applicationStatus"));
        clearanceList.add(new ClearanceApplication(studid, reason, status));
      }

      stmt.close();
      conn.close();
      return clearanceList;
    } catch (Exception e) {
      System.out.println(e.getMessage());
      return null;
    }
  }

  public static ArrayList<ClearanceApplication> getPendingApplications() {
    String query = "SELECT studentId, reason, applicationStatus FROM clearanceapplication";
    return Database.getApplications(query);
  }

  public static ArrayList<ClearanceApplication> getPendingApplications(String userId) {
    String query = "SELECT studentId, reason, applicationStatus FROM clearanceapplication WHERE applicationStatus = \"PENDING\" AND studentId = "
        + "\""
        + userId + "\"";
    return Database.getApplications(query);
  }

  public static int createNotification(String studentId, Notification notification) throws SQLException {
    String sql = "INSERT INTO Notification (studentId, notificationTitle, notificationMessage, notificationDate) VALUES (?, ?, ?, ?)";
    try (Connection conn = DriverManager.getConnection(driver);
        PreparedStatement pstmt = conn.prepareStatement(sql)) {
      pstmt.setString(1, studentId);
      pstmt.setString(2, notification.getTitle());
      pstmt.setString(3, notification.getMessage());
      pstmt.setString(4, notification.getDate());
      return pstmt.executeUpdate();
    } catch (SQLException e) {
      throw e;
    }
  }

  public static Notification readNotification(int notificationId) throws SQLException {
    String sql = "SELECT * FROM Notification WHERE notificationId = ?";

    try (Connection conn = DriverManager.getConnection(driver);
        PreparedStatement pstmt = conn.prepareStatement(sql)) {
      pstmt.setInt(1, notificationId);
      try (ResultSet rs = pstmt.executeQuery()) {
        if (rs.next()) {
          return new Notification(
              rs.getString("notificationTitle"),
              rs.getString("notificationMessage"),
              rs.getString("notificationDate"));
        } else {
          return null;
        }
      }
    } catch (SQLException e) {
      throw e;
    }
  }

  public static ArrayList<Notification> readAllNotifications() throws SQLException {
    String sql = "SELECT * FROM Notification";
    ArrayList<Notification> notifications = new ArrayList<>();

    try (Connection conn = DriverManager.getConnection(driver);
        PreparedStatement pstmt = conn.prepareStatement(sql);
        ResultSet rs = pstmt.executeQuery()) {
      while (rs.next()) {
        notifications.add(new Notification(
            rs.getString("notificationTitle"),
            rs.getString("notificationMessage"),
            rs.getString("notificationDate")));
      }
    } catch (SQLException e) {
      throw e;
    }
    return notifications;
  }

  public static boolean deleteNotification(int notificationId) throws SQLException {
    String sql = "DELETE FROM Notification WHERE notificationId = ?";

    try (Connection conn = DriverManager.getConnection(driver);
        PreparedStatement pstmt = conn.prepareStatement(sql)) {
      pstmt.setInt(1, notificationId);

      int affectedRows = pstmt.executeUpdate();
      return affectedRows > 0;
    } catch (SQLException e) {
      throw e;
    }
  }

  public static boolean updateNotification(int notificationId, Notification notification) throws SQLException {
    String sql = "UPDATE Notification SET notificationTitle = ?, notificationMessage = ?, notificationDate = ? WHERE notificationId = ?";

    try (Connection conn = DriverManager.getConnection(driver);
        PreparedStatement pstmt = conn.prepareStatement(sql)) {
      pstmt.setString(1, notification.getTitle());
      pstmt.setString(2, notification.getMessage());
      pstmt.setString(3, notification.getDate());
      pstmt.setInt(4, notificationId);

      int affectedRows = pstmt.executeUpdate();
      return affectedRows > 0;
    } catch (SQLException e) {
      throw e;
    }
  }

  public static ArrayList<Notification> getNotificationsByStudentId(String studentId) throws SQLException {
    String sql = "SELECT * FROM Notification WHERE studentId = ?";
    ArrayList<Notification> notifications = new ArrayList<>();

    try (Connection conn = DriverManager.getConnection(driver);
        PreparedStatement pstmt = conn.prepareStatement(sql)) {
      pstmt.setString(1, studentId);
      try (ResultSet rs = pstmt.executeQuery()) {
        while (rs.next()) {
          notifications.add(new Notification(rs.getString("notificationTitle"),
              rs.getString("notificationMessage"),
              rs.getString("notificationDate")));
        }
      }
    } catch (SQLException e) {
      throw e;
    }
    return notifications;
  }

  public static String getAdminPassword(String adminId) throws SQLException {
    String query = "SELECT adminPassword FROM ClearanceAdmin WHERE adminId = ?";
    try (Connection conn = DriverManager.getConnection(driver);
        PreparedStatement pstmt = conn.prepareStatement(query)) {
      pstmt.setString(1, adminId);
      ResultSet rs = pstmt.executeQuery();
      if (rs.next()) {
        return rs.getString("adminPassword");
      } else {
        return null;
      }
    }
  }

  public static String getAdminName(String adminId) throws SQLException {
    String query = "SELECT adminName FROM ClearanceAdmin WHERE adminId = ?";
    try (Connection conn = DriverManager.getConnection(driver);
        PreparedStatement pstmt = conn.prepareStatement(query)) {
      pstmt.setString(1, adminId);
      ResultSet rs = pstmt.executeQuery();
      if (rs.next()) {
        return rs.getString("adminName");
      } else {
        return null;
      }
    }
  }

  public static ArrayList<ClearanceApp> getClearanceApplicationsByStatusColumn(String statusColumn) throws Exception {
    ArrayList<ClearanceApp> applications = new ArrayList<>();
    String query = "SELECT ca.applicationId, ca.reason, ca.applicationDate, dcr.studentId, dcr." + statusColumn
        + " AS status " +
        "FROM ClearanceApplication ca " +
        "JOIN DepratmentClearanceRequest dcr ON ca.applicationId = dcr.applicationId";
    try (Connection conn = DriverManager.getConnection(driver);
        PreparedStatement stmt = conn.prepareStatement(query)) {

      try (ResultSet rs = stmt.executeQuery()) {
        while (rs.next()) {
          ClearanceApp application = new ClearanceApp();
          application.setApplicationId(rs.getInt("applicationId"));
          application.setReason(rs.getString("reason"));
          application.setDate(rs.getString("applicationDate"));
          application.setApplicantId(rs.getString("studentId"));
          application.setStatus(Status.fromString(rs.getString("status")));

          applications.add(application);
        }
      }
    } catch (Exception e) {
      throw e;
    }
    return applications;
  }

  public static void createDepratmentClearanceRequest(int applicationId, String studentId, Status status)
      throws SQLException {
    String sql = "INSERT INTO DepratmentClearanceRequest (applicationId, studentId, registrarStatus, dormitoryStatus, collegeAdminStatus, diningOfficeStatus) VALUES (?, ?, ?, ?, ?, ?)";

    try (Connection connection = DriverManager.getConnection(driver);
        PreparedStatement statement = connection.prepareStatement(sql)) {

      statement.setInt(1, applicationId);
      statement.setString(2, studentId);
      statement.setString(3, status.name());
      statement.setString(4, status.name());
      statement.setString(5, status.name());
      statement.setString(6, status.name());

      int rowsInserted = statement.executeUpdate();
      if (rowsInserted > 0) {
        // System.out.println("A new clearance request was inserted successfully!");
      }

    } catch (SQLException e) {
      System.err.println(e.getMessage());
      throw e;
    }
  }

  public static void updateStatusColumn(int applicationId, String statusColumn, String newStatus) throws Exception {
    String updateQuery = "UPDATE DepratmentClearanceRequest SET " + statusColumn + " = ? WHERE applicationId = ?";
    try (Connection conn = DriverManager.getConnection(driver);
        PreparedStatement stmt = conn.prepareStatement(updateQuery)) {

      stmt.setString(1, newStatus);
      stmt.setInt(2, applicationId);

      int rowsUpdated = stmt.executeUpdate();
      if (rowsUpdated > 0) {
        // System.out.println("The status was successfully updated.");
      } else {
        // System.out.println("No record found with the given applicationId.");
      }

      String dept = getAdminName(statusColumn);
      createNotification(getApplicantId(applicationId), new Notification("Application update from " + dept,
          "Your application with id " + applicationId + "status is updated to" + newStatus, Util.getDateString()));
      
      if (checkIfAllStatusesApproved(applicationId)) {
        updateStatus(applicationId, Status.fromString(newStatus));
      }
      
      
      String declinedDept = checkIfAnyStatusDeclined(applicationId);
      if (declinedDept != null) {
        updateStatus(applicationId, Status.DECLINED);
        createNotification(getApplicantId(applicationId),
            new Notification("Message From " + declinedDept,
                "Your application is not accepted. Please contact the department for further information",
                Util.getDateString()));
      }
    } catch (Exception e) {
      throw e;
    }
  }

  public static void updateStatusColumn(String statusColumn, String newStatus) throws Exception {
    var applications = getAllApplicationIds().iterator();
    while (applications.hasNext()) {
      int id = applications.next();
      updateStatusColumn(id, statusColumn, newStatus);
    }
  }

  private static void createClearanceAdmin(String adminName, String adminId, String adminPwd) throws SQLException {
    String query = "INSERT INTO ClearanceAdmin (adminName, adminId, adminPassword) VALUES (?, ?, ?)";
    try (Connection conn = DriverManager.getConnection(driver);
        PreparedStatement pstmt = conn.prepareStatement(query)) {
      pstmt.setString(1, adminName);
      pstmt.setString(2, adminId);
      pstmt.setString(3, adminPwd);
      pstmt.executeUpdate();
    }
  }

  public static boolean checkIfAllStatusesApproved(int applicationId) throws SQLException {
    String sql = "SELECT registrarStatus, dormitoryStatus, collegeAdminStatus, diningOfficeStatus " +
        "FROM DepratmentClearanceRequest " +
        "WHERE applicationId = ?";

    try (Connection connection = DriverManager.getConnection(driver);
        PreparedStatement statement = connection.prepareStatement(sql)) {

      // Set the application ID parameter
      statement.setInt(1, applicationId);

      try (ResultSet resultSet = statement.executeQuery()) {
        if (resultSet.next()) {
          String registrarStatus = resultSet.getString("registrarStatus");
          String dormitoryStatus = resultSet.getString("dormitoryStatus");
          String collegeAdminStatus = resultSet.getString("collegeAdminStatus");
          String diningOfficeStatus = resultSet.getString("diningOfficeStatus");

          return "APPROVED".equals(registrarStatus) &&
              "APPROVED".equals(dormitoryStatus) &&
              "APPROVED".equals(collegeAdminStatus) &&
              "APPROVED".equals(diningOfficeStatus);
        } else {
          // No record found for the given application ID
          return false;
        }
      }
    }
  }

  public static String checkIfAnyStatusDeclined(int applicationId) throws SQLException {
    String sql = "SELECT registrarStatus, dormitoryStatus, collegeAdminStatus, diningOfficeStatus " +
        "FROM DepratmentClearanceRequest " +
        "WHERE applicationId = ?";

    try (Connection connection = DriverManager.getConnection(driver);
        PreparedStatement statement = connection.prepareStatement(sql)) {

      statement.setInt(1, applicationId);

      try (ResultSet resultSet = statement.executeQuery()) {
        if (resultSet.next()) {
          String registrarStatus = resultSet.getString("registrarStatus");
          String dormitoryStatus = resultSet.getString("dormitoryStatus");
          String collegeAdminStatus = resultSet.getString("collegeAdminStatus");
          String diningOfficeStatus = resultSet.getString("diningOfficeStatus");

          if ("DECLINED".equals(registrarStatus)) {
            return "AASTU Registrar";
          } else if ("DECLINED".equals(dormitoryStatus)) {
            return "AASTU Dormitory";
          } else if ("AASTU DECLINED".equals(collegeAdminStatus)) {
            return "College Administration";
          } else if ("DECLINED".equals(diningOfficeStatus)) {
            return "AASTU Dining Office";
          }
        }
      }
    }
    return null;
  }

  public static ArrayList<Integer> getAllApplicationIds() throws SQLException {
    ArrayList<Integer> applicationIds = new ArrayList<>();
    String sql = "SELECT applicationId FROM DepratmentClearanceRequest";
    try (Connection connection = DriverManager.getConnection(driver);
        PreparedStatement statement = connection.prepareStatement(sql);
        ResultSet resultSet = statement.executeQuery()) {

      while (resultSet.next()) {
        int applicationId = resultSet.getInt("applicationId");
        applicationIds.add(applicationId);
      }
    }
    return applicationIds;
  }

  public static void main(String[] args) throws SQLException {

  }
}
