package org.kurodev

import org.kurodev.value.Value
import org.kurodev.value.toValue
import kotlin.test.Test
import kotlin.test.assertEquals

class StringTests {
    @Test
    fun testStringRepresentation() {
        assertEquals("sin(5)", 5.toValue().sin().toString())
        assertEquals("sin(5 + 7)", 5.toValue().plus(7).sin().toString())
        assertEquals("ln(sin(5 + 7) * 2)", 5.toValue().plus(7).sin().multiply(2).ln().toString())
    }

    @Test
    fun testStringRepresentation2() {
        assertEquals("x", Value().toString())
        assertEquals("2x", Value(2).toString())
        assertEquals("2.75x", Value(2.75).toString())
    }
}

