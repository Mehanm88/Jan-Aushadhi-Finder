package com.example.janna.util

import android.content.Context
import android.net.Uri
import com.example.janna.data.ReminderEntity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.OutputStreamWriter
import java.util.Scanner

class BackupManager(private val context: Context) {
    private val gson = Gson()

    fun exportReminders(reminders: List<ReminderEntity>, uri: Uri): Boolean {
        return try {
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                val json = gson.toJson(reminders)
                OutputStreamWriter(outputStream).use { writer ->
                    writer.write(json)
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun importReminders(uri: Uri): List<ReminderEntity>? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val scanner = Scanner(inputStream).useDelimiter("\\A")
                val json = if (scanner.hasNext()) scanner.next() else ""
                val type = object : TypeToken<List<ReminderEntity>>() {}.type
                gson.fromJson<List<ReminderEntity>>(json, type)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
