package com.example.localjobs.service

import org.json.JSONArray
import org.json.JSONObject
import java.io.File

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