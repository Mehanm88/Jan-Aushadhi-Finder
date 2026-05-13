package com.example.janna.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.janna.MainActivity
import com.example.janna.R

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val medicineName = intent.getStringExtra("MEDICINE_NAME") ?: "Medicine"
        val isStockAlert = intent.getBooleanExtra("IS_STOCK_ALERT", false)
        showNotification(context, medicineName, isStockAlert)
    }

    private fun showNotification(context: Context, medicineName: String, isStockAlert: Boolean) {
        val channelId = "refill_reminders"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Refill Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for medical refills"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val mainIntent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context, 0, mainIntent, PendingIntent.FLAG_IMMUTABLE
        )

        val title = if (isStockAlert) "Low Stock Alert: $medicineName" else "Refill Due: $medicineName"
        val content = if (isStockAlert) 
            "Your stock for $medicineName is running low. Please reorder soon." 
            else "It's time to refill your prescription for $medicineName."

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .addAction(0, "Dismiss", pendingIntent) // Simplified dismiss
            .build()

        notificationManager.notify(medicineName.hashCode(), notification)
    }
}
