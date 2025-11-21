package com.example.localjobs.Auth

import com.example.localjobs.models.User
import com.example.localjobs.service.loadUsers
import java.io.File

// Authentication
fun authenticate(filesDir: File, email: String, password: String): User? {
    val users = loadUsers(filesDir)
    for (u in users) if (u.email.equals(email, ignoreCase = true) && u.password == password) return u
    return null
}