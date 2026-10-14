package com.example.nexusbusiness.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nome: String = "",
    val qtd: Int = 0,
    val custo: String = "R$ 0,00",
    val margem: Double = 0.0,
    val precoVenda: String = "R$ 0,00",
    val rateio: String = "R$ 0,00",
    val photoPath: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)
