package com.example.biometriclogin

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class SignupActivity : AppCompatActivity() {

    private lateinit var emailEditText: EditText
    private lateinit var submitButton: Button
    private lateinit var passwordEditText: EditText
    private lateinit var confirmPasswordEditText: EditText
    private lateinit var signInBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_signup)

        emailEditText = findViewById(R.id.signInEmail)
        passwordEditText = findViewById(R.id.signInPassword)
        confirmPasswordEditText = findViewById(R.id.signUpPassword2)
        submitButton = findViewById(R.id.LogInBtn)
        signInBtn = findViewById(R.id.signInBtn)
        val db = DatabaseHelper(this)

        submitButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            val confirmPassword = confirmPasswordEditText.text.toString().trim()

            if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password!= confirmPassword) {
                Toast.makeText(this, "Mismatch password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!EmailValidator.isValidEmail(email)) {
                Toast.makeText(this, "Invalid email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            db.addUser(email, password);
            val intent = Intent(this, SigninActivity::class.java)
            startActivity(intent)
            finish()
        }

        signInBtn.setOnClickListener {
            val intent = Intent(this, SigninActivity::class.java)
            startActivity(intent)
            finish()
        }


    }


}