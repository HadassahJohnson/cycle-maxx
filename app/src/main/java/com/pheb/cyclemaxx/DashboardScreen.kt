package com.pheb.cyclemaxx

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.MenstruationPeriodRecord
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import java.time.Instant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavController,
    viewModel: DashboardViewModel = viewModel(factory = DashboardViewModelFactory(LocalContext.current))
) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val uid = auth.currentUser?.uid

    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    val permissions = setOf(HealthPermission.getReadPermission(MenstruationPeriodRecord::class))
    val requestPermissionsLauncher = rememberLauncherForActivityResult(
        PermissionController.createRequestPermissionResultContract()
    ) { granted ->
        if (granted.containsAll(permissions)) {
            // Permissions granted, data will reload via SnapshotListener/ViewModel
        } else {
            Toast.makeText(context, "Health Connect permissions denied", Toast.LENGTH_SHORT).show()
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val selectedDate = datePickerState.selectedDateMillis
                    if (selectedDate != null && uid != null) {
                        db.collection("users").document(uid).update("manualLastPeriodDate", selectedDate)
                            .addOnSuccessListener {
                                showDatePicker = false
                                Toast.makeText(context, "Period date saved!", Toast.LENGTH_SHORT).show()
                            }
                    }
                }) { Text("Confirm") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cycle Maxx", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { navController.navigate("profile") }) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Profile",
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        if (viewModel.isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp)
            ) {
                Text(
                    text = "Phase: ${viewModel.currentPhase.name.lowercase().replaceFirstChar { it.uppercase() }}",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(8.dp))

                EnergyMeterCard(phase = viewModel.currentPhase)

                Spacer(modifier = Modifier.height(16.dp))

                if (!viewModel.isHealthDataFound) {
                    ManualDataPromptCard { showDatePicker = true }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                Text(text = "Today's Workout", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(viewModel.workouts) { exercise ->
                        ExerciseCard(exercise) {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(exercise.videoUrl))
                            context.startActivity(intent)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EnergyMeterCard(phase: MenstrualPhase) {
    val (energyLevel, color) = when (phase) {
        MenstrualPhase.OVULATORY -> "High" to Color(0xFF4CAF50) // Green
        MenstrualPhase.FOLLICULAR -> "Moderate-High" to Color(0xFF8BC34A) // Light Green
        MenstrualPhase.LUTEAL -> "Moderate-Low" to Color(0xFFFFC107) // Amber
        MenstrualPhase.MENSTRUAL -> "Low" to Color(0xFFF44336) // Red
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Energy Meter", style = MaterialTheme.typography.labelLarge)
                Text(energyLevel, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = color)
            }
            Box(
                modifier = Modifier
                    .width(100.dp)
                    .height(12.dp)
                    .background(Color.LightGray, shape = MaterialTheme.shapes.small)
            ) {
                val progress = when(phase) {
                    MenstrualPhase.OVULATORY -> 1f
                    MenstrualPhase.FOLLICULAR -> 0.75f
                    MenstrualPhase.LUTEAL -> 0.4f
                    MenstrualPhase.MENSTRUAL -> 0.15f
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .fillMaxHeight()
                        .background(color, shape = MaterialTheme.shapes.small)
                )
            }
        }
    }
}

@Composable
fun ExerciseCard(exercise: Exercise, onWatchClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = exercise.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    text = "${exercise.category} • ${exercise.intensity} Intensity",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Button(
                onClick = onWatchClick,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text("Watch Tutorial", style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@Composable
fun ManualDataPromptCard(onLogClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "No cycle data found. Log your last period manually?",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = onLogClick) {
                Text("Log Date")
            }
        }
    }
}
