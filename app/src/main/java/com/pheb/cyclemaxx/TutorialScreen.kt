package com.pheb.cyclemaxx

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

data class ExerciseTutorial(
    val name: String,
    val description: String,
    val youtubeId: String
)

@Composable
fun TutorialScreen() {
    val context = LocalContext.current
    
    // This is your database of tutorials
    val tutorials = listOf(
        ExerciseTutorial("Deadlifts", "Great for Follicular/Ovulatory phases.", "op9u0_4Xvuo"),
        ExerciseTutorial("Yoga Flow", "Perfect for Menstrual phase recovery.", "v7AYKMP6rOE"),
        ExerciseTutorial("HIIT Sprints", "High intensity for peak energy.", "ml6cT4AZdqI"),
        ExerciseTutorial("Pilates Core", "Ideal for the Luteal phase.", "2yTInU6JvK0")
    )

    Scaffold(
        topBar = { Text("Exercise Tutorials", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(16.dp)) }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding)) {
            items(tutorials) { tutorial ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .clickable {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=${tutorial.youtubeId}"))
                            context.startActivity(intent)
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = tutorial.name, style = MaterialTheme.typography.titleLarge)
                            Text(text = tutorial.description, style = MaterialTheme.typography.bodyMedium)
                        }
                        Text("▶️", style = MaterialTheme.typography.headlineSmall)
                    }
                }
            }
        }
    }
}