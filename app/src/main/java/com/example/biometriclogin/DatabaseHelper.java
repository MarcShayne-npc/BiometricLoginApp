package com.example.biometriclogin;

import android.content.Context;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.hardware.biometrics.BiometricPrompt;
import android.location.Location;
import android.util.Log;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "UserDatabase.db";
    private static final int DATABASE_VERSION = 1;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_USER_TABLE = "CREATE TABLE users ( " +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "email TEXT, " +
                "password TEXT, " +
                "biometric_registered INTEGER DEFAULT 0 )";
        db.execSQL(CREATE_USER_TABLE);

        String CREATE_CHECK_IN_OUT_TABLE = "CREATE TABLE check_in_out_records ( " +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "email TEXT, " +
                "check_in_out_time TEXT, " +
                "action_type TEXT )"; // Add action_type column
        db.execSQL(CREATE_CHECK_IN_OUT_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS users");
        onCreate(db);
    }

    public void addUser(String email, String password) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("email", email);
        values.put("password", password);

        long newRowId = db.insert("users", null, values);
        db.close();

        if (newRowId == -1) {
            Log.e("DatabaseHelper", "Failed to insert row for " + email);
        } else {
            Log.i("DatabaseHelper", "Inserted row with rowId: " + newRowId);
        }
    }

    public boolean authenticateUser(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM users WHERE email = ? AND password = ?", new String[]{email, password});

        if (cursor.moveToFirst()) {
            // User exists and password matches
            cursor.close();
            return true;
        } else {
            // User does not exist or password does not match
            cursor.close();
            return false;
        }
    }

    public boolean isBiometricRegistered(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT biometric_registered FROM users WHERE email = ?", new String[]{email});
        Log.e("DatabaseHelper", "is Registered " + cursor);
        if (cursor.moveToFirst()) {
            int isRegistered = cursor.getInt(0);
            cursor.close();
            return isRegistered == 1;
        } else {
            cursor.close();
            return false;
        }
    }

    public void saveBiometricRegistrationStatus(String email, boolean isRegistered) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("biometric_registered", isRegistered ? 1 : 0);

        int rowsAffected = db.update("users", values, "email = ?", new String[]{email});
        db.close();

        if (rowsAffected == 0) {
            Log.e("DatabaseHelper", "Failed to update biometric registration status for " + email);
        } else {
            Log.i("DatabaseHelper", "Updated biometric registration status for " + email);
        }
    }

    public boolean isBiometricMatch(String email) {
        // This method should return true if the biometric matches, false otherwise
        return true; // Placeholder return value
    }

    public void saveCheckInOut(String email, LocalDateTime checkInOutTime, String actionType) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("email", email);
        values.put("check_in_out_time", checkInOutTime.toString()); // Convert LocalDateTime to String
        values.put("action_type", actionType);
        Log.e("DatabaseHelper", "check in email: " + email);


        long newRowId = db.insert("check_in_out_records", null, values);
        db.close();

        if (newRowId == -1) {
            Log.e("DatabaseHelper", "Failed to insert check-in/out record for " + email);
        } else {
            Log.i("DatabaseHelper", "Inserted check-in/out record with rowId: " + newRowId);
        }
    }

    public boolean hasCheckedInToday(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT EXISTS(SELECT 1 FROM check_in_out_records WHERE email = ? AND date(check_in_out_time) = date('now') AND action_type = 'check-in')";
        Cursor cursor = db.rawQuery(query, new String[]{email});
        boolean hasCheckedIn = cursor.moveToFirst() && cursor.getInt(0) == 1;
        cursor.close();

        if (hasCheckedIn) {
            Log.e("DatabaseHelper", "User " + email + " has checked in today.");
        } else {
            Log.e("DatabaseHelper", "User " + email + " has not checked in today.");
        }

        return hasCheckedIn;
    }
    public List<AttendanceRecord> getAttendanceRecords(String email) {
        List<AttendanceRecord> records = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT check_in_out_time, action_type FROM check_in_out_records WHERE email = ? ORDER BY check_in_out_time DESC", new String[]{email});

        while (cursor.moveToNext()) {
            int timeIndex = cursor.getColumnIndex("check_in_out_time");
            int actionIndex = cursor.getColumnIndex("action_type");

            if (timeIndex >= 0 && actionIndex >= 0) {
                String time = cursor.getString(timeIndex);
                String action = cursor.getString(actionIndex);
                records.add(new AttendanceRecord(time.substring(0, 10), time.substring(11, 19), action));
            } else {
                // Handle the case where the column does not exist
                // This could be logging an error or throwing an exception
                Log.e("DatabaseHelper", "Column not found in cursor");
            }
        }
        cursor.close();
        return records;
    }


    public boolean hasCheckedOutToday(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT EXISTS(SELECT 1 FROM check_in_out_records WHERE email = ? AND date(check_in_out_time) = date('now') AND action_type = 'check-out')";
        Cursor cursor = db.rawQuery(query, new String[]{email});
        boolean hasCheckedOut = cursor.moveToFirst() && cursor.getInt(0) == 1;
        cursor.close();

        if (hasCheckedOut) {
            Log.e("DatabaseHelper", "User " + email + " has checked out today.");
        } else {
            Log.e("DatabaseHelper", "User " + email + " has not checked out today.");
        }

        return hasCheckedOut;
    }
    public boolean isUserAtOffice(Location currentLocation) {
        // This method should return true if the user is at the office, false otherwise
        return true; // Placeholder return value
    }



}


