package com.pheb.cyclemaxx

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.MenstruationPeriodRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import java.time.Instant

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun RegistrationScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    
    // Auth Mode state
    var isLoginMode by remember { mutableStateOf(false) }

    // Health Connect setup
    val healthConnectClient = remember { HealthConnectClient.getOrCreate(context) }
    val permissions = setOf(HealthPermission.getReadPermission(MenstruationPeriodRecord::class))

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var workoutFrequency by remember { mutableStateOf("<3") }
    var expanded by remember { mutableStateOf(false) }
    
    // New states for the requested features
    var isMetric by remember { mutableStateOf(true) }
    var expertiseIndex by remember { mutableIntStateOf(0) }
    var locationIndex by remember { mutableIntStateOf(0) }
    val expertiseLevels = listOf("Beginner", "Intermediate", "Expert")
    val locations = listOf("Home", "Gym")
    
    val exercises = listOf("HIIT", "Calisthenics", "Yoga", "Pilates", "Cardio", "Machine Weights", "Cable Exercises", "Free Weights")
    val selectedExercises = remember { mutableStateListOf<String>() }

    var lastPeriodStart by remember { mutableStateOf<Instant?>(null) }

    val options = listOf("<3", "3-5", "5-7")

    val requestPermissionsLauncher = rememberLauncherForActivityResult(
        PermissionController.createRequestPermissionResultContract()
    ) { granted ->
        if (granted.containsAll(permissions)) {
            scope.launch {
                lastPeriodStart = fetchLatestPeriodStart(healthConnectClient)
                Toast.makeText(context, "Health data synced!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun checkHealthConnectPermissions() {
        scope.launch {
            val granted = healthConnectClient.permissionController.getGrantedPermissions()
            if (granted.containsAll(permissions)) {
                lastPeriodStart = fetchLatestPeriodStart(healthConnectClient)
            } else {
                requestPermissionsLauncher.launch(permissions)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = if (isLoginMode) "Login" else "Create Account", 
            style = MaterialTheme.typography.headlineMedium
        )

        if (!isLoginMode) {
            OutlinedButton(
                onClick = { checkHealthConnectPermissions() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (lastPeriodStart == null) "Sync Cycle from Health Connect" else "Cycle Synced ✓")
            }
        }

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )

        if (!isLoginMode) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = age,
                onValueChange = { age = it },
                label = { Text("Age") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            // Unit Switch
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Imperial (lb/in)")
                Switch(
                    checked = isMetric, 
                    onCheckedChange = { 
                        val oldMetric = isMetric
                        isMetric = it 
                        // Instantly convert display values
                        if (oldMetric && !isMetric) {
                            height = height.toDoubleOrNull()?.let { UnitConverter.kgToLbs(it) }?.let { UnitConverter.format(it) } ?: ""
                            weight = weight.toDoubleOrNull()?.let { UnitConverter.kgToLbs(it) }?.let { UnitConverter.format(it) } ?: ""
                        } else if (!oldMetric && isMetric) {
                            height = height.toDoubleOrNull()?.let { UnitConverter.lbsToKg(it) }?.let { UnitConverter.format(it) } ?: ""
                            weight = weight.toDoubleOrNull()?.let { UnitConverter.lbsToKg(it) }?.let { UnitConverter.format(it) } ?: ""
                        }
                    }
                )
                Text("Metric (kg/cm)")
            }

            OutlinedTextField(
                value = height,
                onValueChange = { height = it },
                label = { Text(if (isMetric) "Height (cm)" else "Height (in)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )

            OutlinedTextField(
                value = weight,
                onValueChange = { weight = it },
                label = { Text(if (isMetric) "Weight (kg)" else "Weight (lb)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )

            // Expertise Selection
            Text("Expertise Level", style = MaterialTheme.typography.titleMedium)
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                expertiseLevels.forEachIndexed { index, label ->
                    SegmentedButton(
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = expertiseLevels.size),
                        onClick = { expertiseIndex = index },
                        selected = index == expertiseIndex
                    ) {
                        Text(label)
                    }
                }
            }

            // Location Selection
            Text("Workout Location", style = MaterialTheme.typography.titleMedium)
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                locations.forEachIndexed { index, label ->
                    SegmentedButton(
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = locations.size),
                        onClick = { locationIndex = index },
                        selected = index == locationIndex
                    ) {
                        Text(label)
                    }
                }
            }

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = workoutFrequency,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Workout Frequency (times/week)") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable),
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    options.forEach { selectionOption ->
                        DropdownMenuItem(
                            text = { Text(selectionOption) },
                            onClick = {
                                workoutFrequency = selectionOption
                                expanded = false
                            }
                        )
                    }
                }
            }

            // Preferred Exercises (FlowRow)
            Text("Preferred Exercises", style = MaterialTheme.typography.titleMedium)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                exercises.forEach { exercise ->
                    FilterChip(
                        selected = selectedExercises.contains(exercise),
                        onClick = {
                            if (selectedExercises.contains(exercise)) {
                                selectedExercises.remove(exercise)
                            } else {
                                selectedExercises.add(exercise)
                            }
                        },
                        label = { Text(exercise) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                val trimmedEmail = email.trim()
                val trimmedPassword = password.trim()

                if (trimmedEmail.isEmpty() || trimmedPassword.isEmpty() || (!isLoginMode && name.isEmpty())) {
                    Toast.makeText(context, "Please fill in all basic fields", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                if (isLoginMode) {
                    auth.signInWithEmailAndPassword(trimmedEmail, trimmedPassword)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(context, "Login Successful!", Toast.LENGTH_SHORT).show()
                                navController.navigate("dashboard") {
                                    popUpTo("auth") { inclusive = true }
                                }
                            } else {
                                Toast.makeText(context, "Login Error: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                } else {
                    auth.createUserWithEmailAndPassword(trimmedEmail, trimmedPassword)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val uid = auth.currentUser?.uid ?: ""
                                
                                // Always store in Metric in Firestore
                                val storedHeight = if (isMetric) height.toDoubleOrNull() ?: 0.0 else UnitConverter.inchesToCm(height.toDoubleOrNull() ?: 0.0)
                                val storedWeight = if (isMetric) weight.toDoubleOrNull() ?: 0.0 else UnitConverter.lbsToKg(weight.toDoubleOrNull() ?: 0.0)

                                val userProfile = UserProfile(
                                    uid = uid,
                                    name = name,
                                    age = age.toIntOrNull() ?: 0,
                                    height = storedHeight,
                                    weight = storedWeight,
                                    unitSystem = if (isMetric) "Metric" else "Imperial",
                                    expertise = expertiseLevels[expertiseIndex],
                                    workoutLocation = locations[locationIndex],
                                    workoutFrequency = workoutFrequency,
                                    preferredExercises = selectedExercises.toList()
                                )

                                db.collection("users").document(uid).set(userProfile)
                                    .addOnSuccessListener {
                                        Toast.makeText(context, "Registration Successful!", Toast.LENGTH_SHORT).show()
                                        navController.navigate("dashboard") {
                                            popUpTo("auth") { inclusive = true }
                                        }
                                    }
                                    .addOnFailureListener { e ->
                                        Toast.makeText(context, "Firestore Error: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                            } else {
                                Toast.makeText(context, "Auth Error: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (isLoginMode) "Login" else "Sign Up")
        }

        TextButton(onClick = { isLoginMode = !isLoginMode }) {
            Text(if (isLoginMode) "Don't have an account? Sign Up" else "Already have an account? Login")
        }
    }
}

suspend fun fetchLatestPeriodStart(client: HealthConnectClient): Instant? {
    return try {
        val response = client.readRecords(
            ReadRecordsRequest(
                recordType = MenstruationPeriodRecord::class,
                timeRangeFilter = TimeRangeFilter.before(Instant.now())
            )
        )
        response.records.maxByOrNull { it.startTime }?.startTime
    } catch (e: Exception) {
        null
    }
}