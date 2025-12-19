package org.kurodev

import org.kurodev.value.Value
import kotlin.test.Test
import kotlin.test.assertEquals

class DifferentiateTests {

    @Test
    fun testSimpleDifferentiation() {
        assertEquals("5", Value(5).differentiate().toString())
        assertEquals("1", Value().differentiate().toString())
        assertEquals("5.5", Value(5.5).differentiate().toString())
    }

    @Test
    fun testMoreComplexDifferentiation() {
        
    }
}