package com.pheb.cyclemaxx

data class Workout(val name: String, val duration: String, val videoId: String)

fun generateDailyWorkout(userProfile: UserProfile, phase: MenstrualPhase): List<Exercise> {
    val allowedIntensities = when (phase) {
        MenstrualPhase.MENSTRUAL -> listOf(Intensity.LOW)
        MenstrualPhase.FOLLICULAR, MenstrualPhase.OVULATORY -> listOf(Intensity.HIGH, Intensity.MEDIUM)
        MenstrualPhase.LUTEAL -> listOf(Intensity.MEDIUM, Intensity.LOW)
    }

    val filtered = exerciseLibrary.filter { exercise ->
        // 1. Filter by Location/Equipment
        val locationMatch = exercise.equipment == "Both" || 
                           exercise.equipment.equals(userProfile.workoutLocation, ignoreCase = true)
        
        // 2. Filter by Preferred Categories
        val categoryMatch = userProfile.preferredExercises.isEmpty() || 
                           userProfile.preferredExercises.any { it.equals(exercise.category, ignoreCase = true) }
        
        // 3. Filter by Phase-Appropriate Intensity
        val intensityMatch = allowedIntensities.contains(exercise.intensity)
        
        locationMatch && categoryMatch && intensityMatch
    }

    // Return randomized list of 4-6 exercises (or fewer if library is limited)
    val count = if (filtered.isNotEmpty()) (4..6).random().coerceAtMost(filtered.size) else 0
    return filtered.shuffled().take(count)
}

// Keeping old functions for compatibility if needed elsewhere
fun getPersonalizedWorkout(userProfile: UserProfile, phase: MenstrualPhase): List<Exercise> {
    val targetIntensity = when (phase) {
        MenstrualPhase.MENSTRUAL, MenstrualPhase.LUTEAL -> Intensity.LOW
        MenstrualPhase.FOLLICULAR, MenstrualPhase.OVULATORY -> Intensity.HIGH
    }

    val filtered = exerciseLibrary.filter { exercise ->
        val locationMatch = exercise.equipment == "Both" || exercise.equipment.equals(userProfile.workoutLocation, ignoreCase = true)
        val categoryMatch = userProfile.preferredExercises.any { it.equals(exercise.category, ignoreCase = true) }
        locationMatch && categoryMatch && exercise.intensity == targetIntensity
    }

    val levelMatched = filtered.filter { it.level.equals(userProfile.expertise, ignoreCase = true) }
    
    return if (levelMatched.size >= 5) {
        levelMatched.shuffled().take(5)
    } else {
        filtered.shuffled().take(5)
    }
}

fun getPlanForPhase(phase: MenstrualPhase, frequency: String): List<Workout> {
    return when (phase) {
        MenstrualPhase.OVULATORY -> {
            listOf(
                Workout("HIIT Blast", "20 min", "dQw4w9WgXcQ"), 
                Workout("Heavy Strength", "45 min", "videoId2")
            )
        }
        MenstrualPhase.MENSTRUAL -> {
            listOf(
                Workout("Restorative Yoga", "30 min", "videoId3"),
                Workout("Gentle Walk", "20 min", "videoId4")
            )
        }
        else -> listOf(Workout("Standard Strength", "30 min", "videoId5"))
    }
}

fun generateWorkoutPlan(currentPhase: MenstrualPhase, frequency: String): List<ExerciseTutorial> {
    val tutorials = listOf(
        ExerciseTutorial("Deadlifts", "Great for Follicular/Ovulatory phases.", "op9u0_4Xvuo"),
        ExerciseTutorial("Yoga Flow", "Perfect for Menstrual phase recovery.", "v7AYKMP6rOE"),
        ExerciseTutorial("HIIT Sprints", "High intensity for peak energy.", "ml6cT4AZdqI"),
        ExerciseTutorial("Pilates Core", "Ideal for the Luteal phase.", "2yTInU6JvK0")
    )

    val baseWorkouts = when (currentPhase) {
        MenstrualPhase.MENSTRUAL -> tutorials.filter { it.name == "Yoga Flow" }
        MenstrualPhase.FOLLICULAR, MenstrualPhase.OVULATORY -> 
            tutorials.filter { it.name == "Deadlifts" || it.name == "HIIT Sprints" }
        MenstrualPhase.LUTEAL -> tutorials.filter { it.name == "Pilates Core" }
    }

    val targetCount = when (frequency) {
        "<3" -> 2
        "3-5" -> 4
        "5-7" -> 6
        else -> 3
    }

    val plan = mutableListOf<ExerciseTutorial>()
    if (baseWorkouts.isNotEmpty()) {
        for (i in 0 until targetCount) {
            plan.add(baseWorkouts[i % baseWorkouts.size])
        }
    }
    return plan
}