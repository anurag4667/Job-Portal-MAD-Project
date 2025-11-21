package com.example.localjobs.service

import com.example.localjobs.models.MockTest
import com.example.localjobs.models.OptionItem
import com.example.localjobs.models.Question
import org.json.JSONArray
import org.json.JSONObject
import java.io.File


private const val MOCK_TESTS_FILE = "mocktests.json"


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