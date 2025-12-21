@file:Suppress("unused")

package org.kurodev.value

import org.kurodev.MathUtil
import java.util.*
import kotlin.math.*

private val ZERO: Value by lazy { ConstantValue(0) }
private val ONE: Value by lazy { ConstantValue(1) }
private val TWO: Value by lazy { ConstantValue(2) }
private val THREE: Value by lazy { ConstantValue(3) }

private val NEG_ONE: Value = ConstantValue(-1)
private val E: Value by lazy {
    object : ConstantValue(Math.E) {
        override fun toString() = "E"
    }
}

private val PI: Value by lazy {
    object : ConstantValue(Math.PI) {
        override fun toString() = "π"
    }
}
private val LN_TWO: Value by lazy { LnValue(2.toValue()).simplify() }

fun Double.toValue(): Value {
    return when {
        this == Math.E -> E
        this == Math.PI -> PI
        else -> ConstantValue(this)
    }
}

fun Int.toValue(): Value {
    return when (this) {
        0 -> ZERO
        1 -> ONE
        2 -> TWO
        3 -> THREE
        -1 -> NEG_ONE
        else -> ConstantValue(this)
    }
}

fun Char.toValue(): Value {
    return VariableValue(this)
}

class VariableValue(val variable: Char) : Value() {
    override fun compute(vars: Map<Char, Value>): Double {
        val value = vars[variable]
        if (value != null) {
            return value.compute(vars);
        }
        throw IllegalArgumentException("'$variable' was not defined")
    }


    override fun isConstant(): Boolean = false

    override fun differentiate(d: Char): Value = if (d == variable) ONE else ZERO;

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as VariableValue

        return variable == other.variable
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + variable.hashCode()
        return result
    }

    override fun toString(): String {
        return "$variable"
    }

}

open class ConstantValue(val num: Double) : Value() {
    constructor(num: Int) : this(num.toDouble())

    override fun compute(vars: Map<Char, Value>) = num
    override fun toString(): String = if (num % 1.0 == 0.0) num.toInt().toString() else num.toString()
    override fun isConstant(): Boolean = true
    override fun differentiate(d: Char): Value = ZERO;
}


abstract class Value() {

    override fun toString(): String = "undefined";
    abstract fun compute(vars: Map<Char, Value>): Double;
    fun compute(): Double = compute(Collections.emptyMap())
    fun compute(x: Int): Double = compute(x.toValue())
    fun compute(x: Double): Double = compute(x.toValue())
    fun compute(x: Value): Double {
        val vars = HashMap<Char, Value>()
        vars['x'] = x;
        return compute(vars)
    }

    fun toRadians(): Value = RadianValue(this)
    fun toDegrees(): Value = DegreeValue(this)

    abstract fun isConstant(): Boolean;

    abstract fun differentiate(d: Char): Value;

    /**
     * Simplifies all constant values by precomputing them
     */
    open fun simplify(): Value {
        if (isConstant()) {
            return compute(0).toValue()
        }
        return this
    }

    /**
     * Multiplies the current value with -1
     */
    operator fun unaryMinus(): Value = MultiplyValue(NEG_ONE, this).simplify()

    fun isZero(): Boolean = this is ConstantValue && this.num == 0.0
    fun isOne(): Boolean = this is ConstantValue && this.num == 1.0
    fun isNegativeOne(): Boolean = this is ConstantValue && this.num == -1.0

    fun negate(): Value = when {
        this == ZERO -> ZERO
        this is MultiplyValue && this.a == NEG_ONE -> this.b
        else -> MultiplyValue(NEG_ONE, this).simplify()
    }

    fun plus(other: Value): Value = PlusValue(this, other)
    fun plus(other: Double) = plus(other.toValue())
    fun plus(other: Int) = plus(other.toValue())

    fun minus(other: Value): Value = MinusValue(this, other)
    fun minus(other: Double) = minus(other.toValue())
    fun minus(other: Int) = minus(other.toValue())

    fun multiply(other: Value): Value = MultiplyValue(this, other)
    fun multiply(other: Double) = multiply(other.toValue())
    fun multiply(other: Int) = multiply(other.toValue())

    fun divide(other: Value): Value = DivideValue(this, other)
    fun divide(other: Double) = divide(other.toValue())
    fun divide(other: Int) = divide(other.toValue())

    fun pow(other: Value): Value = PowerValue(this, other)
    fun pow(other: Double) = pow(other.toValue())
    fun pow(other: Int) = pow(other.toValue())

    fun log(base: Value): Value = LogValue(this, base)
    fun log(base: Double) = log(base.toValue())
    fun log(base: Int) = log(base.toValue())

    fun ln(): Value = LnValue(this)
    fun ld(): Value = LdValue(this)

    fun exp(): Value = ExpValue(this)
    fun sqrt(): Value = SqrtValue(this)
    fun cbrt(): Value = CbrtValue(this)

    fun sin(): Value = SinValue(this)
    fun asin(): Value = AsinValue(this)
    fun cos(): Value = CosValue(this)
    fun acos(): Value = AcosValue(this)
    fun tan(): Value = TanValue(this)
    fun atan(): Value = AtanValue(this)

    fun nthRoot(base: Int): Value = NthRootValue(this, base)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Value) return false
        if (this.isConstant() && other.isConstant()) {
            return this.compute(0) == other.compute(0)
        }
        return true
    }

    override fun hashCode(): Int {
        return javaClass.hashCode()
    }
}

class RadianValue(val value: Value) : Value() {
    override fun compute(vars: Map<Char, Value>): Double {
        return Math.toRadians(value.compute(vars))
    }

    override fun isConstant(): Boolean = value.isConstant()
    override fun differentiate(d: Char): Value = value.differentiate(d).toRadians()
    override fun toString(): String {
        return "$value"
    }
}

class DegreeValue(val value: Value) : Value() {
    override fun compute(vars: Map<Char, Value>): Double {
        return Math.toDegrees(value.compute(vars))
    }

    override fun toString(): String {
        return "$value"
    }

    override fun isConstant(): Boolean = value.isConstant()
    override fun differentiate(d: Char): Value = value.differentiate(d).toRadians()
}

class PlusValue(val a: Value, val b: Value) : Value() {
    override fun compute(vars: Map<Char, Value>) = a.compute(vars) + b.compute(vars)
    override fun toString() = "$a + $b"
    override fun isConstant(): Boolean = a.isConstant() && b.isConstant()

    override fun simplify(): Value {
        val tempA = a.simplify()
        val tempB = b.simplify()

        when {
            tempA.isConstant() && tempB.isConstant() -> return compute(0).toValue()
            tempA == ZERO -> return tempB
            tempB == ZERO -> return tempA
            tempB is MultiplyValue && tempB.a == NEG_ONE && tempB.b == tempA -> return ZERO
            tempA is MultiplyValue && tempA.a == NEG_ONE && tempA.b == tempB -> return ZERO
            tempA is MultiplyValue && tempB is MultiplyValue -> {
                when {
                    tempA.b == tempB.b -> return MultiplyValue(
                        PlusValue(tempA.a, tempB.a).simplify(),
                        tempA.b
                    ).simplify()

                    tempA.a == tempB.a -> return MultiplyValue(
                        tempA.a,
                        PlusValue(tempA.b, tempB.b).simplify()
                    ).simplify()
                }
            }

            tempA !is ConstantValue && tempB is ConstantValue -> {
                return PlusValue(tempB, tempA)
            }

            else -> return PlusValue(tempA, tempB)
        }
        return this;
    }

    override fun differentiate(d: Char): Value {
        return PlusValue(a.differentiate(d), b.differentiate(d))
    }
}

class MinusValue(val a: Value, val b: Value) : Value() {
    override fun compute(vars: Map<Char, Value>) = a.compute(vars) - b.compute(vars)
    override fun toString() = "$a - $b"
    override fun isConstant(): Boolean = a.isConstant() && b.isConstant()
    override fun simplify(): Value {
        val tempA = a.simplify()
        val tempB = b.simplify()
        return when {
            tempA.isConstant() && tempB.isConstant() -> compute(0).toValue()
            tempA == tempB -> ZERO // x - x = 0
            tempB == ZERO -> tempA // x - 0 = x
            tempA == ZERO -> MultiplyValue(NEG_ONE, tempB).simplify() // 0 - x = -x
            tempB is ConstantValue && tempB.num < 0 -> PlusValue(tempA, (-tempB.num).toValue()).simplify()
            tempB is MultiplyValue && tempB.a == NEG_ONE -> PlusValue(tempA, tempB.b).simplify() // a - (-b) = a + b
            else -> MinusValue(tempA, tempB)
        }
    }

    override fun differentiate(d: Char): Value {
        return MinusValue(a.differentiate(d), b.differentiate(d))
    }
}

class MultiplyValue(val a: Value, val b: Value) : Value() {
    override fun compute(vars: Map<Char, Value>) = a.compute(vars) * b.compute(vars)
    override fun toString() = when (a) {
        is VariableValue if b is VariableValue -> "$a$b"
        is VariableValue -> "$b$a"
        !is VariableValue if b is VariableValue -> "$a$b"
        else -> "$a * $b"
    }

    override fun isConstant(): Boolean = a.isConstant() && b.isConstant()
    override fun simplify(): Value {
        val tempA = a.simplify()
        val tempB = b.simplify()
        when {
            tempA.isConstant() && tempB.isConstant() -> return compute(0).toValue()
            tempA == ZERO || tempB == ZERO -> return ZERO // 0 * x = 0
            tempA == ONE -> return tempB // 1 * x = x
            tempB == ONE -> return tempA
            tempA == NEG_ONE -> return MultiplyValue(NEG_ONE, tempB).simplify() // (-1) * x = -x
            tempB == NEG_ONE -> return MultiplyValue(NEG_ONE, tempA).simplify()
            tempA == tempB -> return PowerValue(tempA, TWO).simplify() // x * x = x²
            tempA is PowerValue && tempB is PowerValue && tempA.base == tempB.base ->    // x^a * x^b = x^(a+b)
                return PowerValue(tempA.base, PlusValue(tempA.exponent, tempB.exponent).simplify()).simplify()
            // Distribute multiplication over addition if one factor is constant
            tempA is PlusValue && tempB is ConstantValue ->
                return PlusValue(
                    MultiplyValue(tempA.a, tempB).simplify(),
                    MultiplyValue(tempA.b, tempB).simplify()
                ).simplify()
            // Commutative reordering: move constants to front
            tempA !is ConstantValue && tempB is ConstantValue -> return MultiplyValue(tempB, tempA)
            else -> return MultiplyValue(tempA, tempB)
        }
    }

    override fun differentiate(d: Char): Value {
        return PlusValue(MultiplyValue(a.differentiate(d), b), MultiplyValue(a, b.differentiate(d))).simplify()
    }
}

class DivideValue(val a: Value, val b: Value) : Value() {
    override fun compute(vars: Map<Char, Value>): Double {
        val div = b.compute(vars)
        if (div == 0.0) throw IllegalStateException("Division by zero")
        return a.compute(vars) / div
    }

    override fun simplify(): Value {
        val tempA = a.simplify()
        val tempB = b.simplify()

        when {
            tempA.isConstant() && tempB.isConstant() -> return compute(0).toValue()
            tempA == ZERO && tempB != ZERO -> return ZERO
            tempB == ONE -> return tempA
            tempA == tempB && tempA != ZERO -> return ONE
            tempB == NEG_ONE -> return MultiplyValue(NEG_ONE, tempA).simplify()
            tempA is MultiplyValue -> {
                if (tempA.a == tempB) return tempA.b
                if (tempA.b == tempB) return tempA.a
            }
        }

        when {
            tempB is PowerValue && tempB.base == tempA -> {
                // x / (x^a) = x^(1-a)
                return PowerValue(tempA, MinusValue(ONE, tempB.exponent).simplify()).simplify()
            }

            tempA is PowerValue && tempA.base == tempB -> {
                return PowerValue(tempB, MinusValue(tempA.exponent, ONE).simplify()).simplify()
            }
            // cross out factors on both sides of the division
            tempA is MultiplyValue && tempB is MultiplyValue -> {
                if (tempA.a == tempB.a) return DivideValue(tempA.b, tempB.b).simplify()
                if (tempA.a == tempB.b) return DivideValue(tempA.b, tempB.a).simplify()
                if (tempA.b == tempB.a) return DivideValue(tempA.a, tempB.b).simplify()
                if (tempA.b == tempB.b) return DivideValue(tempA.a, tempB.a).simplify()
            }
        }

        // Move constant denominators to numerator
        return when (tempB) {
            is ConstantValue if tempB.num != 1.0 && tempB.num != -1.0 ->
                MultiplyValue(DivideValue(ONE, tempB), tempA).simplify()

            else -> DivideValue(tempA, tempB)
        }
    }

    override fun isConstant(): Boolean = a.isConstant() && b.isConstant()
    override fun differentiate(d: Char): Value {
        return DivideValue(
            MinusValue(MultiplyValue(a.differentiate(d), b), MultiplyValue(a, b.differentiate(d))),
            b.pow(2)
        )
            .simplify()
    }

    override fun toString() = "$a / $b"
}

class PowerValue(val base: Value, val exponent: Value) : Value() {
    override fun isConstant(): Boolean = base.isConstant() && exponent.isConstant()
    override fun differentiate(d: Char): Value {
        return when {
            exponent.isConstant() -> // Power rule: d/dx f(x)^n = n * f(x)^(n-1) * f'(x)
                MultiplyValue(
                    MultiplyValue(exponent, PowerValue(base, exponent.minus(1))),
                    base.differentiate(d)
                ).simplify()

            base.isConstant() -> // Exponential: d/dx a^f(x) = a^f(x) * ln(a) * f'(x)
                MultiplyValue(
                    MultiplyValue(this, LnValue(base)),
                    exponent.differentiate(d)
                ).simplify()

            else -> // Both vary: use logarithmic differentiation
                MultiplyValue(
                    this,
                    PlusValue(
                        MultiplyValue(exponent.differentiate(d), LnValue(base)),
                        MultiplyValue(
                            exponent,
                            DivideValue(base.differentiate(d), base)
                        )
                    )
                ).simplify()
        }
    }


    override fun compute(vars: Map<Char, Value>) = base.compute(vars).pow(exponent.compute(vars))
    override fun toString() = "$base^$exponent"
    override fun simplify(): Value {
        val tempA = base.simplify()
        val tempB = exponent.simplify()

        return when {
            tempA.isConstant() && tempB.isConstant() -> compute(0).toValue()
            tempB == ZERO && tempA != ZERO -> ONE // x^0 = 1 (x ≠ 0)
            tempB == ONE -> tempA // x^1 = x
            tempA == ONE -> ONE // 1^x = 1
            tempA == ZERO && tempB is ConstantValue && tempB.num > 0 -> ZERO // 0^x = 0 (x > 0)
            tempA is PowerValue ->  // (x^a)^b = x^(a*b)
                PowerValue(tempA.base, MultiplyValue(tempA.exponent, tempB).simplify()).simplify()

            tempA == E && tempB is LnValue -> tempB.value.simplify() // e^ln(x) = x
            else -> PowerValue(tempA, tempB)
        }
    }
}

class LogValue(val value: Value, val base: Value) : Value() {
    override fun isConstant(): Boolean = value.isConstant() && base.isConstant()
    override fun differentiate(d: Char): Value {
        return DivideValue(
            value.differentiate(d),
            MultiplyValue(value, LnValue(base))
        ).simplify()
    }

    override fun compute(vars: Map<Char, Value>) = log(value.compute(vars), base.compute(vars))
    override fun toString() = "log$base($value)"
    override fun simplify(): Value {
        if (isConstant()) return compute(0).toValue()
        val tempA = value.simplify()
        val tempB = base.simplify()
        return when {
            tempA.isConstant() && tempB.isConstant() -> compute(0).toValue()
            tempA == tempB -> ONE
            else -> LogValue(tempA, tempB)
        }
    }
}

class LnValue(val value: Value) : Value() {
    override fun isConstant(): Boolean = value.isConstant()
    override fun compute(vars: Map<Char, Value>) = ln(value.compute(vars))
    override fun toString() = "ln($value)"

    override fun differentiate(d: Char): Value {
        return DivideValue(value.differentiate(d), value).simplify()
    }

    override fun simplify(): Value {
        if (isConstant()) {
            return compute(0).toValue()
        }
        when (val simpleValue = value.simplify()) {
            ONE -> return ZERO // ln(1) = 0
            E -> return ONE // ln(e) = 1
            is ExpValue -> return simpleValue.value.simplify() // ln(e^x) = x
            is PowerValue -> return MultiplyValue( // ln(x^a) = a * ln(x)
                simpleValue.exponent,
                LnValue(simpleValue.base)
            ).simplify()

            is MultiplyValue -> return PlusValue( // ln(a * b) = ln(a) + ln(b)
                LnValue(simpleValue.a),
                LnValue(simpleValue.b)
            ).simplify()

            is DivideValue -> return MinusValue( // ln(a / b) = ln(a) - ln(b)
                LnValue(simpleValue.a),
                LnValue(simpleValue.b)
            ).simplify()

            else -> return LnValue(simpleValue)
        }
    }
}

class LdValue(val value: Value) : Value() {
    override fun isConstant(): Boolean = value.isConstant()
    override fun differentiate(d: Char): Value = toLn().differentiate(d).simplify()

    private fun toLn(): Value {
        return DivideValue(LnValue(value), LN_TWO).simplify()
    }

    override fun compute(vars: Map<Char, Value>) = log2(value.compute(vars))
    override fun toString() = "ld($value)"

    override fun simplify(): Value {
        if (isConstant()) {
            return compute(0).toValue()
        }
        return LdValue(value.simplify())
    }
}

class ExpValue(val value: Value) : Value() {
    override fun isConstant(): Boolean = value.isConstant()
    override fun differentiate(d: Char): Value = MultiplyValue(this, value.differentiate(d)).simplify()

    override fun compute(vars: Map<Char, Value>) = exp(value.compute(vars))
    override fun toString() = "exp($value)"

    override fun simplify(): Value {
        val simplified = value.simplify()

        return when {
            isConstant() -> compute(0).toValue()
            simplified == ONE -> E
            simplified == ZERO -> ONE
            simplified is LnValue -> simplified.value  // e^ln(x) = x
            simplified is MultiplyValue -> { // e^(a * ln(b)) = b^a
                if (simplified.b is LnValue) {
                    PowerValue(simplified.b.value, simplified.a).simplify()
                } else if (simplified.a is LnValue) {
                    PowerValue(simplified.a.value, simplified.b).simplify()
                } else {
                    ExpValue(simplified)
                }
            }

            else -> ExpValue(simplified)
        }
    }
}

class SqrtValue(val value: Value) : Value() {
    override fun isConstant(): Boolean = value.isConstant()
    override fun differentiate(d: Char): Value {
        return DivideValue(
            value.differentiate(d),
            MultiplyValue(TWO, this)
        ).simplify()
    }

    override fun compute(vars: Map<Char, Value>) = sqrt(value.compute(vars))
    override fun toString() = "sqrt($value)"
    override fun simplify(): Value {
        if (isConstant()) {
            return compute(0).toValue()
        }
        return when (val simplified = value.simplify()) {
            ZERO -> ZERO
            ONE -> ONE
            is PowerValue if simplified.exponent == TWO -> simplified.base
            is ConstantValue -> SqrtValue(simplified).compute(0).toValue()
            else -> SqrtValue(simplified)
        }
    }
}

class CbrtValue(val value: Value) : Value() {
    override fun isConstant(): Boolean = value.isConstant()
    override fun differentiate(d: Char): Value {
        // d/dx [cbrt(u)] = u' / (3 * (cbrt(u))²)
        return DivideValue(
            value.differentiate(d),
            MultiplyValue(
                THREE,
                PowerValue(this, TWO)
            )
        ).simplify()
    }

    override fun compute(vars: Map<Char, Value>) = cbrt(value.compute(vars))
    override fun toString() = "cbrt($value)"
    override fun simplify(): Value {
        return when (val simplified = value.simplify()) {
            ZERO -> ZERO // cbrt(0) = 0
            ONE -> ONE // cbrt(1) = 1
            NEG_ONE -> NEG_ONE // cbrt(-1) = -1
            is PowerValue if simplified.exponent == THREE -> simplified.base
            is ConstantValue -> CbrtValue(simplified).compute(0).toValue()
            else -> CbrtValue(simplified)
        }
    }
}

class NthRootValue(val value: Value, val n: Int) : Value() {
    override fun isConstant(): Boolean = value.isConstant()
    override fun differentiate(d: Char): Value {
        return DivideValue(
            value.differentiate(d),
            MultiplyValue(
                MultiplyValue(n.toValue(), this),
                PowerValue(this, (n - 1).toValue())
            )
        ).simplify()
    }

    override fun compute(vars: Map<Char, Value>) = MathUtil.nthRoot(value.compute(vars), n)
    override fun toString() = "root$n($value)"
    override fun simplify(): Value {
        if (isConstant()) {
            return compute(0).toValue()
        }
        return when (value) {
            is PowerValue if value.exponent.simplify() == n.toValue() -> value
            else -> NthRootValue(value.simplify(), n)
        }
    }
}

class SinValue(val inner: Value) : Value() {
    override fun isConstant(): Boolean = inner.isConstant()
    override fun differentiate(d: Char): Value {
        return MultiplyValue(CosValue(inner), inner.differentiate(d)).simplify()
    }

    override fun compute(vars: Map<Char, Value>) = sin(inner.compute(vars))
    override fun toString() = "sin($inner)"
    override fun simplify(): Value {
        if (isConstant()) {
            return compute(0).toValue()
        }
        return when (val simplified = inner.simplify()) {
            ZERO -> ZERO // sin(0) = 0
            PI -> ZERO // sin(π) = 0
            DivideValue(PI, TWO) -> ONE // sin(π/2) = 1
            is MultiplyValue if simplified.a == NEG_ONE -> { // sin(-x) = -sin(x)
                MultiplyValue(NEG_ONE, SinValue(simplified.b)).simplify()
            }

            else -> SinValue(simplified)
        }
    }
}

class AsinValue(val inner: Value) : Value() {
    override fun isConstant(): Boolean = inner.isConstant()
    override fun differentiate(d: Char): Value = DivideValue(
        inner.differentiate(d),
        SqrtValue(ONE.minus(inner.pow(2)))
    ).simplify()

    override fun compute(vars: Map<Char, Value>) = asin(inner.compute(vars))
    override fun toString() = "asin($inner)"
    override fun simplify(): Value {
        if (isConstant()) {
            return compute(0).toValue()
        }
        return AsinValue(inner.simplify())
    }
}

class CosValue(val inner: Value) : Value() {
    override fun isConstant(): Boolean = inner.isConstant()
    override fun differentiate(d: Char): Value {
        return MultiplyValue(SinValue(inner).multiply(-1), inner.differentiate(d)).simplify()
    }

    override fun compute(vars: Map<Char, Value>) = cos(inner.compute(vars))
    override fun toString() = "cos($inner)"
    override fun simplify(): Value {
        if (isConstant()) {
            return compute(0).toValue()
        }

        return when (val simplified = inner.simplify()) {
            ZERO -> ONE // cos(0) = 1
            PI -> NEG_ONE // cos(π) = -1
            DivideValue(PI, TWO) -> ZERO // cos(π/2) = 0
            is MultiplyValue if simplified.a == NEG_ONE -> CosValue(simplified.b) // cos(-x) = cos(x)
            else -> CosValue(simplified)
        }
    }
}

class AcosValue(val inner: Value) : Value() {
    override fun isConstant(): Boolean = inner.isConstant()
    override fun differentiate(d: Char): Value {
        return MultiplyValue(
            DivideValue(
                ONE.multiply(-1),
                SqrtValue(ONE.minus(inner.pow(2)))
            ),
            inner.differentiate(d)
        ).simplify()
    }

    override fun compute(vars: Map<Char, Value>) = acos(inner.compute(vars))
    override fun toString() = "acos($inner)"
    override fun simplify(): Value {
        if (isConstant()) {
            return compute(0).toValue()
        }
        return AcosValue(inner.simplify())
    }
}

class TanValue(val inner: Value) : Value() {
    override fun isConstant(): Boolean = inner.isConstant()
    override fun differentiate(d: Char): Value {
        return DivideValue(
            inner.differentiate(d).simplify(),
            CosValue(inner.simplify()).pow(2)
        ).simplify()
    }

    override fun compute(vars: Map<Char, Value>) = tan(inner.compute(vars))
    override fun toString() = "tan($inner)"
    override fun simplify(): Value {
        if (isConstant()) {
            return compute(0).toValue()
        }
        return TanValue(inner.simplify())
    }
}

class AtanValue(val inner: Value) : Value() {
    override fun isConstant(): Boolean = inner.isConstant()
    override fun differentiate(d: Char): Value {
        return MultiplyValue(
            ONE.divide(ONE.plus(inner.pow(2))),
            inner.differentiate(d)
        ).simplify()
    }

    override fun compute(vars: Map<Char, Value>) = atan(inner.compute(vars))
    override fun toString() = "atan($inner)"
    override fun simplify(): Value {
        if (isConstant()) {
            return compute(0).toValue()
        }
        return AtanValue(inner.simplify())
    }
}

