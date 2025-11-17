package com.example.localjobs

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.*

// --- Data models ---
data class User(val email: String, val password: String, val isAdmin: Boolean)
data class Job(
    val id: String,
    val title: String,
    val company: String,
    val description: String,
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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Ensure users file exists and admin seeded
        seedAdminIfNeeded()
        // Ensure jobs, profiles & applications file exist
        ensureFileExists("jobs.json")
        ensureFileExists("profiles.json")
        ensureFileExists("applications.json")

        setContent {
            MaterialTheme {
                AppRoot()
            }
        }
    }

    private fun seedAdminIfNeeded() {
        val usersFile = File(filesDir, "users.json")
        if (!usersFile.exists()) {
            // seed with admin user
            val arr = JSONArray()
            val admin = JSONObject()
            admin.put("email", "admin@mail.com")
            admin.put("password", "1234")
            admin.put("isAdmin", true)
            arr.put(admin)
            usersFile.writeText(arr.toString())
        } else {
            // ensure admin exists
            val content = usersFile.readText()
            try {
                val arr = JSONArray(content)
                var found = false
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    if (obj.optString("email") == "admin@mail.com") {
                        found = true
                        break
                    }
                }
                if (!found) {
                    val admin = JSONObject()
                    admin.put("email", "admin@mail.com")
                    admin.put("password", "1234")
                    admin.put("isAdmin", true)
                    arr.put(admin)
                    usersFile.writeText(arr.toString())
                }
            } catch (e: Exception) {
                // if parsing failed, overwrite
                val arr2 = JSONArray()
                val admin = JSONObject()
                admin.put("email", "admin@mail.com")
                admin.put("password", "1234")
                admin.put("isAdmin", true)
                arr2.put(admin)
                usersFile.writeText(arr2.toString())
            }
        }
    }

    private fun ensureFileExists(name: String) {
        val f = File(filesDir, name)
        if (!f.exists()) f.writeText("[]")
    }
}

// --- Storage helpers (file-based using internal storage) ---

// USERS
fun loadUsers(filesDir: File): List<User> {
    val f = File(filesDir, "users.json")
    if (!f.exists()) return emptyList()
    val content = f.readText()
    val list = mutableListOf<User>()
    try {
        val arr = JSONArray(content)
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            list.add(User(obj.getString("email"), obj.getString("password"), obj.optBoolean("isAdmin", false)))
        }
    } catch (e: Exception) {
        // ignore parse error
    }
    return list
}

fun saveUsers(filesDir: File, users: List<User>) {
    val arr = JSONArray()
    for (u in users) {
        val obj = JSONObject()
        obj.put("email", u.email)
        obj.put("password", u.password)
        obj.put("isAdmin", u.isAdmin)
        arr.put(obj)
    }
    File(filesDir, "users.json").writeText(arr.toString())
}

fun addUser(filesDir: File, user: User) {
    val users = loadUsers(filesDir).toMutableList()
    users.add(user)
    saveUsers(filesDir, users)
}

// JOBS
fun loadJobs(filesDir: File): List<Job> {
    val f = File(filesDir, "jobs.json")
    if (!f.exists()) return emptyList()
    val content = f.readText()
    val list = mutableListOf<Job>()
    try {
        val arr = JSONArray(content)
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            list.add(
                Job(
                    obj.getString("id"),
                    obj.getString("title"),
                    obj.getString("company"),
                    obj.getString("description"),
                    obj.optString("salary", "Not specified")
                )
            )
        }
    } catch (e: Exception) {
        // ignore parse error
    }
    return list
}

fun saveJobs(filesDir: File, jobs: List<Job>) {
    val arr = JSONArray()
    for (j in jobs) {
        val obj = JSONObject()
        obj.put("id", j.id)
        obj.put("title", j.title)
        obj.put("company", j.company)
        obj.put("description", j.description)
        obj.put("salary", j.salary)
        arr.put(obj)
    }
    File(filesDir, "jobs.json").writeText(arr.toString())
}

fun addJob(filesDir: File, job: Job) {
    val jobs = loadJobs(filesDir).toMutableList()
    jobs.add(job)
    saveJobs(filesDir, jobs)
}

// PROFILES
fun loadProfiles(filesDir: File): List<Profile> {
    val f = File(filesDir, "profiles.json")
    if (!f.exists()) return emptyList()
    val content = f.readText()
    val list = mutableListOf<Profile>()
    try {
        val arr = JSONArray(content)
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            list.add(
                Profile(
                    obj.optString("email", ""),
                    obj.optString("name", ""),
                    obj.optString("college", ""),
                    obj.optString("dob", ""),
                    obj.optString("cgpa", ""),
                    obj.optString("resumeLink", "")
                )
            )
        }
    } catch (e: Exception) { }
    return list
}

fun saveProfiles(filesDir: File, profiles: List<Profile>) {
    val arr = JSONArray()
    for (p in profiles) {
        val obj = JSONObject()
        obj.put("email", p.email)
        obj.put("name", p.name)
        obj.put("college", p.college)
        obj.put("dob", p.dob)
        obj.put("cgpa", p.cgpa)
        obj.put("resumeLink", p.resumeLink)
        arr.put(obj)
    }
    File(filesDir, "profiles.json").writeText(arr.toString())
}

fun getProfileForEmail(filesDir: File, email: String): Profile? {
    return loadProfiles(filesDir).firstOrNull { it.email.equals(email, ignoreCase = true) }
}

fun upsertProfile(filesDir: File, profile: Profile) {
    val list = loadProfiles(filesDir).toMutableList()
    val idx = list.indexOfFirst { it.email.equals(profile.email, ignoreCase = true) }
    if (idx >= 0) list[idx] = profile else list.add(profile)
    saveProfiles(filesDir, list)
}

// APPLICATIONS (who applied to which job)
// stored in applications.json as array of { "jobId": "id", "applicants": ["email1","email2"] }

fun loadApplications(filesDir: File): List<Pair<String, List<String>>> {
    val f = File(filesDir, "applications.json")
    if (!f.exists()) return emptyList()
    val content = f.readText()
    val list = mutableListOf<Pair<String, List<String>>>()
    try {
        val arr = JSONArray(content)
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            val jobId = obj.optString("jobId", "")
            val apps = mutableListOf<String>()
            val aArr = obj.optJSONArray("applicants")
            if (aArr != null) {
                for (j in 0 until aArr.length()) apps.add(aArr.getString(j))
            }
            if (jobId.isNotBlank()) list.add(jobId to apps)
        }
    } catch (e: Exception) { }
    return list
}

fun saveApplications(filesDir: File, applications: List<Pair<String, List<String>>>) {
    val arr = JSONArray()
    for (pair in applications) {
        val obj = JSONObject()
        obj.put("jobId", pair.first)
        val aArr = JSONArray()
        for (email in pair.second) aArr.put(email)
        obj.put("applicants", aArr)
        arr.put(obj)
    }
    File(filesDir, "applications.json").writeText(arr.toString())
}

fun getApplicantsForJob(filesDir: File, jobId: String): List<String> {
    val apps = loadApplications(filesDir)
    return apps.firstOrNull { it.first == jobId }?.second ?: emptyList()
}

fun addApplicantToJob(filesDir: File, jobId: String, email: String) {
    val apps = loadApplications(filesDir).toMutableList()
    val idx = apps.indexOfFirst { it.first == jobId }
    if (idx >= 0) {
        val current = apps[idx].second.toMutableList()
        if (!current.any { it.equals(email, true) }) {
            current.add(email)
            apps[idx] = jobId to current
        }
    } else {
        apps.add(jobId to listOf(email))
    }
    saveApplications(filesDir, apps)
}

// Authentication
fun authenticate(filesDir: File, email: String, password: String): User? {
    val users = loadUsers(filesDir)
    for (u in users) if (u.email.equals(email, ignoreCase = true) && u.password == password) return u
    return null
}

// --- Compose UI ---

@Composable
fun AppRoot() {
    val context = LocalContext.current
    val filesDir = context.filesDir
    var currentUser by remember { mutableStateOf<User?>(null) }
    var screen by remember { mutableStateOf("login") }
    var selectedJobId by remember { mutableStateOf<String?>(null) }

    val scaffoldState = rememberScaffoldState()

    Scaffold(scaffoldState = scaffoldState) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (screen) {
                "login" -> LoginScreen(onLogin = { user -> currentUser = user; screen = "jobs" }, onRegister = { screen = "register" }, filesDir = filesDir)
                "register" -> RegisterScreen(onRegistered = { user -> currentUser = user; screen = "jobs" }, onBack = { screen = "login" }, filesDir = filesDir)
                "jobs" -> {
                    if (currentUser != null) JobsScreen(currentUser = currentUser!!,
                        onLogout = { currentUser = null; screen = "login" },
                        onPostJob = { screen = "post" },
                        onProfile = {
                            if (!currentUser!!.isAdmin) screen = "profile"
                        },
                        onViewApplications = { jobId -> selectedJobId = jobId; screen = "job_applications" },
                        filesDir = filesDir)
                }
                "post" -> PostJobScreen(onPosted = { screen = "jobs" }, onCancel = { screen = "jobs" }, filesDir = filesDir)
                "profile" -> {
                    if (currentUser != null) ProfileScreen(currentUser = currentUser!!, onEdit = { screen = "editProfile" }, onBack = { screen = "jobs" }, filesDir = filesDir)
                }
                "editProfile" -> {
                    if (currentUser != null) EditProfileScreen(currentUser = currentUser!!, onSaved = { screen = "profile" }, onCancel = { screen = "profile" }, filesDir = filesDir)
                }
                "job_applications" -> {
                    val jid = selectedJobId
                    if (currentUser != null && jid != null) JobApplicationsScreen(currentUser = currentUser!!, jobId = jid, onBack = { screen = "jobs" }, filesDir = filesDir)
                }
            }
        }
    }
}

@Composable
fun LoginScreen(onLogin: (User) -> Unit, onRegister: () -> Unit, filesDir: File) {
    val ctx = LocalContext.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text("Job Spot", style = MaterialTheme.typography.h4)
        Spacer(modifier = Modifier.height(24.dp))
        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Password") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            scope.launch(Dispatchers.IO) {
                val user = authenticate(filesDir, email.trim(), password)
                if (user != null) {
                    launch(Dispatchers.Main) { onLogin(user) }
                } else {
                    launch(Dispatchers.Main) { Toast.makeText(ctx, "Invalid credentials", Toast.LENGTH_SHORT).show() }
                }
            }
        }, modifier = Modifier.fillMaxWidth()) { Text("Login") }
        Spacer(modifier = Modifier.height(8.dp))
        TextButton(onClick = onRegister) { Text("Register new account") }
    }
}

@Composable
fun RegisterScreen(onRegistered: (User) -> Unit, onBack: () -> Unit, filesDir: File) {
    val ctx = LocalContext.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text("Register", style = MaterialTheme.typography.h5)
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Password") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            if (email.isBlank() || password.isBlank()) { Toast.makeText(ctx, "Fill both fields", Toast.LENGTH_SHORT).show(); return@Button }
            val existing = loadUsers(filesDir)
            if (existing.any { it.email.equals(email.trim(), ignoreCase = true) }) { Toast.makeText(ctx, "Email already registered", Toast.LENGTH_SHORT).show(); return@Button }
            val user = User(email.trim(), password, false)
            val scope2 = scope
            scope2.launch(Dispatchers.IO) {
                addUser(filesDir, user)
                // create empty profile placeholder
                upsertProfile(filesDir, Profile(user.email, "", "", "", "", ""))
                launch(Dispatchers.Main) { onRegistered(user) }
            }
        }, modifier = Modifier.fillMaxWidth()) { Text("Create account") }
        Spacer(modifier = Modifier.height(8.dp))
        TextButton(onClick = onBack) { Text("Back to Login") }
    }
}

@Composable
fun JobsScreen(
    currentUser: User,
    onLogout: () -> Unit,
    onPostJob: () -> Unit,
    onProfile: () -> Unit,
    onViewApplications: (String) -> Unit,
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
                if (title.isBlank() || company.isBlank() || desc.isBlank()) { Toast.makeText(ctx, "Fill all fields", Toast.LENGTH_SHORT).show(); return@Button }
                val job = Job(UUID.randomUUID().toString(), title.trim(), company.trim(), desc.trim(), salary.trim())
                scope.launch(Dispatchers.IO) {
                    addJob(filesDir, job)
                    launch(Dispatchers.Main) { onPosted() }
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

// Profiles UI (view & edit) - same as before

@Composable
fun ProfileScreen(currentUser: User, onEdit: () -> Unit, onBack: () -> Unit, filesDir: File) {
    val ctx = LocalContext.current
    var profile by remember { mutableStateOf<Profile?>(null) }

    LaunchedEffect(currentUser.email) {
        profile = getProfileForEmail(filesDir, currentUser.email)
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Profile", style = MaterialTheme.typography.h5)
            Row {
                OutlinedButton(onClick = onEdit) { Text("Edit") }
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedButton(onClick = onBack) { Text("Back") }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        if (profile == null) {
            Text("No profile found. Click Edit to create one.")
        } else {
            val p = profile!!
            Text("Email: ${p.email}", style = MaterialTheme.typography.subtitle2)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Name: ${p.name}")
            Text("College: ${p.college}")
            Text("DOB: ${p.dob}")
            Text("CGPA: ${p.cgpa}")
            Text("Resume: ${p.resumeLink}")
        }
    }
}

@Composable
fun EditProfileScreen(currentUser: User, onSaved: () -> Unit, onCancel: () -> Unit, filesDir: File) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    var name by remember { mutableStateOf("") }
    var college by remember { mutableStateOf("") }
    var dob by remember { mutableStateOf("") }
    var cgpa by remember { mutableStateOf("") }
    var resumeLink by remember { mutableStateOf("") }

    LaunchedEffect(currentUser.email) {
        val existing = getProfileForEmail(filesDir, currentUser.email)
        if (existing != null) {
            name = existing.name
            college = existing.college
            dob = existing.dob
            cgpa = existing.cgpa
            resumeLink = existing.resumeLink
        }
    }
    // testing
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Edit Profile", style = MaterialTheme.typography.h5)
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = college, onValueChange = { college = it }, label = { Text("College") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = dob, onValueChange = { dob = it }, label = { Text("DOB") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = cgpa, onValueChange = { cgpa = it }, label = { Text("CGPA") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = resumeLink, onValueChange = { resumeLink = it }, label = { Text("Resume (Google Docs link)") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(16.dp))

        Row {
            Button(onClick = {
                if (name.isBlank() || college.isBlank()) {
                    Toast.makeText(ctx, "Please fill name and college", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                val profile = Profile(currentUser.email, name.trim(), college.trim(), dob.trim(), cgpa.trim(), resumeLink.trim())
                scope.launch(Dispatchers.IO) {
                    upsertProfile(filesDir, profile)
                    launch(Dispatchers.Main) {
                        Toast.makeText(ctx, "Profile saved", Toast.LENGTH_SHORT).show()
                        onSaved()
                    }
                }
            }) { Text("Save") }

            Spacer(modifier = Modifier.width(8.dp))
            OutlinedButton(onClick = onCancel) { Text("Cancel") }
        }
    }
}
