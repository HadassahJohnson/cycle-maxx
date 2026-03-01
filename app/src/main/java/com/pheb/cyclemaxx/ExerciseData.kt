package com.pheb.cyclemaxx

data class Exercise(
    val name: String,
    val videoUrl: String,
    val intensity: Intensity, // LOW, MEDIUM, HIGH
    val category: String, // Yoga, HIIT, Strength, etc.
    val equipment: String, // Home, Gym, Both
    val level: String = "Beginner" // Beginner, Intermediate, Expert
)

enum class Intensity { LOW, MEDIUM, HIGH }

val exerciseLibrary = listOf(
    Exercise("Squats", "https://youtu.be/2t3Ab7a2ZM4?si=ZHHzQRq0vyHPyNhj", Intensity.MEDIUM, "Strength", "Both", "Beginner"),
    Exercise("Jumping jacks", "https://youtube.com/shorts/yg3KQQn3QWg?si=8cMeEqBx1KNRVWxy", Intensity.MEDIUM, "Cardio", "Home", "Beginner"),
    Exercise("Lunges", "https://youtube.com/shorts/1cS-6KsJW9g?si=ECAcJs6XyTh3oel2", Intensity.MEDIUM, "Strength", "Both", "Beginner"),
    Exercise("Bulgarian Split Squats", "https://youtu.be/vgn7bSXkgkA?si=IEPmrltWAdV41NyX", Intensity.HIGH, "Strength", "Both", "Intermediate"),
    Exercise("Burpees", "https://youtu.be/qLBImHhCXSw?si=_VWMJm1eGA6crmQ8", Intensity.HIGH, "HIIT", "Home", "Intermediate"),
    Exercise("Push-ups", "https://youtu.be/P3D2PPMYWjk?si=6rHSoIqTmKSjcyR9", Intensity.MEDIUM, "Calisthenics", "Home", "Beginner"),
    Exercise("Box Jumps", "https://youtu.be/NBY9-kTuHEk?si=pcMRtBBxDInl9Bnw", Intensity.HIGH, "HIIT", "Gym", "Expert"),
    Exercise("Tuck Jumps", "https://youtu.be/-bnJGikRGsM?si=GEK5OoWS_aky-3n7", Intensity.HIGH, "HIIT", "Home", "Expert"),
    Exercise("Tree Pose", "https://youtube.com/shorts/7hStkBjyocg?si=llpLwuO0RDDgPlPP", Intensity.LOW, "Yoga", "Home", "Beginner"),
    Exercise("Downward Dog", "https://youtu.be/ayQoxw8sRTk?si=0eu-ZK2xdRozZ9Nn", Intensity.LOW, "Yoga", "Home", "Beginner"),
    Exercise("Pigeon Pose", "https://youtu.be/M1gEGLtF1p0?si=CW3WdjY8xgHpF0JT", Intensity.LOW, "Yoga", "Home", "Beginner"),
    Exercise("Warrior 1 Pose", "https://youtu.be/kkGY3xBnaGc?si=2V82BWpTt6Npv-wr", Intensity.LOW, "Yoga", "Home", "Beginner"),
    Exercise("Pelvic Tilt", "https://youtu.be/44D6Xc2Fkek?si=S6Ungk-HaR6MofZN", Intensity.LOW, "Calisthenics", "Home", "Beginner"),
    Exercise("Knee Folds", "https://youtu.be/6IpR2glUC1w?si=vpa_bXC1Fx46fwjM", Intensity.LOW, "Calisthenics", "Home", "Beginner"),
    Exercise("Heel Slides", "https://youtu.be/aO3UxjPqZyI?si=fwGaOM6boEuYNxqD", Intensity.LOW, "Calisthenics", "Home", "Beginner"),
    Exercise("Cable Crunches", "https://youtube.com/shorts/VLbPGv1osLw?si=L9Froikth_XDQ-Bn", Intensity.MEDIUM, "Cable Exercises", "Gym", "Intermediate"),
    Exercise("Lat Pulldown", "https://youtube.com/shorts/pb2AHraWJxg?si=Sf3QqgmK74HwxtM_", Intensity.MEDIUM, "Machine Weights", "Gym", "Beginner"),
    Exercise("Overhead Tricep Extension", "https://youtube.com/shorts/xiXJxlGKryY?si=3Eh4S8KMpI6VvpHH", Intensity.MEDIUM, "Free Weights", "Both", "Beginner"),
    Exercise("Romanian Deadlift", "https://youtube.com/shorts/ecuZtKTNI9U?si=Cl8SyBdFKZuNdmXv", Intensity.HIGH, "Free Weights", "Gym", "Intermediate"),
    Exercise("Hip Thrust", "https://youtube.com/shorts/uUuPYcK9SJI?si=LOvw0WzZppY2eiu4", Intensity.HIGH, "Free Weights", "Gym", "Intermediate"),
    Exercise("Jab", "https://youtube.com/shorts/jAH3lKu99go?si=VIB-U0ANXIejZ9gB", Intensity.MEDIUM, "Cardio", "Home", "Beginner"),
    Exercise("Uppercut", "https://youtube.com/shorts/J4mQSD8yzBk?si=X5X9GRuF6F1c1v7", Intensity.MEDIUM, "Cardio", "Home", "Beginner")
)