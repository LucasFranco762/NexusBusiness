package com.example.nexusbusiness.util

import java.text.NumberFormat
import java.util.Locale

object CurrencyUtils {
    private val ptBr = Locale("pt", "BR")

    fun format(value: Double): String {
        return NumberFormat.getCurrencyInstance(ptBr).format(value)
    }

    fun parse(value: String): Double {
        if (value.isBlank()) return 0.0
        val cleanString = value.replace("[R$\\s.]".toRegex(), "").replace(",", ".")
        return cleanString.toDoubleOrNull() ?: 0.0
    }
}
