package com.example.localjobs.models


// --- Data models ---
data class User(val email: String, val password: String, val isAdmin: Boolean)
data class Job(
    val id: String,
    val title: String,
    val company: String,
    val salary: String
)
data class Profile(
    val email: String,
    val name: String,
    val college: String,
    val dob: String,    // stored as String
    val cgpa: String,   // stored as String
    val resumeLink: String
)

// --- Mock test models ---
data class MockTest(
    val id: String,
    val title: String,
    val description: String,
    val timeLimitMinutes: Int?, // optional
    val questions: List<Question>
)

data class Question(
    val id: String,
    val text: String,
    val options: List<OptionItem>, // exactly 4
    val correctIndex: Int, // 0..3
    val marks: Int = 1
)

data class OptionItem(val index: Int, val text: String)

data class TestResult(
    val id: String,
    val testId: String,
    val userEmail: String,
    val score: Int,
    val maxScore: Int,
    val percentage: Double,
    val answers: List<Answer>,
    val createdAt: Long = System.currentTimeMillis()
)

data class Answer(val questionId: String, val selectedIndex: Int, val correct: Boolean)
