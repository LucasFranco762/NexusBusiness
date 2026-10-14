package com.example.nexusbusiness.data.model

data class SaleCartItem(
    val id: Long = System.nanoTime(),
    val product: Product,
    val quantity: Int
)
