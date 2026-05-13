package com.example.janna.util

import android.content.Context
import android.content.SharedPreferences

class PreferenceManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("janna_prefs", Context.MODE_PRIVATE)

    fun addLifetimeSavings(amount: Double) {
        val current = prefs.getFloat("lifetime_savings", 0f)
        prefs.edit().putFloat("lifetime_savings", current + amount.toFloat()).apply()
    }

    fun getLifetimeSavings(): Double {
        return prefs.getFloat("lifetime_savings", 0f).toDouble()
    }

    fun trackSearch(query: String) {
        if (query.isBlank()) return
        val currentCount = prefs.getInt("search_count_$query", 0)
        prefs.edit().putInt("search_count_$query", currentCount + 1).apply()
    }
    
    fun getIsLoggedIn(): Boolean = prefs.getBoolean("is_logged_in", false)
    fun setLoggedIn(value: Boolean) = prefs.edit().putBoolean("is_logged_in", value).apply()
}
