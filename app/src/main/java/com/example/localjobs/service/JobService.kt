package com.example.localjobs.service

import com.example.localjobs.models.Job
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

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
