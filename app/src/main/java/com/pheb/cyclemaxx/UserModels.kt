package com.pheb.cyclemaxx

data class UserProfile(
    val uid: String = "",
    val name: String = "",
    val age: Int = 0,
    val height: Double = 0.0,
    val weight: Double = 0.0,
    val unitSystem: String = "Metric", // "Metric" (kg/cm) or "Imperial" (lb/in)
    val expertise: String = "Beginner", // Beginner, Intermediate, Expert
    val workoutLocation: String = "Gym", // Gym or Home
    val workoutFrequency: String = "", // e.g., "3-5 times/week"
    val workoutMethod: String = "",    // e.g., "Gym" or "Home"
    val fitnessGoals: String = "",
    val preferredExercises: List<String> = emptyList(), // hiit, yoga, etc.
    val manualLastPeriodDate: Long? = null // Timestamp in milliseconds
)