package org.kurodev

import org.kurodev.value.toValue
import kotlin.test.Test
import kotlin.test.assertEquals

class DifferentiateTests {

    @Test
    fun testSimpleDifferentiation() {
        assertEquals("5", 'x'.toValue().multiply(5).differentiate('x').toString())
        assertEquals("1", 'x'.toValue().differentiate('x').toString())
        assertEquals("5.5", 'x'.toValue().multiply(5.5).differentiate('x').toString())
    }

    @Test
    fun testMoreComplexDifferentiation() {

    }
}