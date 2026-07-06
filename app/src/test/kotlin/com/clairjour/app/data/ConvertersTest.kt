package com.clairjour.app.data

import com.clairjour.app.data.db.Converters
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ConvertersTest {

    private val c = Converters()

    @Test
    fun `instant round trip`() {
        val i = Instant.fromEpochMilliseconds(1_700_000_000_000L)
        assertEquals(i, c.longToInstant(c.instantToLong(i)))
    }

    @Test
    fun `local date round trip`() {
        val d = LocalDate(2026, 7, 5)
        assertEquals(d, c.stringToLocalDate(c.localDateToString(d)))
    }

    @Test
    fun `string list round trip preserves items with special chars`() {
        val list = listOf("stress", "boredom", "with|pipe", "with,comma")
        val encoded = c.stringListToString(list)
        assertEquals(list, c.stringToStringList(encoded))
    }

    @Test
    fun `string list empty round trip`() {
        assertEquals(emptyList<String>(), c.stringToStringList(c.stringListToString(emptyList())))
    }

    @Test
    fun `string list null decodes to empty`() {
        assertEquals(emptyList<String>(), c.stringToStringList(null))
    }

    @Test
    fun `nullable instant returns null`() {
        assertNull(c.instantToLong(null))
        assertNull(c.longToInstant(null))
    }
}
