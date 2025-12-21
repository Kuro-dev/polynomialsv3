package org.kurodev

import org.kurodev.value.toValue
import kotlin.math.PI
import kotlin.test.Test
import kotlin.test.assertEquals

class SimplifyTests {
    @Test
    fun testSimplify() {
        val eq = PI.toValue().divide(2).sin()
        assertEquals("sin(π / 2)", eq.toString())
        assertEquals(1.0, eq.compute())
        assertEquals("1", eq.simplify().toString())
    }
}