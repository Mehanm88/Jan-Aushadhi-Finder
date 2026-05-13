package com.example.janna.util

import com.example.janna.data.Medicine

object SavingsCalculator {
    data class SavingsResult(
        val absoluteSavings: Double,
        val percentageSavings: Int,
        val isHighSavings: Boolean
    )

    fun calculateSavings(brandPrice: Double, genericPrice: Double): SavingsResult {
        // Prevent crashes by handling zero or negative prices
        if (brandPrice <= 0.0) return SavingsResult(0.0, 0, false)
        
        val diff = brandPrice - genericPrice
        val percentage = ((diff / brandPrice) * 100).toInt().coerceIn(0, 100)
        
        return SavingsResult(
            absoluteSavings = if (diff > 0) diff else 0.0,
            percentageSavings = percentage,
            isHighSavings = percentage >= 50
        )
    }
}
