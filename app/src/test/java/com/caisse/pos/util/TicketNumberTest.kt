package com.caisse.pos.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TicketNumberTest {

    @Test
    fun formatTicket_padsSequenceToSixDigits() {
        assertEquals("A3F2-000001", DeviceIdProvider.formatTicket("A3F2", 1))
        assertEquals("A3F2-000042", DeviceIdProvider.formatTicket("A3F2", 42))
        assertEquals("B7C1-100000", DeviceIdProvider.formatTicket("B7C1", 100000))
    }

    @Test
    fun differentDevicePrefixes_produceDistinctTicketSpaces() {
        val a = DeviceIdProvider.formatTicket("A3F2", 1)
        val b = DeviceIdProvider.formatTicket("B7C1", 1)
        assertTrue(a != b)
        assertEquals("A3F2-000001", a)
        assertEquals("B7C1-000001", b)
    }
}
