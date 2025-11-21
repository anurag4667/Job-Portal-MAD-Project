package com.example.localjobs

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.*

import com.example.localjobs.ui.AppRoot
import org.json.JSONArray
import org.json.JSONObject
import java.io.File


// test

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

