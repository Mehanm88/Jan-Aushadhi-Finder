package com.example.janna.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import androidx.core.app.NotificationCompat
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.example.janna.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val medicineName = intent.getStringExtra("MEDICINE_NAME") ?: "Medicine"
        val imageUrl = intent.getStringExtra("IMAGE_URL")
        
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val bitmap = imageUrl?.let { fetchBitmap(context, it) }
                showNotification(context, medicineName, bitmap)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private suspend fun fetchBitmap(context: Context, url: String): Bitmap? {
        val loader = ImageLoader(context)
        val request = ImageRequest.Builder(context)
            .data(url)
            .allowHardware(false) // Bitmaps for notifications should not be hardware-accelerated
            .build()
        
        val result = loader.execute(request)
        return if (result is SuccessResult) {
            (result.drawable as? BitmapDrawable)?.bitmap
        } else {
            null
        }
    }

    private fun showNotification(context: Context, medicineName: String, bitmap: Bitmap?) {
        val channelId = "medicine_reminders"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Medicine Reminders",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, channelId)
            .setContentTitle("Medicine Mission: Time to Heal! ✨")
            .setContentText("Reminder: Take $medicineName now.")
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        if (bitmap != null) {
            // Set Large Icon for a professional look
            builder.setLargeIcon(bitmap)
            // BigPictureStyle for the full anime aesthetic in the notification drawer
            builder.setStyle(
                NotificationCompat.BigPictureStyle()
                    .bigPicture(bitmap)
                    .bigLargeIcon(null as Bitmap?) // Hide large icon when expanded
            )
        }

        notificationManager.notify(medicineName.hashCode(), builder.build())
    }
}
