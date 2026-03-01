package com.pheb.cyclemaxx

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class DashboardViewModel(
    private val healthConnectManager: HealthConnectManager
) : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val uid = auth.currentUser?.uid

    var userProfile by mutableStateOf<UserProfile?>(null)
        private set
    var currentPhase by mutableStateOf(MenstrualPhase.FOLLICULAR)
        private set
    var workouts by mutableStateOf<List<Exercise>>(emptyList())
        private set
    var isHealthDataFound by mutableStateOf(false)
        private set
    var isLoading by mutableStateOf(true)
        private set

    init {
        loadDashboardData()
    }

    private fun loadDashboardData() {
        if (uid == null) {
            isLoading = false
            return
        }

        db.collection("users").document(uid).addSnapshotListener { document, _ ->
            val profile = document?.toObject(UserProfile::class.java)
            userProfile = profile
            
            viewModelScope.launch {
                // 1. Fetch data from Health Connect
                val hcPhase = healthConnectManager.fetchCurrentPhase()
                
                // 2. Determine phase: HC record > Manual Date > Fallback
                if (hcPhase != MenstrualPhase.FOLLICULAR) {
                    currentPhase = hcPhase
                    isHealthDataFound = true
                } else if (profile?.manualLastPeriodDate != null) {
                    currentPhase = getPhaseFromTimestamp(profile.manualLastPeriodDate)
                    isHealthDataFound = true
                } else {
                    currentPhase = MenstrualPhase.FOLLICULAR
                    isHealthDataFound = false
                }

                // 3. Trigger workout generation once the phase is known
                profile?.let {
                    workouts = generateDailyWorkout(it, currentPhase)
                }
                isLoading = false
            }
        }
    }
}

class DashboardViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DashboardViewModel(HealthConnectManager(context)) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}