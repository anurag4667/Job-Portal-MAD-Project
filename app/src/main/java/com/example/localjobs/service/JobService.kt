package com.example.localjobs.service

import com.example.localjobs.models.Job
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

fun loadJobs(filesDir: File): List<Job> {
    val f = File(filesDir, "jobs.json")
    val list = mutableListOf<Job>()

    // ---------------- Existing local JSON load ----------------
    if (f.exists()) {
        val content = f.readText()
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
    }

    // ---------------- NEW: Fetch 10 jobs from API ----------------
    try {
        val url = URL("http://10.0.2.2:8000/scrape-amazon-jobs")
        // For emulator use: http://10.0.2.2:8000/scrape-amazon-jobs

        val conn = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 25000
            readTimeout = 25000
        }

        if (conn.responseCode == HttpURLConnection.HTTP_OK) {
            val response = conn.inputStream.bufferedReader().use { it.readText() }
            val json = JSONObject(response)
            val jobsArray = json.getJSONArray("jobs")

            val limit = minOf(10, jobsArray.length())
            for (i in 0 until limit) {
                val j = jobsArray.getJSONObject(i)

                list.add(
                    Job(
                        id = j.getString("job_id"),
                        title = j.optString("title", "Untitled Job"),
                        company = "Amazon",
                        salary = "Not specified"
                    )
                )
            }
        }
        conn.disconnect()

    } catch (e: Exception) {
        e.printStackTrace()
        // API failure shouldn't crash loadJobs
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
