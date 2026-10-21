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