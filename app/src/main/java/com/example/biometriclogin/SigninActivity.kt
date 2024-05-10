package com.example.biometriclogin

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class SigninActivity : AppCompatActivity() {

    private lateinit var email: EditText
    private lateinit var password: EditText
    private lateinit var login: Button
    private lateinit var db: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_signin)

        email = findViewById(R.id.signInEmail)
        password = findViewById(R.id.signInPassword)
        login = findViewById(R.id.LogInBtn)

        db = DatabaseHelper(this)

        login.setOnClickListener {
            val userEmail = email.text.toString()
            val userPassword = password.text.toString()

            if (userEmail.isEmpty() || userPassword.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val isAuthenticated = db.authenticateUser(userEmail, userPassword)

            if (isAuthenticated) {
                // Login successful, navigate to the next activity
                val intent = Intent(this, homeActivity::class.java)
                intent.putExtra("email", userEmail)
                startActivity(intent)
                finish() // Close the current activity
            } else {
                // Login failed, show an error message
                Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
