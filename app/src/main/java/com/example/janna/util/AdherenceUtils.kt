package com.example.janna.util

import com.example.janna.data.ReminderEntity
import java.util.concurrent.TimeUnit

object AdherenceUtils {
    
    /**
     * Calculates how many days of stock remain based on current stock and daily dosage.
     */
    fun calculateDaysRemaining(reminder: ReminderEntity): Int {
        if (reminder.dosagePerDay <= 0) return 0
        return reminder.currentStock / reminder.dosagePerDay
    }

    /**
     * Predicts the refill date based on current stock.
     */
    fun predictRefillDate(reminder: ReminderEntity): Long {
        val daysRemaining = calculateDaysRemaining(reminder)
        return System.currentTimeMillis() + TimeUnit.DAYS.toMillis(daysRemaining.toLong())
    }

    /**
     * Returns a status color based on stock levels.
     */
    fun getStockStatusColor(daysRemaining: Int): androidx.compose.ui.graphics.Color {
        return when {
            daysRemaining <= 3 -> androidx.compose.ui.graphics.Color(0xFFD32F2F) // Critical
            daysRemaining <= 7 -> androidx.compose.ui.graphics.Color(0xFFFFB300) // Warning
            else -> androidx.compose.ui.graphics.Color(0xFF388E3C) // Safe
        }
    }
}
