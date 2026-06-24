package com.example.timetracker

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "app_login.db"
        private const val DATABASE_VERSION = 2
        const val TABLE_NAME = "users"
        const val COLUMN_ID = "id"
        const val COLUMN_USERNAME = "username"
        const val COLUMN_EMAIL = "email"
        const val COLUMN_PASSWORD = "password"

        const val TABLE_ACTIVITIES = "activities"
        const val COLUMN_ACT_ID = "act_id"
        const val COLUMN_ACT_NAME = "activity_name"
        const val COLUMN_ACT_PROJECT = "project"
        const val COLUMN_ACT_CATEGORY = "category"
        const val COLUMN_ACT_DURATION = "duration" // in minutes
        const val COLUMN_ACT_NOTES = "notes"
        const val COLUMN_ACT_DATE = "date_millis"
        const val COLUMN_ACT_TIME = "start_time"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createTable = ("CREATE TABLE " + TABLE_NAME + " ("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_USERNAME + " TEXT UNIQUE, "
                + COLUMN_EMAIL + " TEXT UNIQUE, "
                + COLUMN_PASSWORD + " TEXT)")
        db?.execSQL(createTable)

        val createActivitiesTable = ("CREATE TABLE " + TABLE_ACTIVITIES + " ("
                + COLUMN_ACT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_ACT_NAME + " TEXT, "
                + COLUMN_ACT_PROJECT + " TEXT, "
                + COLUMN_ACT_CATEGORY + " TEXT, "
                + COLUMN_ACT_DURATION + " INTEGER, "
                + COLUMN_ACT_NOTES + " TEXT, "
                + COLUMN_ACT_DATE + " INTEGER, "
                + COLUMN_ACT_TIME + " TEXT)")
        db?.execSQL(createActivitiesTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            val createActivitiesTable = ("CREATE TABLE " + TABLE_ACTIVITIES + " ("
                    + COLUMN_ACT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + COLUMN_ACT_NAME + " TEXT, "
                    + COLUMN_ACT_PROJECT + " TEXT, "
                    + COLUMN_ACT_CATEGORY + " TEXT, "
                    + COLUMN_ACT_DURATION + " INTEGER, "
                    + COLUMN_ACT_NOTES + " TEXT, "
                    + COLUMN_ACT_DATE + " INTEGER, "
                    + COLUMN_ACT_TIME + " TEXT)")
            db?.execSQL(createActivitiesTable)
        }
    }

    fun addUser(username: String, email: String, password: String): Long {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(COLUMN_USERNAME, username)
        values.put(COLUMN_EMAIL, email)
        values.put(COLUMN_PASSWORD, password)
        val result = db.insert(TABLE_NAME, null, values)
        db.close()
        return result
    }

    fun addActivity(name: String, project: String, category: String, duration: Int, notes: String, date: Long, time: String): Long {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(COLUMN_ACT_NAME, name)
        values.put(COLUMN_ACT_PROJECT, project)
        values.put(COLUMN_ACT_CATEGORY, category)
        values.put(COLUMN_ACT_DURATION, duration)
        values.put(COLUMN_ACT_NOTES, notes)
        values.put(COLUMN_ACT_DATE, date)
        values.put(COLUMN_ACT_TIME, time)
        val result = db.insert(TABLE_ACTIVITIES, null, values)
        db.close()
        return result
    }

    fun getActivitiesByDate(dateMillis: Long): List<ActivityRecord> {
        val db = this.readableDatabase
        val list = mutableListOf<ActivityRecord>()
        
        val cal = java.util.Calendar.getInstance()
        cal.timeInMillis = dateMillis
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
        cal.set(java.util.Calendar.MINUTE, 0)
        cal.set(java.util.Calendar.SECOND, 0)
        cal.set(java.util.Calendar.MILLISECOND, 0)
        val start = cal.timeInMillis
        val end = start + (24 * 60 * 60 * 1000) - 1

        val cursor = db.rawQuery("SELECT * FROM $TABLE_ACTIVITIES WHERE $COLUMN_ACT_DATE BETWEEN ? AND ?", arrayOf(start.toString(), end.toString()))
        if (cursor.moveToFirst()) {
            do {
                list.add(ActivityRecord(
                    cursor.getInt(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getString(3),
                    cursor.getInt(4),
                    cursor.getString(5),
                    cursor.getLong(6),
                    cursor.getString(7)
                ))
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return list
    }

    fun getAllActivities(): List<ActivityRecord> {
        val db = this.readableDatabase
        val list = mutableListOf<ActivityRecord>()
        val cursor = db.rawQuery("SELECT * FROM $TABLE_ACTIVITIES", null)
        if (cursor.moveToFirst()) {
            do {
                list.add(ActivityRecord(
                    cursor.getInt(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getString(3),
                    cursor.getInt(4),
                    cursor.getString(5),
                    cursor.getLong(6),
                    cursor.getString(7)
                ))
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return list
    }

    fun checkUser(usernameOrEmail: String, password: String): Boolean {
        val db = this.readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM $TABLE_NAME WHERE ($COLUMN_USERNAME = ? OR $COLUMN_EMAIL = ?) AND $COLUMN_PASSWORD = ?",
            arrayOf(usernameOrEmail, usernameOrEmail, password)
        )
        val exists = cursor.count > 0
        cursor.close()
        db.close()
        return exists
    }

    fun isUsernameTaken(username: String): Boolean {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_NAME WHERE $COLUMN_USERNAME = ?", arrayOf(username))
        val exists = cursor.count > 0
        cursor.close()
        db.close()
        return exists
    }

    fun isEmailTaken(email: String): Boolean {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_NAME WHERE $COLUMN_EMAIL = ?", arrayOf(email))
        val exists = cursor.count > 0
        cursor.close()
        db.close()
        return exists
    }

    fun getUserDetails(usernameOrEmail: String): Map<String, String>? {
        val db = this.readableDatabase
        val cursor = db.rawQuery(
            "SELECT $COLUMN_USERNAME, $COLUMN_EMAIL FROM $TABLE_NAME WHERE $COLUMN_USERNAME = ? OR $COLUMN_EMAIL = ?",
            arrayOf(usernameOrEmail, usernameOrEmail)
        )
        
        var userDetails: Map<String, String>? = null
        if (cursor.moveToFirst()) {
            userDetails = mapOf(
                "username" to cursor.getString(0),
                "email" to cursor.getString(1)
            )
        }
        cursor.close()
        db.close()
        return userDetails
    }
}

data class ActivityRecord(
    val id: Int,
    val name: String,
    val project: String,
    val category: String,
    val duration: Int,
    val notes: String,
    val dateMillis: Long,
    val startTime: String
)