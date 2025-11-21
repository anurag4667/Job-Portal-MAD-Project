package com.example.localjobs.service

import com.example.localjobs.models.Answer
import com.example.localjobs.models.TestResult
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

private const val TEST_RESULTS_FILE = "testresults.json"
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
