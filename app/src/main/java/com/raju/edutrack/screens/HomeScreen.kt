package com.raju.edutrack.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.raju.edutrack.AppSettings
import com.raju.edutrack.AttendanceManager
import com.raju.edutrack.BatchManager
import com.raju.edutrack.BottomNavItem
import com.raju.edutrack.StudentManager
import com.raju.edutrack.effectiveMonthsUnpaid
import java.util.Calendar

@Composable
fun HomeScreen(
    navController: NavController,
    onOpenStudents: () -> Unit = {
        navController.navigate(BottomNavItem.Students.route)
    },
    onOpenBatches: () -> Unit = {
        navController.navigate(BottomNavItem.Batches.route)
    },
    onOpenAttendance: () -> Unit = {
        navController.navigate("attendance")
    }
) {

    val students = StudentManager.students

    val dashboardStats by remember {
        derivedStateOf {
            val studentCount = students.size
            val schoolCount = students
                .map { student -> student.schoolName }
                .filter { school -> school.isNotBlank() }
                .distinctBy { school -> school.lowercase() }
                .size
            val batchCount = BatchManager.batches.size
            val pendingCount = students.count { student ->
                val monthlyFee =
                    student.feeDueAmount ?: if (
                        AppSettings.autoClassFeesEnabled.value
                    ) {
                        AppSettings.parseClassFeeAmount(student.className)
                    } else {
                        null
                    }
                effectiveMonthsUnpaid(
                    student = student,
                    countFeeFromJoinDate =
                        AppSettings.countFeeFromJoinDate.value,
                    monthlyFee = monthlyFee
                ) > 0
            }
            val todayPresent = AttendanceManager.getTodayPresentCount()
            DashboardStats(
                studentCount = studentCount,
                schoolCount = schoolCount,
                batchCount = batchCount,
                pendingCount = pendingCount,
                todayPresent = todayPresent
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        DashboardHeader()

        Spacer(
            modifier = Modifier.height(24.dp)
        )

        Text(
            text = "Overview",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement =
                Arrangement.spacedBy(12.dp)
        ) {

            OverviewCard(
                onClick = onOpenStudents,
                title = "Students",
                value = dashboardStats.studentCount.toString(),
                icon = {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                modifier = Modifier.weight(1f)
            )

            OverviewCard(
                onClick = {
                    navController.navigate(
                        "overview/Schools"
                    )
                },
                title = "Schools",
                value = dashboardStats.schoolCount.toString(),
                icon = {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement =
                Arrangement.spacedBy(12.dp)
        ) {

            OverviewCard(
                onClick = onOpenBatches,
                title = "Batch",
                value = dashboardStats.batchCount.toString(),
                icon = {
                    Icon(
                        imageVector = Icons.Default.Groups,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                modifier = Modifier.weight(1f)
            )

            OverviewCard(
                onClick = {
                    navController.navigate(
                        "overview/Dues"
                    )
                },
                title = "Dues",
                value = dashboardStats.pendingCount.toString(),
                icon = {
                    Icon(
                        imageVector = Icons.Default.Payments,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        // Attendance Quick Button / Banner Card
        AttendanceOverviewCard(
            onClick = onOpenAttendance,
            todayPresent = dashboardStats.todayPresent,
            totalStudents = dashboardStats.studentCount
        )
    }
}

private data class DashboardStats(
    val studentCount: Int,
    val schoolCount: Int,
    val batchCount: Int,
    val pendingCount: Int,
    val todayPresent: Int
)

@Composable
fun DashboardHeader() {

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(
                MaterialTheme.colorScheme.primaryContainer
            )
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement =
                Arrangement.SpaceBetween
        ) {

            Column {
                val currentHour = Calendar
                    .getInstance()
                    .get(Calendar.HOUR_OF_DAY)

                val greeting = when {

                    currentHour < 12 -> "Good Morning 👋"

                    currentHour < 17 -> "Good Afternoon ☀️"

                    currentHour < 21 -> "Good Evening 👋"

                    else -> "Good Night 🌙"

                }

                Text(
                    text = greeting,
                    style =
                        MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                Text(
                    text = "EduHub Dashboard",
                    style =
                        MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Text(
                text = "Manage Students Professionally",
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f)
            )
        }
    }
}

@Composable
fun AttendanceOverviewCard(
    onClick: () -> Unit,
    todayPresent: Int,
    totalStudents: Int
) {
    ElevatedCard(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = 6.dp
        ),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary,
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.EventAvailable,
                            contentDescription = "Attendance Icon"
                        )
                    }
                }

                Column {
                    Text(
                        text = "Attendance",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (totalStudents > 0) "$todayPresent / $totalStudents Marked Present" else "Tap to Mark Daily Attendance",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                    )
                }
            }

            Button(
                onClick = onClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                )
            ) {
                Text("Mark")
            }
        }
    }
}

@Composable
fun OverviewCard(
    title: String,
    value: String,
    icon: @Composable () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {

    ElevatedCard(
        onClick = onClick,
        modifier = modifier.height(140.dp),
        elevation =
            CardDefaults.elevatedCardElevation(
                defaultElevation = 6.dp
            )
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),

            horizontalAlignment =
                Alignment.CenterHorizontally,

            verticalArrangement =
                Arrangement.Center
        ) {

            icon()

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            Text(
                text = value,
                style =
                    MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(
                modifier = Modifier.height(6.dp)
            )

            Text(text = title)
        }
    }
}
