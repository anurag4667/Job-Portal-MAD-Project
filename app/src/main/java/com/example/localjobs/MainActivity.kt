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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Ensure users file exists and admin seeded
        seedAdminIfNeeded()
        // Ensure jobs, profiles & applications & mocktests & testresults file exist
        ensureFileExists("jobs.json")
        ensureFileExists("profiles.json")
        ensureFileExists("applications.json")
        ensureFileExists("mocktests.json")
        ensureFileExists("testresults.json")

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

// ------------------------
// Mock tests: storage & helpers (file-based JSON)
// ------------------------

private const val MOCK_TESTS_FILE = "mocktests.json"
private const val TEST_RESULTS_FILE = "testresults.json"

fun loadMockTests(filesDir: File): List<MockTest> {
    val f = File(filesDir, MOCK_TESTS_FILE)
    if (!f.exists()) return emptyList()
    val content = f.readText()
    val list = mutableListOf<MockTest>()
    try {
        val arr = JSONArray(content)
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            val qArr = obj.getJSONArray("questions")
            val questions = mutableListOf<Question>()
            for (q in 0 until qArr.length()) {
                val qObj = qArr.getJSONObject(q)
                val optsArr = qObj.getJSONArray("options")
                val opts = mutableListOf<OptionItem>()
                for (o in 0 until optsArr.length()) {
                    val oObj = optsArr.getJSONObject(o)
                    opts.add(OptionItem(oObj.getInt("index"), oObj.getString("text")))
                }
                questions.add(
                    Question(
                        qObj.getString("id"),
                        qObj.getString("text"),
                        opts,
                        qObj.getInt("correctIndex"),
                        qObj.optInt("marks", 1)
                    )
                )
            }
            list.add(
                MockTest(
                    obj.getString("id"),
                    obj.getString("title"),
                    obj.optString("description", ""),
                    if (obj.has("timeLimitMinutes")) obj.optInt("timeLimitMinutes") else null,
                    questions
                )
            )
        }
    } catch (e: Exception) {
        // ignore parse errors
    }
    return list
}

fun saveMockTests(filesDir: File, tests: List<MockTest>) {
    val arr = JSONArray()
    for (t in tests) {
        val tObj = JSONObject()
        tObj.put("id", t.id)
        tObj.put("title", t.title)
        tObj.put("description", t.description)
        if (t.timeLimitMinutes != null) tObj.put("timeLimitMinutes", t.timeLimitMinutes)
        val qArr = JSONArray()
        for (q in t.questions) {
            val qObj = JSONObject()
            qObj.put("id", q.id)
            qObj.put("text", q.text)
            qObj.put("correctIndex", q.correctIndex)
            qObj.put("marks", q.marks)
            val optsArr = JSONArray()
            for (o in q.options) {
                val oObj = JSONObject()
                oObj.put("index", o.index)
                oObj.put("text", o.text)
                optsArr.put(oObj)
            }
            qObj.put("options", optsArr)
            qArr.put(qObj)
        }
        tObj.put("questions", qArr)
        arr.put(tObj)
    }
    File(filesDir, MOCK_TESTS_FILE).writeText(arr.toString())
}

fun addMockTest(filesDir: File, test: MockTest) {
    val tests = loadMockTests(filesDir).toMutableList()
    tests.add(test)
    saveMockTests(filesDir, tests)
}

fun getMockTestById(filesDir: File, id: String): MockTest? {
    return loadMockTests(filesDir).firstOrNull { it.id == id }
}

// Test results helpers
fun loadTestResults(filesDir: File): List<TestResult> {
    val f = File(filesDir, TEST_RESULTS_FILE)
    if (!f.exists()) return emptyList()
    val content = f.readText()
    val list = mutableListOf<TestResult>()
    try {
        val arr = JSONArray(content)
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            val ansArr = obj.getJSONArray("answers")
            val answers = mutableListOf<Answer>()
            for (a in 0 until ansArr.length()) {
                val aObj = ansArr.getJSONObject(a)
                answers.add(Answer(aObj.getString("questionId"), aObj.getInt("selectedIndex"), aObj.optBoolean("correct", false)))
            }
            list.add(
                TestResult(
                    obj.getString("id"),
                    obj.getString("testId"),
                    obj.getString("userEmail"),
                    obj.getInt("score"),
                    obj.getInt("maxScore"),
                    obj.getDouble("percentage"),
                    answers,
                    obj.optLong("createdAt", System.currentTimeMillis())
                )
            )
        }
    } catch (e: Exception) { }
    return list
}

fun saveTestResults(filesDir: File, results: List<TestResult>) {
    val arr = JSONArray()
    for (r in results) {
        val rObj = JSONObject()
        rObj.put("id", r.id)
        rObj.put("testId", r.testId)
        rObj.put("userEmail", r.userEmail)
        rObj.put("score", r.score)
        rObj.put("maxScore", r.maxScore)
        rObj.put("percentage", r.percentage)
        rObj.put("createdAt", r.createdAt)
        val aArr = JSONArray()
        for (a in r.answers) {
            val aObj = JSONObject()
            aObj.put("questionId", a.questionId)
            aObj.put("selectedIndex", a.selectedIndex)
            aObj.put("correct", a.correct)
            aArr.put(aObj)
        }
        rObj.put("answers", aArr)
        arr.put(rObj)
    }
    File(filesDir, TEST_RESULTS_FILE).writeText(arr.toString())
}

fun addTestResult(filesDir: File, result: TestResult) {
    val results = loadTestResults(filesDir).toMutableList()
    results.add(result)
    saveTestResults(filesDir, results)
}

fun getResultsForTest(filesDir: File, testId: String): List<TestResult> {
    return loadTestResults(filesDir).filter { it.testId == testId }.sortedByDescending { it.createdAt }
}

// --- Compose UI ---

@Composable
fun MockTestsList(currentUser: User, filesDir: File, onBack: () -> Unit, onAttempt: (String) -> Unit, onCreate: () -> Unit, onViewResults: (String) -> Unit) {
    val scope = rememberCoroutineScope()
    var tests by remember { mutableStateOf<List<MockTest>>(emptyList()) }

    LaunchedEffect(Unit) {
        tests = loadMockTests(filesDir)
    }

    Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Mock Tests", style = MaterialTheme.typography.h5)
            Row {
                if (currentUser.isAdmin) {
                    Button(onClick = onCreate) { Text("Post Mock Test") }
                    Spacer(modifier = Modifier.width(8.dp))
                }
                OutlinedButton(onClick = onBack) { Text("Back") }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))

        if (tests.isEmpty()) {
            Text("No mock tests available.")
        } else {
            LazyColumn {
                items(tests) { t ->
                    Card(modifier = Modifier.fillMaxWidth().padding(8.dp), elevation = 4.dp) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(t.title, style = MaterialTheme.typography.h6)
                            Text(t.description, style = MaterialTheme.typography.body2)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row {
                                Button(onClick = { onAttempt(t.id) }) { Text("Attempt") }
                                Spacer(modifier = Modifier.width(8.dp))
                                if (currentUser.isAdmin) {
                                    OutlinedButton(onClick = { onViewResults(t.id) }) { Text("Results") }
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
fun AdminCreateMockTest(filesDir: File, onSaved: () -> Unit, onCancel: () -> Unit) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var timeLimitStr by remember { mutableStateOf("") }
    // dynamic list of questions builder
    var questions by remember { mutableStateOf<List<Question>>(emptyList()) }

    // helpers to add/remove question
    fun addEmptyQuestion() {
        val q = Question(
            id = UUID.randomUUID().toString(),
            text = "",
            options = listOf(
                OptionItem(0, ""),
                OptionItem(1, ""),
                OptionItem(2, ""),
                OptionItem(3, "")
            ),
            correctIndex = 0,
            marks = 1
        )
        questions = questions + q
    }

    Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Create Mock Test", style = MaterialTheme.typography.h5)
            Row {
                OutlinedButton(onClick = onCancel) { Text("Cancel") }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = timeLimitStr, onValueChange = { timeLimitStr = it }, label = { Text("Time limit (minutes, optional)") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(12.dp))

        Text("Questions", style = MaterialTheme.typography.h6)
        Spacer(modifier = Modifier.height(8.dp))

        // Questions editor
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(questions) { q ->
                var qText by remember { mutableStateOf(q.text) }
                // local copy of options
                var opt0 by remember { mutableStateOf(q.options.getOrNull(0)?.text ?: "") }
                var opt1 by remember { mutableStateOf(q.options.getOrNull(1)?.text ?: "") }
                var opt2 by remember { mutableStateOf(q.options.getOrNull(2)?.text ?: "") }
                var opt3 by remember { mutableStateOf(q.options.getOrNull(3)?.text ?: "") }
                var correct by remember { mutableStateOf(q.correctIndex) }
                var marks by remember { mutableStateOf(q.marks.toString()) }

                Card(modifier = Modifier.fillMaxWidth().padding(8.dp), elevation = 2.dp) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        OutlinedTextField(value = qText, onValueChange = {
                            qText = it
                            questions = questions.map { if (it.id == q.id) it.copy(text = qText) else it }
                        }, label = { Text("Question") }, modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(value = opt0, onValueChange = {
                            opt0 = it
                            questions = questions.map { if (it.id == q.id) it.copy(options = listOf(OptionItem(0, opt0), OptionItem(1, opt1), OptionItem(2, opt2), OptionItem(3, opt3))) else it }
                        }, label = { Text("Option A") }, modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(value = opt1, onValueChange = {
                            opt1 = it
                            questions = questions.map { if (it.id == q.id) it.copy(options = listOf(OptionItem(0, opt0), OptionItem(1, opt1), OptionItem(2, opt2), OptionItem(3, opt3))) else it }
                        }, label = { Text("Option B") }, modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(value = opt2, onValueChange = {
                            opt2 = it
                            questions = questions.map { if (it.id == q.id) it.copy(options = listOf(OptionItem(0, opt0), OptionItem(1, opt1), OptionItem(2, opt2), OptionItem(3, opt3))) else it }
                        }, label = { Text("Option C") }, modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(value = opt3, onValueChange = {
                            opt3 = it
                            questions = questions.map { if (it.id == q.id) it.copy(options = listOf(OptionItem(0, opt0), OptionItem(1, opt1), OptionItem(2, opt2), OptionItem(3, opt3))) else it }
                        }, label = { Text("Option D") }, modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Correct option: ")
                            Spacer(modifier = Modifier.width(8.dp))
                            DropdownMenuCorrectIndex(selected = correct, onSelected = {
                                correct = it
                                questions = questions.map { if (it.id == q.id) it.copy(correctIndex = correct) else it }
                            })
                            Spacer(modifier = Modifier.width(12.dp))
                            OutlinedTextField(value = marks, onValueChange = {
                                marks = it.filter { ch -> ch.isDigit() }
                                val markInt = marks.toIntOrNull() ?: 1
                                questions = questions.map { if (it.id == q.id) it.copy(marks = markInt) else it }
                            }, label = { Text("Marks") }, modifier = Modifier.width(90.dp))
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row {
                            OutlinedButton(onClick = {
                                // remove question
                                questions = questions.filterNot { it.id == q.id }
                            }) { Text("Remove") }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Row {
            Button(onClick = { addEmptyQuestion() }) { Text("Add Question") }
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = {
                // validate and save
                if (title.isBlank()) { Toast.makeText(ctx, "Please enter title", Toast.LENGTH_SHORT).show(); return@Button }
                if (questions.isEmpty()) { Toast.makeText(ctx, "Add at least one question", Toast.LENGTH_SHORT).show(); return@Button }
                // basic validation: each question must have 4 non-empty options
                for (q in questions) {
                    if (q.text.isBlank()) { Toast.makeText(ctx, "Each question must have text", Toast.LENGTH_SHORT).show(); return@Button }
                    if (q.options.size != 4 || q.options.any { it.text.isBlank() }) { Toast.makeText(ctx, "Each question must have 4 options filled", Toast.LENGTH_SHORT).show(); return@Button }
                    if (q.correctIndex !in 0..3) { Toast.makeText(ctx, "Correct index must be 0..3", Toast.LENGTH_SHORT).show(); return@Button }
                }
                val timeLimit = timeLimitStr.toIntOrNull()
                val test = MockTest(UUID.randomUUID().toString(), title.trim(), desc.trim(), timeLimit, questions)
                scope.launch(Dispatchers.IO) {
                    addMockTest(filesDir, test)
                    launch(Dispatchers.Main) {
                        Toast.makeText(ctx, "Mock test created", Toast.LENGTH_SHORT).show()
                        onSaved()
                    }
                }
            }) { Text("Save Test") }
        }
    }
}

@Composable
fun DropdownMenuCorrectIndex(selected: Int, onSelected: (Int) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val options = listOf("A", "B", "C", "D")
    Box {
        OutlinedButton(onClick = { expanded = true }) {
            Text(options.getOrNull(selected) ?: "A")
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEachIndexed { idx, label ->
                DropdownMenuItem(onClick = { expanded = false; onSelected(idx) }) {
                    Text(label)
                }
            }
        }
    }
}

@Composable
fun AttemptMockTest(currentUser: User, testId: String, filesDir: File, onBack: () -> Unit, onResult: (TestResult) -> Unit) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    var test by remember { mutableStateOf<MockTest?>(null) }

    // map questionId -> selectedIndex
    var answersState by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(testId) {
        test = getMockTestById(filesDir, testId)
        loading = false
    }

    if (loading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }

    val t = test
    if (t == null) {
        Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
            Text("Test not found")
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(onClick = onBack) { Text("Back") }
        }
        return
    }

    Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(t.title, style = MaterialTheme.typography.h5)
            OutlinedButton(onClick = onBack) { Text("Back") }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(t.description)
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(t.questions) { q ->
                Card(modifier = Modifier.fillMaxWidth().padding(8.dp), elevation = 2.dp) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(q.text, style = MaterialTheme.typography.subtitle1)
                        Spacer(modifier = Modifier.height(8.dp))
                        q.options.forEach { o ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = (answersState[q.id] ?: -1) == o.index,
                                    onClick = { answersState = answersState.plus(q.id to o.index) }
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(o.text)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = {
            // Build answers and score
            val userAnswers = t.questions.map { q ->
                val sel = answersState[q.id]
                val selectedIndex = sel ?: -1
                val correct = selectedIndex == q.correctIndex
                Pair(q, selectedIndex)
            }

            // compute score: unanswered counted as wrong
            var score = 0
            var maxScore = 0
            val answersSaved = mutableListOf<Answer>()
            for ((q, sel) in userAnswers) {
                maxScore += q.marks
                val correct = sel == q.correctIndex
                if (correct) score += q.marks
                answersSaved.add(Answer(q.id, if (sel < 0) -1 else sel, correct))
            }
            val perc = if (maxScore == 0) 0.0 else (score.toDouble() / maxScore.toDouble()) * 100.0
            val result = TestResult(UUID.randomUUID().toString(), t.id, currentUser.email, score, maxScore, perc, answersSaved, System.currentTimeMillis())
            scope.launch(Dispatchers.IO) {
                addTestResult(filesDir, result)
                launch(Dispatchers.Main) {
                    onResult(result)
                }
            }
        }, modifier = Modifier.fillMaxWidth()) {
            Text("Submit and See Score")
        }
    }
}

@Composable
fun MockTestResultScreen(result: TestResult, filesDir: File, onBack: () -> Unit) {
    val test = getMockTestById(filesDir, result.testId)
    Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Result", style = MaterialTheme.typography.h5)
            OutlinedButton(onClick = onBack) { Text("Back") }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text("Score: ${result.score} / ${result.maxScore}", style = MaterialTheme.typography.h6)
        Text("Percentage: ${"%.2f".format(result.percentage)}%", style = MaterialTheme.typography.body1)
        Spacer(modifier = Modifier.height(12.dp))
        Text("Question-wise:", style = MaterialTheme.typography.subtitle1)
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn {
            items(result.answers) { ans ->
                val q = test?.questions?.firstOrNull { it.id == ans.questionId }
                Card(modifier = Modifier.fillMaxWidth().padding(8.dp), elevation = 2.dp) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(q?.text ?: "Question")
                        Spacer(modifier = Modifier.height(6.dp))
                        val selectedText = q?.options?.firstOrNull { it.index == ans.selectedIndex }?.text ?: "No answer"
                        val correctText = q?.options?.firstOrNull { it.index == q.correctIndex }?.text ?: "N/A"
                        Text("Your answer: $selectedText")
                        Text("Correct answer: $correctText")
                        Text(if (ans.correct) "Correct" else "Wrong")
                    }
                }
            }
        }
    }
}

@Composable
fun ViewMockTestResults(filesDir: File, testId: String, onBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    var results by remember { mutableStateOf<List<TestResult>>(emptyList()) }
    LaunchedEffect(testId) {
        results = getResultsForTest(filesDir, testId)
    }

    Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Test Results", style = MaterialTheme.typography.h5)
            OutlinedButton(onClick = onBack) { Text("Back") }
        }
        Spacer(modifier = Modifier.height(12.dp))
        if (results.isEmpty()) {
            Text("No attempts yet.")
        } else {
            LazyColumn {
                items(results) { r ->
                    Card(modifier = Modifier.fillMaxWidth().padding(8.dp), elevation = 2.dp) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text("User: ${r.userEmail}")
                            Text("Score: ${r.score} / ${r.maxScore}")
                            Text("Percent: ${"%.2f".format(r.percentage)}%")
                            Text("At: ${Date(r.createdAt)}")
                        }
                    }
                }
            }
        }
    }
}

// --- Main app root with navigation state updated to include mock tests ---

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
                    if (currentUser != null) JobsScreen(currentUser = currentUser!!,
                        onLogout = { currentUser = null; screen = "login" },
                        onPostJob = { screen = "post" },
                        onProfile = {
                            if (!currentUser!!.isAdmin) screen = "profile"
                        },
                        onViewApplications = { jobId -> selectedJobId = jobId; screen = "job_applications" },
                        onMockTests = { selectedTestId = null; screen = "mock_tests" },
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
                scope.launch {
                    kotlinx.coroutines.withContext(Dispatchers.IO) {
                        upsertProfile(filesDir, profile)
                    }
                    Toast.makeText(ctx, "Profile saved", Toast.LENGTH_SHORT).show()
                    onSaved()
                }
            }) { Text("Save") }


            Spacer(modifier = Modifier.width(8.dp))
            OutlinedButton(onClick = onCancel) { Text("Cancel") }
        }
    }
}
