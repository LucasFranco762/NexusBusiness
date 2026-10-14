package com.example.nexusbusiness.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sales")
data class Sale(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val customerName: String = "",
    val productsSummary: String = "",
    val paymentMethod: String = "",
    val totalValue: String = "",
    val totalProfit: Double = 0.0, // Lucro total desta venda para o fluxo de caixa
    val timestamp: Long = System.currentTimeMillis()
)
