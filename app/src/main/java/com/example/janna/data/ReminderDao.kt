package com.example.janna.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderDao {
    @Query("SELECT * FROM reminder_table ORDER BY reminderTime ASC")
    fun getAllReminders(): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminder_table WHERE profileId = :profileId ORDER BY reminderTime ASC")
    fun getRemindersByProfile(profileId: Int): Flow<List<ReminderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: ReminderEntity): Long

    @Delete
    suspend fun deleteReminder(reminder: ReminderEntity)

    @Query("UPDATE reminder_table SET isActive = :active WHERE id = :id")
    suspend fun setReminderActive(id: Int, active: Boolean)

    // Profile DAOs
    @Query("SELECT * FROM profile_table")
    fun getAllProfiles(): Flow<List<ProfileEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: ProfileEntity)
}
