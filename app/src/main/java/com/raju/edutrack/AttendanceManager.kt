package com.raju.edutrack

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object AttendanceManager {

    val records = mutableStateListOf<AttendanceRecord>()
    private var isLoaded = false

    fun getTodayDateString(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }

    fun load(context: Context) {
        if (isLoaded) return
        records.clear()
        records.addAll(AttendanceStorage.loadRecords(context))
        isLoaded = true
    }

    fun getRecordForDate(dateString: String): AttendanceRecord? {
        return records.firstOrNull { it.dateString == dateString }
    }

    fun saveAttendanceForDate(
        context: Context,
        dateString: String,
        entries: List<AttendanceEntry>
    ) {
        val existingIndex = records.indexOfFirst { it.dateString == dateString }
        val newRecord = AttendanceRecord(dateString, entries)
        if (existingIndex >= 0) {
            records[existingIndex] = newRecord
        } else {
            records.add(newRecord)
        }
        AttendanceStorage.saveRecords(context, records)
    }

    fun getTodayPresentCount(): Int {
        val today = getTodayDateString()
        val record = getRecordForDate(today) ?: return 0
        return record.entries.count { it.status == AttendanceStatus.PRESENT }
    }
}
