package com.example.nexusbusiness.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "orders")
data class Order(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val customerName: String = "",
    val productsSummary: String = "",
    val processedItems: String = "", // Armazena os itens que já foram para o estoque (separados por \n)
    val totalValue: String = "",
    val status: String = "PENDENTE", // PENDENTE, VENDA REALIZADA, CANCELADO
    val timestamp: Long = System.currentTimeMillis()
)
