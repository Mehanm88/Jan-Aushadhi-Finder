package com.example.janna.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SavingsCalculatorTest {

    @Test
    fun calculateSavings_correctPercentage() {
        val result = SavingsCalculator.calculateSavings(100.0, 20.0)
        assertEquals(80.0, result.absoluteSavings, 0.01)
        assertEquals(80, result.percentageSavings)
        assertTrue(result.isHighSavings)
    }

    @Test
    fun calculateSavings_zeroBrandPrice_returnsZero() {
        val result = SavingsCalculator.calculateSavings(0.0, 20.0)
        assertEquals(0.0, result.absoluteSavings, 0.01)
        assertEquals(0, result.percentageSavings)
        assertFalse(result.isHighSavings)
    }

    @Test
    fun calculateSavings_negativePrices_handlesGracefully() {
        val result = SavingsCalculator.calculateSavings(-10.0, 5.0)
        assertEquals(0.0, result.absoluteSavings, 0.01)
        assertEquals(0, result.percentageSavings)
    }

    @Test
    fun calculateSavings_genericMoreExpensive_noSavings() {
        val result = SavingsCalculator.calculateSavings(50.0, 60.0)
        assertEquals(0.0, result.absoluteSavings, 0.01)
        assertEquals(0, result.percentageSavings)
        assertFalse(result.isHighSavings)
    }
}
