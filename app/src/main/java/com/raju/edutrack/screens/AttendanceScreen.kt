package com.raju.edutrack.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.raju.edutrack.*
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val students = StudentManager.students
    val batches = BatchManager.batches

    var calendar by remember { mutableStateOf(Calendar.getInstance()) }
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    val displayDateFormat = remember { SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault()) }

    val currentDateString = remember(calendar.timeInMillis) {
        dateFormat.format(calendar.time)
    }

    var selectedBatch by remember { mutableStateOf<String?>(null) }

    // Map of studentId -> AttendanceStatus
    val attendanceMap = remember(currentDateString) {
        mutableStateMapOf<String, AttendanceStatus>()
    }

    // Initialize attendance map from existing record or default to PRESENT
    LaunchedEffect(currentDateString, students.size) {
        attendanceMap.clear()
        val existingRecord = AttendanceManager.getRecordForDate(currentDateString)
        val existingMap = existingRecord?.entries?.associate { it.studentId to it.status }.orEmpty()

        students.forEach { student ->
            attendanceMap[student.id] = existingMap[student.id] ?: AttendanceStatus.PRESENT
        }
    }

    val filteredStudents = remember(students.toList(), selectedBatch) {
        if (selectedBatch.isNullOrBlank()) {
            students
        } else {
            students.filter { it.batchName?.equals(selectedBatch, ignoreCase = true) == true }
        }
    }

    val presentCount = filteredStudents.count { attendanceMap[it.id] == AttendanceStatus.PRESENT }
    val absentCount = filteredStudents.count { attendanceMap[it.id] == AttendanceStatus.ABSENT }
    val lateCount = filteredStudents.count { attendanceMap[it.id] == AttendanceStatus.LATE }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Attendance Manager") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            filteredStudents.forEach { student ->
                                attendanceMap[student.id] = AttendanceStatus.PRESENT
                            }
                        }
                    ) {
                        Text("All Present")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    val entries = students.map { student ->
                        AttendanceEntry(
                            studentId = student.id,
                            studentName = student.studentName,
                            status = attendanceMap[student.id] ?: AttendanceStatus.PRESENT
                        )
                    }
                    AttendanceManager.saveAttendanceForDate(context, currentDateString, entries)
                    Toast.makeText(context, "Attendance saved for $currentDateString", Toast.LENGTH_SHORT).show()
                },
                icon = { Icon(Icons.Default.Save, contentDescription = null) },
                text = { Text("Save Attendance") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            // Date Selector Bar
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            val newCal = Calendar.getInstance().apply {
                                timeInMillis = calendar.timeInMillis
                                add(Calendar.DAY_OF_MONTH, -1)
                            }
                            calendar = newCal
                        }
                    ) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = "Previous Day")
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                calendar = Calendar.getInstance()
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Event, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text(
                            text = displayDateFormat.format(calendar.time),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(
                        onClick = {
                            val newCal = Calendar.getInstance().apply {
                                timeInMillis = calendar.timeInMillis
                                add(Calendar.DAY_OF_MONTH, 1)
                            }
                            calendar = newCal
                        }
                    ) {
                        Icon(Icons.Default.ChevronRight, contentDescription = "Next Day")
                    }
                }
            }

            // Summary Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AttendanceStatChip(
                    label = "Present",
                    count = presentCount,
                    containerColor = Color(0xFFDCFCE7),
                    contentColor = Color(0xFF166534),
                    modifier = Modifier.weight(1f)
                )
                AttendanceStatChip(
                    label = "Absent",
                    count = absentCount,
                    containerColor = Color(0xFFFEE2E2),
                    contentColor = Color(0xFF991B1B),
                    modifier = Modifier.weight(1f)
                )
                AttendanceStatChip(
                    label = "Late",
                    count = lateCount,
                    containerColor = Color(0xFFFEF3C7),
                    contentColor = Color(0xFF92400E),
                    modifier = Modifier.weight(1f)
                )
            }

            // Batch Filter Tabs
            if (batches.isNotEmpty()) {
                SecondaryScrollableTabRow(
                    selectedTabIndex = if (selectedBatch == null) 0 else batches.indexOfFirst { it.name == selectedBatch } + 1,
                    edgePadding = 0.dp,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                    Tab(
                        selected = selectedBatch == null,
                        onClick = { selectedBatch = null },
                        text = { Text("All Batches") }
                    )
                    batches.forEach { batch ->
                        Tab(
                            selected = selectedBatch == batch.name,
                            onClick = { selectedBatch = batch.name },
                            text = { Text(batch.name) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (filteredStudents.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No students found",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 88.dp)
                ) {
                    items(filteredStudents, key = { it.id }) { student ->
                        val currentStatus = attendanceMap[student.id] ?: AttendanceStatus.PRESENT
                        StudentAttendanceCard(
                            studentName = student.studentName,
                            className = student.className,
                            batchName = student.batchName,
                            status = currentStatus,
                            onStatusChange = { newStatus ->
                                attendanceMap[student.id] = newStatus
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AttendanceStatChip(
    label: String,
    count: Int,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = containerColor,
        contentColor = contentColor,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

@Composable
private fun StudentAttendanceCard(
    studentName: String,
    className: String,
    batchName: String?,
    status: AttendanceStatus,
    onStatusChange: (AttendanceStatus) -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = studentName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Class $className",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    batchName?.takeIf { it.isNotBlank() }?.let { batch ->
                        Text(
                            text = "•  $batch",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                StatusToggleButton(
                    label = "P",
                    isSelected = status == AttendanceStatus.PRESENT,
                    selectedBg = Color(0xFF22C55E),
                    selectedText = Color.White,
                    onClick = { onStatusChange(AttendanceStatus.PRESENT) }
                )
                StatusToggleButton(
                    label = "A",
                    isSelected = status == AttendanceStatus.ABSENT,
                    selectedBg = Color(0xFFEF4444),
                    selectedText = Color.White,
                    onClick = { onStatusChange(AttendanceStatus.ABSENT) }
                )
                StatusToggleButton(
                    label = "L",
                    isSelected = status == AttendanceStatus.LATE,
                    selectedBg = Color(0xFFF59E0B),
                    selectedText = Color.White,
                    onClick = { onStatusChange(AttendanceStatus.LATE) }
                )
            }
        }
    }
}

@Composable
private fun StatusToggleButton(
    label: String,
    isSelected: Boolean,
    selectedBg: Color,
    selectedText: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(
                if (isSelected) selectedBg else MaterialTheme.colorScheme.surfaceVariant
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) selectedText else MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelLarge
        )
    }
}
