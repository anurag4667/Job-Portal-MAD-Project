package com.example.localjobs.service

import com.example.localjobs.models.User
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

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
