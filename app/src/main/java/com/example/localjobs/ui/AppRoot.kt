package com.example.localjobs.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.localjobs.ui.*;
import com.example.localjobs.models.TestResult
import com.example.localjobs.models.User

@Composable
fun AppRoot() {
    val context = LocalContext.current
    val filesDir = context.filesDir
    var currentUser by remember { mutableStateOf<User?>(null) }
    var screen by remember { mutableStateOf("login") }
    var selectedJobId by remember { mutableStateOf<String?>(null) }
    var selectedTestId by remember { mutableStateOf<String?>(null) }
    var lastResult by remember { mutableStateOf<TestResult?>(null) }

    val scaffoldState = rememberScaffoldState()

    Scaffold(scaffoldState = scaffoldState) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (screen) {
                "login" -> LoginScreen(onLogin = { user -> currentUser = user; screen = "jobs" }, onRegister = { screen = "register" }, filesDir = filesDir)
                "register" -> RegisterScreen(onRegistered = { user -> currentUser = user; screen = "jobs" }, onBack = { screen = "login" }, filesDir = filesDir)
                "jobs" -> {
                    if (currentUser != null) JobsScreen(
                        currentUser = currentUser!!,
                        onLogout = { currentUser = null; screen = "login" },
                        onPostJob = { screen = "post" },
                        onProfile = {
                            if (!currentUser!!.isAdmin) screen = "profile"
                        },
                        onViewApplications = { jobId ->
                            selectedJobId = jobId; screen = "job_applications"
                        },
                        onMockTests = { selectedTestId = null; screen = "mock_tests" },
                        filesDir = filesDir
                    )
                }
                "post" -> PostJobScreen(
                    onPosted = { screen = "jobs" },
                    onCancel = { screen = "jobs" },
                    filesDir = filesDir
                )
                "profile" -> {
                    if (currentUser != null) ProfileScreen(currentUser = currentUser!!, onEdit = { screen = "editProfile" }, onBack = { screen = "jobs" }, filesDir = filesDir)
                }
                "editProfile" -> {
                    if (currentUser != null) EditProfileScreen(currentUser = currentUser!!, onSaved = { screen = "profile" }, onCancel = { screen = "profile" }, filesDir = filesDir)
                }
                "job_applications" -> {
                    val jid = selectedJobId
                    if (currentUser != null && jid != null) JobApplicationsScreen(
                        currentUser = currentUser!!,
                        jobId = jid,
                        onBack = { screen = "jobs" },
                        filesDir = filesDir
                    )
                }
                // Mock tests screens
                "mock_tests" -> {
                    if (currentUser != null) MockTestsList(
                        currentUser = currentUser!!,
                        filesDir = filesDir,
                        onBack = { screen = "jobs" },
                        onAttempt = { tid -> selectedTestId = tid; screen = "attempt_test" },
                        onCreate = { screen = "create_test" },
                        onViewResults = { tid -> selectedTestId = tid; screen = "view_test_results" }
                    )
                }
                "create_test" -> {
                    AdminCreateMockTest(filesDir = filesDir, onSaved = { screen = "mock_tests" }, onCancel = { screen = "mock_tests" })
                }
                "attempt_test" -> {
                    val tid = selectedTestId
                    if (currentUser != null && tid != null) AttemptMockTest(currentUser = currentUser!!, testId = tid, filesDir = filesDir, onBack = { screen = "mock_tests" }, onResult = {
                        lastResult = it
                        screen = "test_result"
                    })
                }
                "test_result" -> {
                    val res = lastResult
                    if (res != null) MockTestResultScreen(res, filesDir = filesDir, onBack = { screen = "mock_tests" })
                    else screen = "mock_tests"
                }
                "view_test_results" -> {
                    val tid = selectedTestId
                    if (tid != null) ViewMockTestResults(filesDir = filesDir, testId = tid, onBack = { screen = "mock_tests" })
                }
            }
        }
    }
}

