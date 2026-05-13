package com.example.janna.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "profile_table")
data class ProfileEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val relation: String, // e.g., "Self", "Parent", "Child"
    val avatarEmoji: String = "👤"
)
