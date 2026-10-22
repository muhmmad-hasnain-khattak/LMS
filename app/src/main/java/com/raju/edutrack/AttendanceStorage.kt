package com.raju.edutrack

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

object AttendanceStorage {

    private const val PREFS_NAME = "edutrack_attendance_prefs"
    private const val KEY_ATTENDANCE = "attendance_records"

    fun loadRecords(context: Context): List<AttendanceRecord> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val rawJson = prefs.getString(KEY_ATTENDANCE, null) ?: return emptyList()
        return parseRecords(rawJson)
    }

    fun saveRecords(context: Context, records: List<AttendanceRecord>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val jsonArray = JSONArray()
        records.forEach { record ->
            val obj = JSONObject()
            obj.put("dateString", record.dateString)
            val entriesArray = JSONArray()
            record.entries.forEach { entry ->
                val entryObj = JSONObject()
                entryObj.put("studentId", entry.studentId)
                entryObj.put("studentName", entry.studentName)
                entryObj.put("status", entry.status.name)
                entryObj.put("note", entry.note)
                entriesArray.put(entryObj)
            }
            obj.put("entries", entriesArray)
            jsonArray.put(obj)
        }
        prefs.edit().putString(KEY_ATTENDANCE, jsonArray.toString()).apply()
    }

    private fun parseRecords(rawJson: String): List<AttendanceRecord> {
        val list = mutableListOf<AttendanceRecord>()
        try {
            val jsonArray = JSONArray(rawJson)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val dateString = obj.optString("dateString", "")
                val entriesArray = obj.optJSONArray("entries") ?: JSONArray()
                val entries = mutableListOf<AttendanceEntry>()
                for (j in 0 until entriesArray.length()) {
                    val entryObj = entriesArray.getJSONObject(j)
                    val studentId = entryObj.optString("studentId", "")
                    val studentName = entryObj.optString("studentName", "")
                    val statusStr = entryObj.optString("status", AttendanceStatus.PRESENT.name)
                    val status = try {
                        AttendanceStatus.valueOf(statusStr)
                    } catch (e: Exception) {
                        AttendanceStatus.PRESENT
                    }
                    val note = entryObj.optString("note", "")
                    entries.add(AttendanceEntry(studentId, studentName, status, note))
                }
                list.add(AttendanceRecord(dateString, entries))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }
}
