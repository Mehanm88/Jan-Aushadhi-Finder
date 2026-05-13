package com.example.janna.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "medicine_table")
data class Medicine(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val brandName: String,
    val genericName: String,
    val brandPrice: Double,
    val genericPrice: Double,
    val category: String,
    val dosage: String = "",
    val sideEffects: String = "No significant side effects reported.",
    val manufacturer: String = "Generic Labs"
)

data class Store(
    val id: String = "",
    val name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val isOpen: Boolean,
    val distance: String,
    val inventory: List<String> = emptyList(),
    val rating: Float = 4.5f,
    val reviewCount: Int = 120,
    val phone: String = "911234567890", // For WhatsApp Chat
    val isStockAvailable: Boolean = true // Crowdsourced field
)

data class CartItem(
    val medicine: Medicine,
    val quantity: Int = 1
)
