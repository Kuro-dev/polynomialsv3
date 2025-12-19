package org.kurodev

import kotlin.math.pow


class MathUtil {
    companion object {
        /**
         * source: https://rosettacode.org/wiki/Nth_root#Nim
         * @param x The number to compute the root of. Must be positive
         * @param n The base of the root. Must be more than 1
         */
        fun nthRoot(x: Double, n: Int): Double {
            if (n < 2) throw IllegalArgumentException("n must be more than 1")
            if (x <= 0.0) throw IllegalArgumentException("x must be positive")
            val np = n - 1
            fun iter(g: Double) = (np * g + x / g.pow(np.toDouble())) / n
            var g1 = x
            var g2 = iter(g1)
            while (g1 != g2) {
                g1 = iter(g1)
                g2 = iter(iter(g2))
            }
            return g1
        }
    }
}
