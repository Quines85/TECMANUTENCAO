package com.tecmanutencao.app.util

import java.text.NumberFormat
import java.util.Locale

object NumberUtils {

    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))

    fun formatCurrency(value: Double): String {
        return currencyFormat.format(value)
    }

    fun formatOrcamentoNumber(number: Int): String {
        return String.format("ORC-%04d", number)
    }
}
