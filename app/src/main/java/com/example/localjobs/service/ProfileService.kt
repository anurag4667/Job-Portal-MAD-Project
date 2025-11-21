package com.example.localjobs.service

import com.example.localjobs.models.Profile
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

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
    return com.example.localjobs.service.loadProfiles(filesDir)
        .firstOrNull { it.email.equals(email, ignoreCase = true) }
}

fun upsertProfile(filesDir: File, profile: Profile) {
    val list = com.example.localjobs.service.loadProfiles(filesDir).toMutableList()
    val idx = list.indexOfFirst { it.email.equals(profile.email, ignoreCase = true) }
    if (idx >= 0) list[idx] = profile else list.add(profile)
    saveProfiles(filesDir, list)
}
