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