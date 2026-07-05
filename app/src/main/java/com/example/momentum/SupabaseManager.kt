package com.example.momentum

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.gotrue.Auth
import kotlinx.serialization.Serializable

@Serializable
data class ProjectDto(
    val id: Int? = null,
    val project_name: String
)

@Serializable
data class ActivityRecordDto(
    val id: Int? = null,
    val user_id: String,
    val activity_name: String,
    val project: String,
    val category: String,
    val duration: Int,
    val notes: String,
    val date_millis: Long,
    val start_time: String
)

object SupabaseManager {
    val client = createSupabaseClient(
        supabaseUrl = "https://rqikncvtosqzvecdngsu.supabase.co",
        supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InJxaWtuY3Z0b3NxenZlY2RuZ3N1Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODI4MDY2MDYsImV4cCI6MjA5ODM4MjYwNn0.hJlo36M4hOKFsM0bMeIC4C8pj0L14MF8HoPMs0pMWCI"
    ) {
        install(Postgrest)
        install(Auth)
    }

    suspend fun getAllProjects(): List<String> {
        return try {
            client.postgrest["projects"]
                .select()
                .decodeList<ProjectDto>()
                .map { it.project_name }
        } catch (e: Exception) {
            e.printStackTrace()
            listOf("Work", "Education", "Personal")
        }
    }

    suspend fun addProject(name: String): Boolean {
        return try {
            client.postgrest["projects"]
                .insert(ProjectDto(project_name = name))
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun deleteProject(name: String): Boolean {
        return try {
            client.postgrest["projects"]
                .delete {
                    filter {
                        eq("project_name", name)
                    }
                }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun getActivitiesByDate(userId: String, dateMillis: Long): List<ActivityRecord> {
        return try {
            val cal = java.util.Calendar.getInstance()
            cal.timeInMillis = dateMillis
            cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
            cal.set(java.util.Calendar.MINUTE, 0)
            cal.set(java.util.Calendar.SECOND, 0)
            cal.set(java.util.Calendar.MILLISECOND, 0)
            val start = cal.timeInMillis
            val end = start + (24 * 60 * 60 * 1000) - 1

            client.postgrest["activities"]
                .select {
                    filter {
                        eq("user_id", userId)
                        gte("date_millis", start)
                        lte("date_millis", end)
                    }
                }
                .decodeList<ActivityRecordDto>()
                .map {
                    ActivityRecord(
                        id = it.id ?: 0,
                        name = it.activity_name,
                        project = it.project,
                        category = it.category,
                        duration = it.duration,
                        notes = it.notes,
                        dateMillis = it.date_millis,
                        startTime = it.start_time
                    )
                }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun getAllActivities(userId: String): List<ActivityRecord> {
        return try {
            client.postgrest["activities"]
                .select {
                    filter {
                        eq("user_id", userId)
                    }
                }
                .decodeList<ActivityRecordDto>()
                .map {
                    ActivityRecord(
                        id = it.id ?: 0,
                        name = it.activity_name,
                        project = it.project,
                        category = it.category,
                        duration = it.duration,
                        notes = it.notes,
                        dateMillis = it.date_millis,
                        startTime = it.start_time
                    )
                }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun addActivity(activity: ActivityRecordDto): Boolean {
        return try {
            client.postgrest["activities"]
                .insert(activity)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // ==========================================
    // TAMBAHAN BARU UNTUK FITUR EDIT & HAPUS
    // ==========================================

    suspend fun updateActivity(activityId: Int, updatedRecord: ActivityRecordDto): Boolean {
        return try {
            client.postgrest["activities"].update(updatedRecord) {
                filter {
                    eq("id", activityId)
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun deleteActivity(activityId: Int): Boolean {
        return try {
            client.postgrest["activities"].delete {
                filter {
                    eq("id", activityId)
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}