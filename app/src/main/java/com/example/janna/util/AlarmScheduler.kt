package com.example.janna.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.janna.data.ReminderEntity
import timber.log.Timber

class AlarmScheduler(private val context: Context) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun schedule(reminder: ReminderEntity) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("MEDICINE_NAME", reminder.medicineName)
            putExtra("REMINDER_ID", reminder.id)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminder.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Schedule daily alarm
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            reminder.reminderTime,
            pendingIntent
        )
        
        Timber.d("Scheduled alarm for ${reminder.medicineName} at ${reminder.reminderTime}")
    }

    fun cancel(reminder: ReminderEntity) {
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminder.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
        Timber.d("Cancelled alarm for ${reminder.medicineName}")
    }

    fun showImmediateNotification(reminder: ReminderEntity, isStockAlert: Boolean = false) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("MEDICINE_NAME", reminder.medicineName)
            putExtra("REMINDER_ID", reminder.id)
            putExtra("IS_STOCK_ALERT", isStockAlert)
        }
        context.sendBroadcast(intent)
        Timber.d("Sent immediate notification broadcast for ${reminder.medicineName}")
    }
}
