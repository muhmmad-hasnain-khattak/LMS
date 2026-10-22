package com.raju.edutrack

enum class AttendanceStatus {
    PRESENT,
    ABSENT,
    LATE,
    NOT_MARKED
}

data class AttendanceEntry(
    val studentId: String,
    val studentName: String,
    val status: AttendanceStatus,
    val note: String = ""
)

data class AttendanceRecord(
    val dateString: String,
    val entries: List<AttendanceEntry> = emptyList()
)
