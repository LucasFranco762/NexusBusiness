package com.example.nexusbusiness.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "routes")
data class Route(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val cidade: String = "",
    val custo: String = "R$ 0,00",
    val ativa: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)
