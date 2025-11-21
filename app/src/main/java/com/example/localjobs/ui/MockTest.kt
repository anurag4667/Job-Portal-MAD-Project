package com.example.localjobs.ui

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.RadioButton
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
import com.example.localjobs.models.Answer
import com.example.localjobs.models.MockTest
import com.example.localjobs.models.OptionItem
import com.example.localjobs.models.Question
import com.example.localjobs.models.TestResult
import com.example.localjobs.models.User
import com.example.localjobs.service.addMockTest
import com.example.localjobs.service.addTestResult
import com.example.localjobs.service.getMockTestById
import com.example.localjobs.service.getResultsForTest
import com.example.localjobs.service.loadMockTests
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.util.Date
import java.util.UUID


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