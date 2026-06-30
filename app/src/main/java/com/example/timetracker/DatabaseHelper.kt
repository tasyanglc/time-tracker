package com.example.timetracker

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "app_login.db"
        private const val DATABASE_VERSION = 3
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

        const val TABLE_PROJECTS = "projects"
        const val COLUMN_PROJ_ID = "proj_id"
        const val COLUMN_PROJ_NAME = "project_name"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createTable = ("CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_USERNAME + " TEXT UNIQUE, "
                + COLUMN_EMAIL + " TEXT UNIQUE, "
                + COLUMN_PASSWORD + " TEXT)")
        db?.execSQL(createTable)

        if (db != null) {
            ensureActivitiesTableExists(db)
            ensureProjectsTableExists(db)
        }
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        if (db != null) {
            ensureActivitiesTableExists(db)
            ensureProjectsTableExists(db)
        }
    }

    private fun ensureActivitiesTableExists(db: SQLiteDatabase) {
        try {
            val createActivitiesTable = ("CREATE TABLE IF NOT EXISTS " + TABLE_ACTIVITIES + " ("
                    + COLUMN_ACT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + COLUMN_ACT_NAME + " TEXT, "
                    + COLUMN_ACT_PROJECT + " TEXT, "
                    + COLUMN_ACT_CATEGORY + " TEXT, "
                    + COLUMN_ACT_DURATION + " INTEGER, "
                    + COLUMN_ACT_NOTES + " TEXT, "
                    + COLUMN_ACT_DATE + " INTEGER, "
                    + COLUMN_ACT_TIME + " TEXT)")
            db.execSQL(createActivitiesTable)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun addUser(username: String, email: String, password: String): Long {
        return try {
            val db = this.writableDatabase
            val values = ContentValues()
            values.put(COLUMN_USERNAME, username)
            values.put(COLUMN_EMAIL, email)
            values.put(COLUMN_PASSWORD, password)
            db.insert(TABLE_NAME, null, values)
        } catch (e: Exception) {
            e.printStackTrace()
            -1L
        }
    }

    fun addActivity(name: String, project: String, category: String, duration: Int, notes: String, date: Long, time: String): Long {
        return try {
            val db = this.writableDatabase
            ensureActivitiesTableExists(db)
            val values = ContentValues()
            values.put(COLUMN_ACT_NAME, name)
            values.put(COLUMN_ACT_PROJECT, project)
            values.put(COLUMN_ACT_CATEGORY, category)
            values.put(COLUMN_ACT_DURATION, duration)
            values.put(COLUMN_ACT_NOTES, notes)
            values.put(COLUMN_ACT_DATE, date)
            values.put(COLUMN_ACT_TIME, time)
            db.insert(TABLE_ACTIVITIES, null, values)
        } catch (e: Exception) {
            e.printStackTrace()
            -1L
        }
    }

    fun getActivitiesByDate(dateMillis: Long): List<ActivityRecord> {
        val list = mutableListOf<ActivityRecord>()
        try {
            val db = this.readableDatabase
            val writeDb = this.writableDatabase
            ensureActivitiesTableExists(writeDb)

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
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    fun getAllActivities(): List<ActivityRecord> {
        val list = mutableListOf<ActivityRecord>()
        try {
            val db = this.readableDatabase
            val writeDb = this.writableDatabase
            ensureActivitiesTableExists(writeDb)

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
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    fun checkUser(usernameOrEmail: String, password: String): Boolean {
        return try {
            val db = this.readableDatabase
            val cursor = db.rawQuery(
                "SELECT * FROM $TABLE_NAME WHERE ($COLUMN_USERNAME = ? OR $COLUMN_EMAIL = ?) AND $COLUMN_PASSWORD = ?",
                arrayOf(usernameOrEmail, usernameOrEmail, password)
            )
            val exists = cursor.count > 0
            cursor.close()
            exists
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun isUsernameTaken(username: String): Boolean {
        return try {
            val db = this.readableDatabase
            val cursor = db.rawQuery("SELECT * FROM $TABLE_NAME WHERE $COLUMN_USERNAME = ?", arrayOf(username))
            val exists = cursor.count > 0
            cursor.close()
            exists
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun isEmailTaken(email: String): Boolean {
        return try {
            val db = this.readableDatabase
            val cursor = db.rawQuery("SELECT * FROM $TABLE_NAME WHERE $COLUMN_EMAIL = ?", arrayOf(email))
            val exists = cursor.count > 0
            cursor.close()
            exists
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun getUserDetails(usernameOrEmail: String): Map<String, String>? {
        return try {
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
            userDetails
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun ensureProjectsTableExists(db: SQLiteDatabase) {
        try {
            val createProjectsTable = ("CREATE TABLE IF NOT EXISTS " + TABLE_PROJECTS + " ("
                    + COLUMN_PROJ_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + COLUMN_PROJ_NAME + " TEXT UNIQUE)")
            db.execSQL(createProjectsTable)
            
            val cursor = db.rawQuery("SELECT COUNT(*) FROM $TABLE_PROJECTS", null)
            var count = 0
            if (cursor.moveToFirst()) {
                count = cursor.getInt(0)
            }
            cursor.close()
            
            if (count == 0) {
                val defaults = listOf("Work", "Education", "Personal")
                for (proj in defaults) {
                    val values = ContentValues()
                    values.put(COLUMN_PROJ_NAME, proj)
                    db.insert(TABLE_PROJECTS, null, values)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getAllProjects(): List<String> {
        val list = mutableListOf<String>()
        try {
            val db = this.readableDatabase
            ensureProjectsTableExists(db)
            val cursor = db.rawQuery("SELECT $COLUMN_PROJ_NAME FROM $TABLE_PROJECTS", null)
            if (cursor.moveToFirst()) {
                do {
                    list.add(cursor.getString(0))
                } while (cursor.moveToNext())
            }
            cursor.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        if (list.isEmpty()) {
            return listOf("Work", "Education", "Personal")
        }
        return list
    }

    fun addProject(name: String): Long {
        return try {
            val db = this.writableDatabase
            ensureProjectsTableExists(db)
            val values = ContentValues()
            values.put(COLUMN_PROJ_NAME, name)
            db.insert(TABLE_PROJECTS, null, values)
        } catch (e: Exception) {
            e.printStackTrace()
            -1L
        }
    }

    fun deleteProject(name: String): Boolean {
        return try {
            val db = this.writableDatabase
            ensureProjectsTableExists(db)
            val rowsDeleted = db.delete(TABLE_PROJECTS, "$COLUMN_PROJ_NAME = ?", arrayOf(name))
            rowsDeleted > 0
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
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