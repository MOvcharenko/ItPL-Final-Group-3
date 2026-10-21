package edu.team3.scheduling.domain;

import java.time.LocalDateTime;
import java.util.List;

/**
 * User — Abstract base actor resolved from Core Platform JWT (TR3.1, TR4.2).
 */
public abstract class User {
    private final String userId;
    private final String displayName;
    private final String email;
    private final Role role;

    public User(String userId, String displayName, String email, Role role) {
        this.userId = userId;
        this.displayName = displayName;
        this.email = email;
        this.role = role;
    }

    public String userId() { return userId; }
    public String displayName() { return displayName; }
    public String email() { return email; }
    public Role role() { return role; }
}

/**
 * Student — Read-only access to personal calendar and notices (TR3.2).
 */
public class Student extends User {
    private final String studentId;

    public Student(String userId, String displayName, String email, String studentId) {
        super(userId, displayName, email, Role.STUDENT);
        this.studentId = studentId;
    }

    public String studentId() { return studentId; }
}

/**
 * Instructor — Faculty actor who manages availability and equipment requests (TR3.3).
 */
public class Instructor extends User {
    private final String department;

    public Instructor(String userId, String displayName, String email, String department) {
        super(userId, displayName, email, Role.INSTRUCTOR);
        this.department = department;
    }

    public String department() { return department; }

    public Booking requestBooking(String bookingId, ResourceType type, Room room, Equipment equipment,
                                  LocalDateTime start, LocalDateTime end, String purpose) {
        return new Booking(bookingId, this, type, room, equipment, start, end, purpose);
    }
}

/**
 * Scheduler — Staff actor with timetable and catalog management rights (TR3.4).
 */
public class Scheduler extends User {
    private final List<String> authorizedDepartments;

    public Scheduler(String userId, String displayName, String email, List<String> authorizedDepartments) {
        super(userId, displayName, email, Role.SCHEDULER);
        this.authorizedDepartments = List.copyOf(authorizedDepartments);
    }

    public List<String> authorizedDepartments() { return authorizedDepartments; }
    public boolean isAuthorizedFor(String department) {
        return authorizedDepartments.contains(department);
    }
}