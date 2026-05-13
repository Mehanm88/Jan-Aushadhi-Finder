package com.example.janna.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reminder_table")
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val medicineName: String,
    val reminderTime: Long, // Timestamp in ms
    val isActive: Boolean = true,
    val profileId: Int = 1, // Default to main profile
    val dosageTime: String = "Morning", // Morning, Noon, Night
    val currentStock: Int = 30, // Total tablets remaining
    val dosagePerDay: Int = 1, // Tablets per day for prediction
    val lastRefillDate: Long = System.currentTimeMillis()
)
