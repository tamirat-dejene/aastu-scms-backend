package com.aastu.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import com.aastu.utils.Util;

/**
    CREATE DATABASE clearance_management_db;
    USE clearance_management_db;

    CREATE TABLE Student (
        studentID VARCHAR(15) PRIMARY KEY,
        firstName VARCHAR(50) NOT NULL,
        middleName VARCHAR(50),
        lastName VARCHAR(50) NOT NULL,
        emailAddress VARCHAR(100) UNIQUE NOT NULL,
        section VARCHAR(5),
        classYear INT,
        college VARCHAR(50),
        department VARCHAR(50),
        degree VARCHAR(50),
        admissionType VARCHAR(50)
    );

    CREATE TABLE StudentAccount (
        accountId INT AUTO_INCREMENT PRIMARY KEY,
        studentId VARCHAR(15),
        userPassword VARCHAR(300) NOT NULL,
        
        FOREIGN KEY StudentAccount(studentId) REFERENCES Student(studentId) ON DELETE CASCADE
    );

    CREATE TABLE ClearanceApplication (
        applicationId INT AUTO_INCREMENT PRIMARY KEY,
        studentId VARCHAR(15) NOT NULL,
        reason VARCHAR(100) NOT NULL,
        applicationDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        applicationStatus ENUM('PENDING', 'APPROVED', 'DECLINED'),
        CHECK (applicationStatus IN ('PENDING', 'APPROVED', 'DECLINED')),
        
        FOREIGN KEY StudentAccount(studentId) REFERENCES Student(studentId)
    );
 */

public class Schema {
  static void createTables() throws SQLException {
    Connection connection = DriverManager.getConnection(Util.getEnv().getProperty("JDBC_DRIVER"));

    try (Statement statement = connection.createStatement()) {
      String createDatabaseSQL = "CREATE DATABASE clearance_management_db";
      statement.executeUpdate(createDatabaseSQL);
      System.out.println("Database created successfully.");
    }

    try (Statement statement = connection.createStatement()) {
      String createStudentTable = "CREATE TABLE Student (" +
          "studentID VARCHAR(15) PRIMARY KEY, " +
          "firstName VARCHAR(50) NOT NULL, " +
          "middleName VARCHAR(50), " +
          "lastName VARCHAR(50) NOT NULL, " +
          "emailAddress VARCHAR(100) UNIQUE NOT NULL, " +
          "section VARCHAR(5), " +
          "classYear INT, " +
          "college VARCHAR(50), " +
          "department VARCHAR(50), " +
          "degree VARCHAR(50), " +
          "admissionType VARCHAR(50) " +
          ")";
      statement.executeUpdate(createStudentTable);
      System.out.println("Student table created successfully.");
    }

    try (Statement statement = connection.createStatement()) {
      String createStudentAccountTable = "CREATE TABLE StudentAccount (" +
          "accountId INT AUTO_INCREMENT PRIMARY KEY, " +
          "studentId VARCHAR(15), " +
          "userPassword VARCHAR(300) NOT NULL, " +
          "FOREIGN KEY (studentId) REFERENCES Student(studentId) ON DELETE CASCADE " +
          ")";
      statement.executeUpdate(createStudentAccountTable);
      System.out.println("StudentAccount table created successfully.");
    }

    try (Statement statement = connection.createStatement()) {
      String createClearanceApplicationTable = "CREATE TABLE ClearanceApplication (" +
          "applicationId INT AUTO_INCREMENT PRIMARY KEY, " +
          "studentId VARCHAR(15) NOT NULL, " +
          "reason VARCHAR(100) NOT NULL, " +
          "applicationDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
          "applicationStatus ENUM('PENDING', 'APPROVED', 'DECLINED'), " +
          "CHECK (applicationStatus IN ('PENDING', 'APPROVED', 'DECLINED')), " +
          "FOREIGN KEY (studentId) REFERENCES Student(studentId) " +
          ")";
      statement.executeUpdate(createClearanceApplicationTable);
      System.out.println("ClearanceApplication table created successfully.");
    }

    try (Statement statement = connection.createStatement()) {
      String createNotificationTable = "CREATE TABLE Notification (" +
          "notificationId INTEGER PRIMARY KEY, " +
          "studentId VARCHAR(15), " +
          "notificationTitle VARCHAR(100), " +
          "notificationMessage VARCHAR(400) " +
          "notificationDate VARCHAR(50), " +
          "FOREIGN KEY Notification(studentId) REFERENCES Student(StudentId) ON DELETE CASCADE " +
          ")";
      statement.executeUpdate(createNotificationTable);
      System.out.println("Notification table created successfully.");
    }
  }

  static void dropSchema() throws SQLException {
    Connection connection = DriverManager.getConnection(Util.getEnv().getProperty("JDBC_DRIVER"));
    try (Statement statement = connection.createStatement()) {
      String createDatabaseSQL = "DROP DATABASE clearance_management_db";
      statement.executeUpdate(createDatabaseSQL);
      System.out.println("Database deleted successfully.");
    }
  }
  public static void main(String[] args) {  
  }
}
