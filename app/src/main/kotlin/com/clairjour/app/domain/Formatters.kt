package com.clairjour.app.domain

import android.content.Context
import kotlinx.datetime.LocalDate
import java.text.NumberFormat
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

object Formatters {
    fun currency(amount: Double, context: Context): String {
        val locale = context.resources.configuration.locales.get(0) ?: Locale.getDefault()
        return NumberFormat.getCurrencyInstance(locale).format(amount)
    }

    fun date(date: LocalDate, context: Context): String {
        val locale = context.resources.configuration.locales.get(0) ?: Locale.getDefault()
        val javaDate = java.time.LocalDate.of(date.year, date.monthNumber, date.dayOfMonth)
        return javaDate.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale))
    }
}
