package com.example.localjobs.ui

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.localjobs.models.Profile
import com.example.localjobs.models.User
import com.example.localjobs.service.getProfileForEmail
import com.example.localjobs.service.upsertProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File


@Composable
fun ProfileScreen(currentUser: User, onEdit: () -> Unit, onBack: () -> Unit, filesDir: File) {
    val ctx = LocalContext.current
    var profile by remember { mutableStateOf<Profile?>(null) }

    LaunchedEffect(currentUser.email) {
        profile = getProfileForEmail(filesDir, currentUser.email)
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Profile", style = MaterialTheme.typography.h5)
            Row {
                OutlinedButton(onClick = onEdit) { Text("Edit") }
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedButton(onClick = onBack) { Text("Back") }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        if (profile == null) {
            Text("No profile found. Click Edit to create one.")
        } else {
            val p = profile!!
            Text("Email: ${p.email}", style = MaterialTheme.typography.subtitle2)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Name: ${p.name}")
            Text("College: ${p.college}")
            Text("DOB: ${p.dob}")
            Text("CGPA: ${p.cgpa}")
            Text("Resume: ${p.resumeLink}")
        }
    }
}

@Composable
fun EditProfileScreen(currentUser: User, onSaved: () -> Unit, onCancel: () -> Unit, filesDir: File) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    var name by remember { mutableStateOf("") }
    var college by remember { mutableStateOf("") }
    var dob by remember { mutableStateOf("") }
    var cgpa by remember { mutableStateOf("") }
    var resumeLink by remember { mutableStateOf("") }

    LaunchedEffect(currentUser.email) {
        val existing = getProfileForEmail(filesDir, currentUser.email)
        if (existing != null) {
            name = existing.name
            college = existing.college
            dob = existing.dob
            cgpa = existing.cgpa
            resumeLink = existing.resumeLink
        }
    }
    // testing
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Edit Profile", style = MaterialTheme.typography.h5)
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = college, onValueChange = { college = it }, label = { Text("College") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = dob, onValueChange = { dob = it }, label = { Text("DOB") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = cgpa, onValueChange = { cgpa = it }, label = { Text("CGPA") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = resumeLink, onValueChange = { resumeLink = it }, label = { Text("Resume (Google Docs link)") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(16.dp))

        Row {
            Button(onClick = {
                if (name.isBlank() || college.isBlank()) {
                    Toast.makeText(ctx, "Please fill name and college", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                val profile = Profile(currentUser.email, name.trim(), college.trim(), dob.trim(), cgpa.trim(), resumeLink.trim())
                scope.launch {
                    kotlinx.coroutines.withContext(Dispatchers.IO) {
                        upsertProfile(filesDir, profile)
                    }
                    Toast.makeText(ctx, "Profile saved", Toast.LENGTH_SHORT).show()
                    onSaved()
                }
            }) { Text("Save") }


            Spacer(modifier = Modifier.width(8.dp))
            OutlinedButton(onClick = onCancel) { Text("Cancel") }
        }
    }
}
