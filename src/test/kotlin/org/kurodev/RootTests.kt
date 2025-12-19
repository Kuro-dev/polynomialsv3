package org.kurodev

import kotlin.test.Test
import kotlin.test.assertEquals

class RootTests {

    @Test
    fun nthRoot() {
        assertEquals( 2.0, MathUtil.nthRoot(32.0, 5),0.0)
        assertEquals( 2.0, MathUtil.nthRoot(65536.0, 16),0.0)
        assertEquals( 3.0, MathUtil.nthRoot(2187.0, 7),0.0)
        assertEquals( 2.024397458499885, MathUtil.nthRoot(34.0, 5),0.00000000001)
        assertEquals( 1.453198460282268, MathUtil.nthRoot(42.0, 10),0.00000000001)
        assertEquals( 2.23606797749979, MathUtil.nthRoot(5.0, 2),0.00000000001)
    }
}