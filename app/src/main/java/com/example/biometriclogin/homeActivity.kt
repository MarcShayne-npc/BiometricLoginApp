package com.example.biometriclogin


import android.content.Context
import android.graphics.Color
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.biometric.BiometricPrompt
import java.time.LocalDateTime

data class AttendanceRecord(val date: String, val time: String, val action: String)

class AttendanceAdapter(context: Context, private val attendanceData: List<AttendanceRecord>) : ArrayAdapter<AttendanceRecord>(context, 0, attendanceData) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.attendance_item, parent, false)

        val attendanceRecord = attendanceData[position]

        val dateTextView = view.findViewById<TextView>(R.id.dateTextView)
        val timeTextView = view.findViewById<TextView>(R.id.timeTextView)
        val actionTextView = view.findViewById<TextView>(R.id.actionTextView)

        dateTextView.text = attendanceRecord.date
        timeTextView.text = attendanceRecord.time
        actionTextView.text = attendanceRecord.action

        return view
    }
}

class homeActivity : AppCompatActivity(){

    private lateinit var db: DatabaseHelper
    private lateinit var userEmail: String
    private lateinit var status: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)

        db = DatabaseHelper(this)
        userEmail = intent.getStringExtra("email") ?: ""

        if (!db.isBiometricRegistered(userEmail)) {
            // Show alert asking the user to register their biometric
            showBiometricRegistrationAlert()
        } else {
            // Biometric already registered, show biometric prompt for login
            showBiometricPrompt()
        }

        val btnCheckIn = findViewById<Button>(R.id.checkInBtn)
        val btnCheckOut = findViewById<Button>(R.id.checkOutBtn)
        status = findViewById<TextView>(R.id.statusTxt)



        btnCheckIn.setOnClickListener {
            if (db.hasCheckedInToday(userEmail)) {
                showAlertDialog("Error", "You have already checked in & out today. Please try again tomorrow.")
                status.setText("Status: Checked in & out")
                status.setTextColor(Color.RED)
            } else {
                val currentDateTime = LocalDateTime.now()
                db.saveCheckInOut(userEmail, currentDateTime, "check-in") // Specify action type
                Toast.makeText(applicationContext, "Checked in successfully.", Toast.LENGTH_SHORT).show()
                status.setText("Status: Checked in")
                status.setTextColor(Color.GREEN)
            }
        }

        btnCheckOut.setOnClickListener {
            if (db.hasCheckedInToday(userEmail)) {
                // Check if the user has already checked out today. If not, allow them to check out.
                if (!db.hasCheckedOutToday(userEmail)) {
                    val currentDateTime = LocalDateTime.now()
                    db.saveCheckInOut(userEmail, currentDateTime, "check-out") // Specify action type
                    Toast.makeText(applicationContext, "Checked out successfully.", Toast.LENGTH_SHORT).show()
                    status.setText("Status: Checked out")
                    status.setTextColor(Color.BLUE)
                } else {
                    showAlertDialog("Error", "You have already checked out today. Please check in tomorrow.")
                    status.setText("Status: Checked in & out")
                    status.setTextColor(Color.RED)
                }
            } else {
                showAlertDialog("Error", "You must check in before you can check out.")
            }
        }

        val attendanceListView = findViewById<ListView>(R.id.attendaceView)
        val viewAttendanceButton = findViewById<Button>(R.id.attendanceBtn)

        viewAttendanceButton.setOnClickListener {
            val attendanceData = db.getAttendanceRecords(userEmail)
            val adapter = AttendanceAdapter(this, attendanceData)
            attendanceListView.adapter = adapter
        }

    }

    private fun showBiometricRegistrationAlert() {
        AlertDialog.Builder(this)
            .setTitle("Biometric Registration")
            .setMessage("Please register your biometric credentials.")
            .setPositiveButton("Register") { _, _ ->
                // User clicked Register, show biometric prompt
                showBiometricPrompt()
            }
            .show()
    }

    private fun showAlertDialog(title: String, message: String): AlertDialog {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(title)
        builder.setMessage(message)
        builder.setPositiveButton("OK", null)
        val dialog = builder.create()
        dialog.show()
        return dialog
    }

    private fun showBiometricPrompt() {
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric login for my app")
            .setSubtitle("Log in using your biometric credential")
            .setNegativeButtonText("Cancel")
            .build()

        val biometricPrompt = BiometricPrompt(this, ContextCompat.getMainExecutor(this),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    // Check if the biometric entered matches the saved biometric
                    if (db.isBiometricMatch(userEmail)) {
                        // Biometric matches, proceed to location check
                        checkLocationAndPerformCheckInOut()
                        db.saveBiometricRegistrationStatus(userEmail,true)
                    }else{
                        showBiometricPrompt()
                    }

                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    // Handle error
                    if (errorCode == BiometricPrompt.ERROR_LOCKOUT) {
                        // Handle lockout, inform the user they need to wait
                        showAlertDialog("Error", "Too many attempts. Please wait for 30 seconds.")
                            .setOnDismissListener {
                            // Activity will finish when the dialog is dismissed
                            finish()
                        }
                    } else {
                        // Handle other errors, re-prompt the user
                        showBiometricPrompt()
                    }

                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    // Handle failure
                    showBiometricPrompt()

                }
            })

        biometricPrompt.authenticate(promptInfo)
    }

    private fun checkLocationAndPerformCheckInOut() {

        val isAtOffice = true
        val btnCheckIn = findViewById<Button>(R.id.checkInBtn)
        val btnCheckOut = findViewById<Button>(R.id.checkOutBtn)
        val userEmail = intent.getStringExtra("email") ?: ""



        if (isAtOffice) {
            btnCheckIn.isEnabled = true
            btnCheckOut.isEnabled = true

            Toast.makeText(applicationContext, "Check-in/out Available.", Toast.LENGTH_SHORT).show()

        } else {
            // User is not at the office, show an error message
            btnCheckIn.isEnabled = false
            btnCheckOut.isEnabled = false
            Toast.makeText(applicationContext, "You must be at the office to check-in/out.", Toast.LENGTH_SHORT).show()
        }
    }
}