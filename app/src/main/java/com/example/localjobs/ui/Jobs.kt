package com.example.localjobs.ui

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.localjobs.models.Job
import com.example.localjobs.models.Profile
import com.example.localjobs.models.User
import com.example.localjobs.service.addApplicantToJob
import com.example.localjobs.service.addJob
import com.example.localjobs.service.getApplicantsForJob
import com.example.localjobs.service.getProfileForEmail
import com.example.localjobs.service.loadApplications
import com.example.localjobs.service.loadJobs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID
import kotlin.collections.any


@Composable
fun JobsScreen(
    currentUser: User,
    onLogout: () -> Unit,
    onPostJob: () -> Unit,
    onProfile: () -> Unit,
    onViewApplications: (String) -> Unit,
    onMockTests: () -> Unit,
    filesDir: File
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    var jobs by remember { mutableStateOf<List<Job>>(emptyList()) }
    var appliedMap by remember { mutableStateOf<Map<String, List<String>>>(emptyMap()) } // jobId -> list of applicant emails

    // load jobs & applications
    LaunchedEffect(Unit) {
        jobs = loadJobs(filesDir)
        appliedMap = loadApplications(filesDir).toMap()
    }

    Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row {
                if (!currentUser.isAdmin) {
                    Button(onClick = onProfile) { Text("Profile") }
                    Spacer(modifier = Modifier.width(8.dp))
                }
            }

            Text("Jobs", style = MaterialTheme.typography.h5)
            Row {
                if (currentUser.isAdmin) {
                    Button(onClick = onPostJob) { Text("Post Job") }
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Button(onClick = onMockTests) { Text("Mock Tests") }
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedButton(onClick = { onLogout() }) { Text("Logout") }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        if (jobs.isEmpty()) {
            Text("No jobs posted yet.")
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(jobs) { job ->
                    Card(modifier = Modifier.fillMaxWidth().padding(8.dp), elevation = 4.dp) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(job.title, style = MaterialTheme.typography.h6)
                            Text(job.company, style = MaterialTheme.typography.subtitle2)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(job.description)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Salary: ${job.salary}", style = MaterialTheme.typography.subtitle1)
                            Spacer(modifier = Modifier.height(8.dp))

                            // Buttons: Apply (users) or View (admin)
                            if (currentUser.isAdmin) {
                                Button(onClick = { onViewApplications(job.id) }) {
                                    Text("View")
                                }
                            } else {
                                val applicants = appliedMap[job.id] ?: emptyList()
                                val alreadyApplied = applicants.any { it.equals(currentUser.email, true) }
                                if (alreadyApplied) {
                                    Button(onClick = { /* no-op */ }, enabled = false) { Text("Done") }
                                } else {
                                    Button(onClick = {
                                        scope.launch(Dispatchers.IO) {
                                            addApplicantToJob(filesDir, job.id, currentUser.email)
                                            // refresh map on main
                                            val refreshed = loadApplications(filesDir).toMap()
                                            launch(Dispatchers.Main) { appliedMap = refreshed }
                                        }
                                    }) { Text("Apply") }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PostJobScreen(onPosted: () -> Unit, onCancel: () -> Unit, filesDir: File) {
    val ctx = LocalContext.current
    var title by remember { mutableStateOf("") }
    var company by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var salary by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Text("Post new job", style = MaterialTheme.typography.h5)
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = company, onValueChange = { company = it }, label = { Text("Company") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = salary, onValueChange = { salary = it }, label = { Text("Salary") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(16.dp))
        Row {
            Button(onClick = {
                if (title.isBlank() || company.isBlank() || desc.isBlank()) {
                    Toast.makeText(ctx, "Fill all fields", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                val job = Job(UUID.randomUUID().toString(), title.trim(), company.trim(), desc.trim(), salary.trim())
                scope.launch {
                    // do IO on IO dispatcher
                    kotlinx.coroutines.withContext(Dispatchers.IO) {
                        addJob(filesDir, job)
                    }
                    onPosted() // back on main because coroutine launched from compose scope runs on Main by default
                }
            }) { Text("Post") }

            Spacer(modifier = Modifier.width(8.dp))
            OutlinedButton(onClick = onCancel) { Text("Cancel") }
        }
    }
}

// --- Admin: view job + applicants ---

@Composable
fun JobApplicationsScreen(currentUser: User, jobId: String, onBack: () -> Unit, filesDir: File) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    var job by remember { mutableStateOf<Job?>(null) }
    var applicants by remember { mutableStateOf<List<String>>(emptyList()) }
    var profiles by remember { mutableStateOf<List<Profile>>(emptyList()) }

    LaunchedEffect(jobId) {
        job = loadJobs(filesDir).firstOrNull { it.id == jobId }
        applicants = getApplicantsForJob(filesDir, jobId)
        profiles = applicants.mapNotNull { getProfileForEmail(filesDir, it) }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Applicants", style = MaterialTheme.typography.h5)
            OutlinedButton(onClick = onBack) { Text("Back") }
        }
        Spacer(modifier = Modifier.height(12.dp))

        job?.let { j ->
            Card(modifier = Modifier.fillMaxWidth().padding(8.dp), elevation = 4.dp) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(j.title, style = MaterialTheme.typography.h6)
                    Text(j.company, style = MaterialTheme.typography.subtitle2)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(j.description)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Salary: ${j.salary}")
                }
            }
        } ?: Text("Job not found")

        Spacer(modifier = Modifier.height(16.dp))
        Text("Applicants (${applicants.size}):", style = MaterialTheme.typography.subtitle1)
        Spacer(modifier = Modifier.height(8.dp))

        if (profiles.isEmpty()) {
            Text("No applicants yet.")
        } else {
            LazyColumn {
                items(profiles) { p ->
                    Card(modifier = Modifier.fillMaxWidth().padding(8.dp), elevation = 2.dp) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Email: ${p.email}", style = MaterialTheme.typography.subtitle2)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Name: ${p.name}")
                            Text("College: ${p.college}")
                            Text("DOB: ${p.dob}")
                            Text("CGPA: ${p.cgpa}")
                            Text("Resume: ${p.resumeLink}")
                        }
                    }
                }
            }
        }
    }
}
