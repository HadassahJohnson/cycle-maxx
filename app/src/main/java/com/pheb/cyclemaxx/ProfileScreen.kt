package com.pheb.cyclemaxx

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.records.MenstruationPeriodRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.*

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val uid = auth.currentUser?.uid

    var userProfile by remember { mutableStateOf<UserProfile?>(null) }
    var menstrualDays by remember { mutableStateOf<Set<LocalDate>>(emptySet()) }
    var showExercisesDialog by remember { mutableStateOf(false) }
    var editingField by remember { mutableStateOf<String?>(null) }
    var editValue by remember { mutableStateOf("") }

    val exercises = listOf("HIIT", "Calisthenics", "Yoga", "Pilates", "Cardio", "Machine Weights", "Cable Exercises", "Free Weights")

    LaunchedEffect(uid) {
        if (uid != null) {
            db.collection("users").document(uid).addSnapshotListener { document, _ ->
                userProfile = document?.toObject(UserProfile::class.java)
            }

            // Fetch Menstrual Records for Calendar
            try {
                val healthConnectClient = HealthConnectClient.getOrCreate(context)
                val response = healthConnectClient.readRecords(
                    ReadRecordsRequest(
                        recordType = MenstruationPeriodRecord::class,
                        timeRangeFilter = TimeRangeFilter.before(Instant.now())
                    )
                )
                val days = mutableSetOf<LocalDate>()
                response.records.forEach { record ->
                    var current = record.startTime.atZone(ZoneId.systemDefault()).toLocalDate()
                    val end = record.endTime.atZone(ZoneId.systemDefault()).toLocalDate()
                    while (!current.isAfter(end)) {
                        days.add(current)
                        current = current.plusDays(1)
                    }
                }
                menstrualDays = days
            } catch (e: Exception) {
                // Handle or ignore Health Connect errors
            }
        }
    }

    // Edit Info Dialog
    if (editingField != null) {
        BasicAlertDialog(onDismissRequest = { editingField = null }) {
            Surface(
                modifier = Modifier.wrapContentWidth().wrapContentHeight(),
                shape = MaterialTheme.shapes.large,
                tonalElevation = AlertDialogDefaults.TonalElevation
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    val isMetric = userProfile?.unitSystem == "Metric"
                    val label = when(editingField) {
                        "Height" -> if (isMetric) "Height (cm)" else "Height (in)"
                        "Weight" -> if (isMetric) "Weight (kg)" else "Weight (lb)"
                        else -> editingField!!
                    }
                    
                    Text(text = "Edit $editingField", style = MaterialTheme.typography.headlineSmall)
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = editValue,
                        onValueChange = { editValue = it },
                        label = { Text(label) }
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { editingField = null }) { Text("Cancel") }
                        TextButton(onClick = {
                            userProfile?.let { profile ->
                                val updatedProfile = when (editingField) {
                                    "Name" -> profile.copy(name = editValue)
                                    "Age" -> profile.copy(age = editValue.toIntOrNull() ?: profile.age)
                                    "Height" -> {
                                        val input = editValue.toDoubleOrNull() ?: profile.height
                                        profile.copy(height = if (isMetric) input else UnitConverter.inchesToCm(input))
                                    }
                                    "Weight" -> {
                                        val input = editValue.toDoubleOrNull() ?: profile.weight
                                        profile.copy(weight = if (isMetric) input else UnitConverter.lbsToKg(input))
                                    }
                                    else -> profile
                                }
                                uid?.let { db.collection("users").document(it).set(updatedProfile) }
                            }
                            editingField = null
                        }) { Text("Save") }
                    }
                }
            }
        }
    }

    if (showExercisesDialog && userProfile != null) {
        ExerciseSelectionDialog(
            currentSelected = userProfile!!.preferredExercises,
            allOptions = exercises,
            onDismiss = { showExercisesDialog = false },
            onSave = { selected ->
                val updated = userProfile!!.copy(preferredExercises = selected)
                uid?.let { db.collection("users").document(it).set(updated) }
                showExercisesDialog = false
            }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text("Profile") })
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text("Cycle Calendar", style = MaterialTheme.typography.titleMedium)
                MenstrualCalendar(menstrualDays)
            }

            item {
                HorizontalDivider()
                Text("Preferences", style = MaterialTheme.typography.titleMedium, modifier = Modifier.fillMaxWidth())
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Unit System (${userProfile?.unitSystem ?: "Metric"})")
                    Switch(
                        checked = userProfile?.unitSystem == "Metric",
                        onCheckedChange = { isMetric ->
                            val newSystem = if (isMetric) "Metric" else "Imperial"
                            uid?.let { db.collection("users").document(it).update("unitSystem", newSystem) }
                        }
                    )
                }
            }

            item {
                HorizontalDivider()
                Text("User Information", style = MaterialTheme.typography.titleMedium, modifier = Modifier.fillMaxWidth())
                
                val isMetric = userProfile?.unitSystem == "Metric"
                
                ProfileInfoRow("Name", userProfile?.name ?: "--") { 
                    editingField = "Name"
                    editValue = userProfile?.name ?: ""
                }
                ProfileInfoRow("Age", "${userProfile?.age ?: "--"}") { 
                    editingField = "Age"
                    editValue = userProfile?.age?.toString() ?: ""
                }
                
                val displayHeight = if (isMetric) {
                    "${userProfile?.height ?: "--"} cm"
                } else {
                    "${UnitConverter.format(UnitConverter.cmToInches(userProfile?.height ?: 0.0))} in"
                }
                ProfileInfoRow("Height", displayHeight) { 
                    editingField = "Height"
                    editValue = if (isMetric) {
                        userProfile?.height?.toString() ?: ""
                    } else {
                        UnitConverter.format(UnitConverter.cmToInches(userProfile?.height ?: 0.0))
                    }
                }

                val displayWeight = if (isMetric) {
                    "${userProfile?.weight ?: "--"} kg"
                } else {
                    "${UnitConverter.format(UnitConverter.kgToLbs(userProfile?.weight ?: 0.0))} lb"
                }
                ProfileInfoRow("Weight", displayWeight) {
                    editingField = "Weight"
                    editValue = if (isMetric) {
                        userProfile?.weight?.toString() ?: ""
                    } else {
                        UnitConverter.format(UnitConverter.kgToLbs(userProfile?.weight ?: 0.0))
                    }
                }
            }

            item {
                HorizontalDivider()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Preferred Exercises", style = MaterialTheme.typography.titleMedium)
                    IconButton(onClick = { showExercisesDialog = true }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Exercises")
                    }
                }
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    userProfile?.preferredExercises?.forEach { exercise ->
                        AssistChip(
                            onClick = { },
                            label = { Text(exercise) }
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = {
                        auth.signOut()
                        navController.navigate("auth") {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Log Out", color = Color.White)
                }
            }
        }
    }
}

@Composable
fun MenstrualCalendar(menstrualDays: Set<LocalDate>) {
    val currentMonth = YearMonth.now()
    val daysInMonth = currentMonth.lengthOfMonth()
    val firstOfMonth = currentMonth.atDay(1)
    val dayOfWeekOffset = firstOfMonth.dayOfWeek.value % 7

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = currentMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault()) + " " + currentMonth.year,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        // Simple Grid implementation
        val totalCells = daysInMonth + dayOfWeekOffset
        val rows = (totalCells + 6) / 7

        repeat(rows) { rowIndex ->
            Row(modifier = Modifier.fillMaxWidth()) {
                repeat(7) { colIndex ->
                    val cellIndex = rowIndex * 7 + colIndex
                    val dayNum = cellIndex - dayOfWeekOffset + 1
                    
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .padding(2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (dayNum in 1..daysInMonth) {
                            val date = currentMonth.atDay(dayNum)
                            val isMenstrual = menstrualDays.contains(date)
                            
                            if (isMenstrual) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color(0xFFFFC0CB), CircleShape) // Pink
                                )
                            }
                            Text(text = dayNum.toString(), style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileInfoRow(label: String, value: String, onEdit: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(label, style = MaterialTheme.typography.labelMedium, color = Color.Gray)
            Text(value, style = MaterialTheme.typography.bodyLarge)
        }
        IconButton(onClick = onEdit) {
            Icon(Icons.Default.Edit, contentDescription = "Edit $label", modifier = Modifier.size(20.dp))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ExerciseSelectionDialog(
    currentSelected: List<String>,
    allOptions: List<String>,
    onDismiss: () -> Unit,
    onSave: (List<String>) -> Unit
) {
    val selected = remember { mutableStateListOf<String>().apply { addAll(currentSelected) } }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Preferred Exercises") },
        text = {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                allOptions.forEach { exercise ->
                    FilterChip(
                        selected = selected.contains(exercise),
                        onClick = {
                            if (selected.contains(exercise)) selected.remove(exercise) else selected.add(exercise)
                        },
                        label = { Text(exercise) }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(selected.toList()) }) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}