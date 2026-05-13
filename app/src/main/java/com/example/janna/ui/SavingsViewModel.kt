package com.example.janna.ui

import androidx.lifecycle.ViewModel
import com.example.janna.data.CartItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class SavingsSummary(
    val totalBrandedCost: Double,
    val totalGenericCost: Double,
    val totalSavings: Double,
    val savingsRatio: Float
)

class SavingsViewModel : ViewModel() {
    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList<CartItem>())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    private val _savingsSummary = MutableStateFlow(SavingsSummary(0.0, 0.0, 0.0, 0f))
    val savingsSummary: StateFlow<SavingsSummary> = _savingsSummary.asStateFlow()

    fun addItemToPrescription(item: CartItem) {
        val currentList = _cartItems.value.toMutableList()
        val existingIndex = currentList.indexOfFirst { it.medicine.id == item.medicine.id }
        
        if (existingIndex != -1) {
            currentList[existingIndex] = currentList[existingIndex].copy(quantity = currentList[existingIndex].quantity + 1)
        } else {
            currentList.add(item)
        }
        
        _cartItems.value = currentList
        calculateTotalPrescriptionSavings()
    }

    fun clearPrescription() {
        _cartItems.value = emptyList<CartItem>()
        calculateTotalPrescriptionSavings()
    }

    private fun calculateTotalPrescriptionSavings() {
        val items = _cartItems.value
        
        var brandedTotal = 0.0
        var genericTotal = 0.0
        
        for (item in items) {
            brandedTotal += item.medicine.brandPrice * item.quantity
            genericTotal += item.medicine.genericPrice * item.quantity
        }
        
        val savings = brandedTotal - genericTotal
        val ratio = if (brandedTotal > 0.0) (genericTotal / brandedTotal).toFloat() else 0f
        
        _savingsSummary.value = SavingsSummary(
            totalBrandedCost = brandedTotal,
            totalGenericCost = genericTotal,
            totalSavings = savings,
            savingsRatio = ratio
        )
    }
}
