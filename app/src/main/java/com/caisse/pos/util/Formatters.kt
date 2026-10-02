package com.caisse.pos.util

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Currency
import java.util.Date
import java.util.Locale

object Money {
    private val localeTn = Locale("fr", "TN")
    private val tnd: NumberFormat = NumberFormat.getCurrencyInstance(localeTn).apply {
        currency = Currency.getInstance("TND")
        maximumFractionDigits = 3
        minimumFractionDigits = 3
    }

    // millimes → affichage DT (1 DT = 1000 millimes)
    fun formatCents(millimes: Int): String = tnd.format(millimes / 1000.0)
}

object Dates {
    private val fmt = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("fr", "TN"))

    fun format(epochMs: Long): String = fmt.format(Date(epochMs))
}
