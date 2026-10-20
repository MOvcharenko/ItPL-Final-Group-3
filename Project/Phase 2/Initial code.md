Here is the refactored initial code, restructured to focus strictly on the **Actors** and **Entities** (along with all supporting enumerations) as specified in the Phase 2 Class Diagram, CRC cards, and Object Catalog.

  

### Package: `edu.team3.scheduling.domain`

#### 1. Enumerations

Java

```
package edu.team3.scheduling.domain;

public enum Role {
    STUDENT, INSTRUCTOR, SCHEDULER
}

public enum TermStatus {
    DRAFT, PUBLISHED
}

public enum MeetingPattern {
    WEEKLY, ONE_TIME, EXAM
}

public enum ResourceType {
    ROOM, EQUIPMENT
}

public enum ResourceStatus {
    AVAILABLE, MAINTENANCE, CHECKED_OUT
}

public enum BookingStatus {
    PENDING, APPROVED, CANCELLED
}

public enum AvailabilityStatus {
    OPEN, PREFER, UNAVAILABLE
}

public enum ConflictType {
    ROOM_DOUBLE_BOOKING,
    INSTRUCTOR_OVERLAP,
    INSTRUCTOR_UNAVAILABLE,
    EQUIPMENT_DOUBLE_BOOKING,
    CAPACITY_EXCEEDED,
    STUDENT_GROUP_CLASH
}

public enum Severity {
    HIGH, MEDIUM, LOW
}

public enum ResolutionState {
    OPEN, RESOLVED, DISMISSED
}

public enum InsightAxis {
    INSTRUCTOR_UTILIZATION, ROOM_CAPACITY
}

public enum ImportType {
    READY_MADE_SCHEDULE, SUBJECT_LIST, ROOMS, EQUIPMENT
}

public enum RowOutcome {
    ADDED, NEEDS_REVIEW
}

public enum RoomType {
    LECTURE_HALL, LAB, SEMINAR_ROOM
}
```

#### 2. Actor Classes

Java

```
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
```

#### 3. Domain Entities

Java

```
package edu.team3.scheduling.domain;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Term — Academic term entity (TR2.1, NFR3.4).
 */
public class Term {
    private final String termId;
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
    private TermStatus status;

    public Term(String termId, String name, LocalDate startDate, LocalDate endDate, TermStatus status) {
        this.termId = termId;
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
    }

    public boolean publish(boolean confirmed) {
        if (this.status == TermStatus.PUBLISHED) return true;
        this.status = TermStatus.PUBLISHED;
        return true;
    }

    public Term copy(String newTermId, String newName) {
        return new Term(newTermId, newName, this.startDate, this.endDate, TermStatus.DRAFT);
    }

    public boolean isLocked() { return status == TermStatus.PUBLISHED; }
    public String termId() { return termId; }
    public String name() { return name; }
    public TermStatus status() { return status; }
}

/**
 * Subject — Course definition independent of term placement (TR2.2).
 */
public class Subject {
    private final String subjectId;
    private final String courseCode;
    private final String title;
    private final int creditHours;
    private final RoomType defaultRoomType;
    private final List<String> typicalEquipment;

    public Subject(String subjectId, String courseCode, String title, int creditHours,
                   RoomType defaultRoomType, List<String> typicalEquipment) {
        this.subjectId = subjectId;
        this.courseCode = courseCode;
        this.title = title;
        this.creditHours = creditHours;
        this.defaultRoomType = defaultRoomType;
        this.typicalEquipment = typicalEquipment == null ? List.of() : List.copyOf(typicalEquipment);
    }

    public String subjectId() { return subjectId; }
    public String courseCode() { return courseCode; }
    public String title() { return title; }
    public int creditHours() { return creditHours; }
    public RoomType defaultRoomType() { return defaultRoomType; }
    public List<String> typicalEquipment() { return typicalEquipment; }
}

/**
 * Section — Scheduled instance of a Subject (TR2.3).
 */
public class Section {
    private final String sectionId;
    private final String courseCode;
    private final String termId;
    private final MeetingPattern pattern;
    private DayOfWeek day;
    private LocalTime start;
    private LocalTime end;
    private Room room;
    private final List<Instructor> instructors = new ArrayList<>();
    private final List<Equipment> requestedEquipment = new ArrayList<>();
    private int enrolledCount;

    public Section(String sectionId, String courseCode, String termId, MeetingPattern pattern, int enrolledCount) {
        this.sectionId = sectionId;
        this.courseCode = courseCode;
        this.termId = termId;
        this.pattern = pattern;
        this.enrolledCount = enrolledCount;
    }

    public void place(DayOfWeek day, LocalTime start, LocalTime end, Room room, List<Instructor> instructors) {
        if (!start.isBefore(end)) throw new IllegalArgumentException("Start time must be before end time");
        if (instructors == null || instructors.isEmpty()) {
            throw new IllegalArgumentException("A section needs at least one instructor (TR2.3)");
        }
        this.day = day;
        this.start = start;
        this.end = end;
        this.room = room;
        this.instructors.clear();
        this.instructors.addAll(instructors);
    }

    public void assignRoom(Room r) { this.room = r; }
    public void addInstructor(Instructor i) { if (!instructors.contains(i)) instructors.add(i); }
    public void addEquipment(Equipment e) { if (!requestedEquipment.contains(e)) requestedEquipment.add(e); }

    public boolean isPlaced() { return day != null && room != null; }

    public boolean overlaps(Section o) {
        return isPlaced() && o.isPlaced() && termId.equals(o.termId) && day == o.day
                && start.isBefore(o.end) && o.start.isBefore(end);
    }

    public String sectionId() { return sectionId; }
    public String courseCode() { return courseCode; }
    public String termId() { return termId; }
    public MeetingPattern pattern() { return pattern; }
    public DayOfWeek day() { return day; }
    public LocalTime start() { return start; }
    public LocalTime end() { return end; }
    public Room room() { return room; }
    public List<Instructor> instructors() { return List.copyOf(instructors); }
    public List<Equipment> requestedEquipment() { return List.copyOf(requestedEquipment); }
    public int enrolledCount() { return enrolledCount; }
    public void setEnrolledCount(int n) { this.enrolledCount = n; }

    public String describe() {
        return courseCode + " (" + day + " " + start + "-" + end + ", " + (room == null ? "no room" : room.label()) + ")";
    }
}

/**
 * Room — Catalog entry for physical campus space (TR2.4).
 */
public class Room {
    private final String roomId;
    private final String building;
    private final int capacity;
    private final RoomType roomType;
    private final List<String> fixedEquipment;
    private ResourceStatus status;

    public Room(String roomId, String building, int capacity, RoomType roomType,
                List<String> fixedEquipment, ResourceStatus status) {
        this.roomId = roomId;
        this.building = building;
        this.capacity = capacity;
        this.roomType = roomType;
        this.fixedEquipment = fixedEquipment == null ? List.of() : List.copyOf(fixedEquipment);
        this.status = status;
    }

    public boolean isBookable() { return status == ResourceStatus.AVAILABLE; }
    public boolean fitsCapacity(int n) { return capacity >= n; }
    public void setStatus(ResourceStatus status) { this.status = status; }

    public String roomId() { return roomId; }
    public String building() { return building; }
    public int capacity() { return capacity; }
    public RoomType roomType() { return roomType; }
    public ResourceStatus status() { return status; }
    public String label() { return building + " " + roomId; }
}

/**
 * Equipment — Catalog entry for portable AV/hardware items (TR2.5).
 */
public class Equipment {
    private final String equipmentId;
    private final String itemType;
    private final int unitCount;
    private final String homeLocation;
    private ResourceStatus status;

    public Equipment(String equipmentId, String itemType, int unitCount, String homeLocation, ResourceStatus status) {
        this.equipmentId = equipmentId;
        this.itemType = itemType;
        this.unitCount = unitCount;
        this.homeLocation = homeLocation;
        this.status = status;
    }

    public boolean isAvailable() { return status == ResourceStatus.AVAILABLE; }
    public void setStatus(ResourceStatus status) { this.status = status; }

    public String equipmentId() { return equipmentId; }
    public String itemType() { return itemType; }
    public int unitCount() { return unitCount; }
    public ResourceStatus status() { return status; }
}

/**
 * Booking — Resource reservation request in shared queue (TR2.6).
 */
public class Booking {
    private final String bookingId;
    private final User requester;
    private final ResourceType resourceType;
    private final Room room;
    private final Equipment equipment;
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;
    private final String purpose;
    private BookingStatus status;

    public Booking(String bookingId, User requester, ResourceType resourceType, Room room,
                   Equipment equipment, LocalDateTime startTime, LocalDateTime endTime, String purpose) {
        if (resourceType == ResourceType.ROOM && room == null) throw new IllegalArgumentException("Room required");
        if (resourceType == ResourceType.EQUIPMENT && equipment == null) throw new IllegalArgumentException("Equipment required");
        this.bookingId = bookingId;
        this.requester = requester;
        this.resourceType = resourceType;
        this.room = room;
        this.equipment = equipment;
        this.startTime = startTime;
        this.endTime = endTime;
        this.purpose = purpose;
        this.status = BookingStatus.PENDING;
    }

    public boolean approve() {
        if (status != BookingStatus.PENDING) return false;
        this.status = BookingStatus.APPROVED;
        return true;
    }

    public void cancel(String note) { this.status = BookingStatus.CANCELLED; }

    public String bookingId() { return bookingId; }
    public User requester() { return requester; }
    public ResourceType resourceType() { return resourceType; }
    public Room room() { return room; }
    public Equipment equipment() { return equipment; }
    public LocalDateTime startTime() { return startTime; }
    public LocalDateTime endTime() { return endTime; }
    public BookingStatus status() { return status; }
}

/**
 * InstructorAvailability — Faculty availability submission block (TR2.7).
 */
public class InstructorAvailability {
    private final String availabilityId;
    private final String instructorId;
    private final String termId;
    private final DayOfWeek dayOfWeek;
    private final LocalTime startTime;
    private final LocalTime endTime;
    private final AvailabilityStatus status;

    public InstructorAvailability(String availabilityId, String instructorId, String termId,
                                DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime, AvailabilityStatus status) {
        this.availabilityId = availabilityId;
        this.instructorId = instructorId;
        this.termId = termId;
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
    }

    public boolean isUnavailable() { return status == AvailabilityStatus.UNAVAILABLE; }
    public String availabilityId() { return availabilityId; }
    public String instructorId() { return instructorId; }
    public String termId() { return termId; }
    public DayOfWeek day() { return dayOfWeek; }
    public LocalTime start() { return startTime; }
    public LocalTime end() { return endTime; }
    public AvailabilityStatus status() { return status; }
}

/**
 * Conflict — Detected schedule conflict record (TR2.8).
 */
public class Conflict {
    private final String conflictId;
    private final ConflictType type;
    private final Severity severity;
    private final String explanation;
    private ResolutionState resolutionState;
    private final List<String> sectionIds;
    private final String bookingId;
    private AuditLogEntry dismissalAudit;

    public Conflict(String conflictId, ConflictType type, Severity severity, String explanation,
                    List<String> sectionIds, String bookingId) {
        this.conflictId = conflictId;
        this.type = type;
        this.severity = severity;
        this.explanation = explanation;
        this.resolutionState = ResolutionState.OPEN;
        this.sectionIds = sectionIds == null ? List.of() : List.copyOf(sectionIds);
        this.bookingId = bookingId;
    }

    public void resolve() { this.resolutionState = ResolutionState.RESOLVED; }

    public AuditLogEntry dismiss(User user, String reason) {
        this.resolutionState = ResolutionState.DISMISSED;
        this.dismissalAudit = new AuditLogEntry("AUD-" + conflictId, LocalDateTime.now(), reason, user.userId());
        return this.dismissalAudit;
    }

    public String conflictId() { return conflictId; }
    public ConflictType type() { return type; }
    public Severity severity() { return severity; }
    public String explanation() { return explanation; }
    public ResolutionState resolutionState() { return resolutionState; }
    public List<String> sectionIds() { return sectionIds; }
}

/**
 * AuditLogEntry — Record of overridden/dismissed warnings (NFR3.3).
 */
public record AuditLogEntry(String entryId, LocalDateTime timestamp, String reason, String dismissedByUserId) {}

/**
 * InsightSuggestion — Advisory utilization/capacity suggestion (TR2.9, NFR4.4).
 */
public class InsightSuggestion {
    private final String suggestionId;
    private final InsightAxis axis;
    private final String message;
    private boolean dismissed;
    private final String termId;
    private final String targetId; // roomId or instructorId

    public InsightSuggestion(String suggestionId, InsightAxis axis, String message, String termId, String targetId) {
        this.suggestionId = suggestionId;
        this.axis = axis;
        this.message = message;
        this.dismissed = false;
        this.termId = termId;
        this.targetId = targetId;
    }

    public void dismiss() { this.dismissed = true; }
    public String suggestionId() { return suggestionId; }
    public InsightAxis axis() { return axis; }
    public String message() { return message; }
    public boolean isDismissed() { return dismissed; }
}

/**
 * ImportBatch & ImportRow — File import records (TR2.10, FR3.6).
 */
public record ImportRow(int rowNumber, RowOutcome outcome, String reason) {}

public class ImportBatch {
    private final String batchId;
    private final String fileName;
    private final ImportType importType;
    private final LocalDateTime importedAt;
    private final String schedulerId;
    private final List<ImportRow> rows = new ArrayList<>();

    public ImportBatch(String batchId, String fileName, ImportType importType, String schedulerId) {
        this.batchId = batchId;
        this.fileName = fileName;
        this.importType = importType;
        this.importedAt = LocalDateTime.now();
        this.schedulerId = schedulerId;
    }

    public void addRowOutcome(int rowNum, RowOutcome outcome, String reason) {
        rows.add(new ImportRow(rowNum, outcome, reason));
    }

    public List<ImportRow> rows() { return List.copyOf(rows); }
}

/**
 * Notice — User notification entity (TR2.11).
 */
public class Notice {
    private final String noticeId;
    private final String recipientUserId;
    private final String triggerEvent;
    private final String message;
    private final LocalDateTime createdAt;
    private boolean isRead;

    public Notice(String noticeId, String recipientUserId, String triggerEvent, String message) {
        this.noticeId = noticeId;
        this.recipientUserId = recipientUserId;
        this.triggerEvent = triggerEvent;
        this.message = message;
        this.createdAt = LocalDateTime.now();
        this.isRead = false;
    }

    public void markAsRead() { this.isRead = true; }
    public String noticeId() { return noticeId; }
    public String recipientUserId() { return recipientUserId; }
    public String message() { return message; }
    public boolean isRead() { return isRead; }
}
```

### 4. Domain Layer Verification Test

Java

```
package edu.team3.scheduling;

import edu.team3.scheduling.domain.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public class DomainModelTests {
    public static void main(String[] args) {
        System.out.println("=== Running Domain Model Integrity Checks ===");

        // 1. Actors & Access Checks
        Scheduler scheduler = new Scheduler("U01", "Maya Brooks", "m.brooks@univ.edu", List.of("CS", "MATH"));
        Instructor instructor = new Instructor("U02", "L. Bianchi", "l.bianchi@univ.edu", "MATH");
        Student student = new Student("U03", "Jordan Ellis", "j.ellis@univ.edu", "S10024");

        assert scheduler.role() == Role.SCHEDULER : "Scheduler role mismatch";
        assert scheduler.isAuthorizedFor("MATH") : "Authorization check failed";

        // 2. Entities & Placement Checks
        Term term = new Term("T-2026-FALL", "Fall 2026", LocalDate.of(2026, 9, 1), LocalDate.of(2026, 12, 20), TermStatus.DRAFT);
        Room room = new Room("204", "Hale Hall", 120, RoomType.LECTURE_HALL, List.of("Projector"), ResourceStatus.AVAILABLE);
        Section section = new Section("SEC-101", "MATH 210", term.termId(), MeetingPattern.WEEKLY, 25);

        section.place(DayOfWeek.TUESDAY, LocalTime.of(10, 0), LocalTime.of(11, 15), room, List.of(instructor));
        assert section.isPlaced() : "Section placement state check failed";

        // 3. Booking & Approval Flow
        Booking booking = instructor.requestBooking("B-001", ResourceType.ROOM, room, null,
                LocalDateTime.of(2026, 10, 20, 14, 0), LocalDateTime.of(2026, 10, 20, 16, 0), "Guest Lecture");
        assert booking.status() == BookingStatus.PENDING : "Booking initial state failed";
        booking.approve();
        assert booking.status() == BookingStatus.APPROVED : "Booking approval failed";

        // 4. Conflict Dismissal Audit Log
        Conflict conflict = new Conflict("C-101", ConflictType.ROOM_DOUBLE_BOOKING, Severity.HIGH,
                "Room collision detected", List.of(section.sectionId()), null);
        AuditLogEntry audit = conflict.dismiss(scheduler, "Overridden for special campus event");

        assert conflict.resolutionState() == ResolutionState.DISMISSED : "Conflict dismissal failed";
        assert audit.dismissedByUserId().equals(scheduler.userId()) : "Audit log user tracking failed";

        System.out.println("All 4 domain model checks passed successfully!");
    }
}
```

Would you like to move on to updating the service layer (`TimetableService`, `BookingService`, `ConflictDetectionEngine`, etc.) to connect with these expanded domain models next?