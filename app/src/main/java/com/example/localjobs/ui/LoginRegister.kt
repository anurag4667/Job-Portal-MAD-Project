package com.example.localjobs.ui

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.localjobs.Auth.authenticate
import com.example.localjobs.models.Profile
import com.example.localjobs.models.User
import com.example.localjobs.service.addUser
import com.example.localjobs.service.loadUsers
import com.example.localjobs.service.upsertProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File


@Composable
fun LoginScreen(onLogin: (User) -> Unit, onRegister: () -> Unit, filesDir: File) {
    val ctx = LocalContext.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text("Job Spot", style = MaterialTheme.typography.h4)
        Spacer(modifier = Modifier.height(24.dp))
        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Password") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            scope.launch(Dispatchers.IO) {
                val user = authenticate(filesDir, email.trim(), password)
                if (user != null) {
                    launch(Dispatchers.Main) { onLogin(user) }
                } else {
                    launch(Dispatchers.Main) { Toast.makeText(ctx, "Invalid credentials", Toast.LENGTH_SHORT).show() }
                }
            }
        }, modifier = Modifier.fillMaxWidth()) { Text("Login") }
        Spacer(modifier = Modifier.height(8.dp))
        TextButton(onClick = onRegister) { Text("Register new account") }
    }
}

@Composable
fun RegisterScreen(onRegistered: (User) -> Unit, onBack: () -> Unit, filesDir: File) {
    val ctx = LocalContext.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text("Register", style = MaterialTheme.typography.h5)
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Password") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            if (email.isBlank() || password.isBlank()) { Toast.makeText(ctx, "Fill both fields", Toast.LENGTH_SHORT).show(); return@Button }
            val existing = loadUsers(filesDir)
            if (existing.any { it.email.equals(email.trim(), ignoreCase = true) }) { Toast.makeText(ctx, "Email already registered", Toast.LENGTH_SHORT).show(); return@Button }
            val user = User(email.trim(), password, false)
            val scope2 = scope
            scope2.launch(Dispatchers.IO) {
                addUser(filesDir, user)
                // create empty profile placeholder
                upsertProfile(filesDir, Profile(user.email, "", "", "", "", ""))
                launch(Dispatchers.Main) { onRegistered(user) }
            }
        }, modifier = Modifier.fillMaxWidth()) { Text("Create account") }
        Spacer(modifier = Modifier.height(8.dp))
        TextButton(onClick = onBack) { Text("Back to Login") }
    }
}