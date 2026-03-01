package com.pheb.cyclemaxx

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.MenstruationPeriodRecord
import com.google.firebase.auth.FirebaseAuth
import com.pheb.cyclemaxx.ui.theme.CycleMaxxTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    // 1. Define the permissions you need
    val permissions = setOf(HealthPermission.getReadPermission(MenstruationPeriodRecord::class))

    // 2. Create the launcher
    val requestPermissionActivityContract = PermissionController.createRequestPermissionResultContract()

    val requestPermissions = registerForActivityResult(requestPermissionActivityContract) { granted ->
        if (granted.containsAll(permissions)) {
            // Permission success!
            Toast.makeText(this, "Health Connect permissions granted", Toast.LENGTH_SHORT).show()
        } else {
            // Still denied - usually means the user clicked 'Don't Allow' or 
            // the Manifest setup above is incorrect.
            Toast.makeText(this, "Health Connect permissions denied", Toast.LENGTH_SHORT).show()
        }
    }

    // 3. Call this function (e.g., on a button click)
    fun checkAndRequestPermissions(client: HealthConnectClient) {
        lifecycleScope.launch {
            val granted = client.permissionController.getGrantedPermissions()
            if (!granted.containsAll(permissions)) {
                requestPermissions.launch(permissions)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CycleMaxxTheme {
                val navController = rememberNavController()
                val currentUser = FirebaseAuth.getInstance().currentUser
                val startDestination = if (currentUser != null) "dashboard" else "auth"

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = startDestination,
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("auth") {
                            RegistrationScreen(navController = navController)
                        }
                        composable("dashboard") {
                            DashboardScreen(navController = navController)
                        }
                        composable("tutorials") {
                            TutorialScreen()
                        }
                        composable("profile") {
                            ProfileScreen(navController = navController)
                        }
                    }
                }
            }
        }
    }
}