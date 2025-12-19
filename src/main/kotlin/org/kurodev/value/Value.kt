package org.kurodev.value

import org.kurodev.MathUtil
import kotlin.math.*

fun Double.toValue(): Value {
    return ConstantValue(this)
}

fun Int.toValue(): Value {
    return ConstantValue(this)
}

class ConstantValue(val num: Double) : Value() {
    constructor(num: Int) : this(num.toDouble())

    override fun compute(x: Double) = num
    override fun toString(): String = if (num % 1.0 == 0.0) num.toInt().toString() else num.toString()
}

val NO_VALUE = ConstantValue(Double.NaN)

/**
 * @param factor The factor to multiply X by.
 */
open class Value(val factor: Double) {
    constructor(factor: Int) : this(factor.toDouble());
    constructor() : this(1);

    open fun compute(x: Double): Double = x
    override fun toString(): String {
        if (factor == 1.0)
            return "x"
        if (factor % 1.0 == 0.0) {
            return factor.toInt().toString().plus("x");
        }
        return factor.toString().plus("x");
    }

    fun compute(x: Int) = compute(x.toDouble())
    open fun differentiate() = factor.toValue()

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
}

class PlusValue(val a: Value, val b: Value) : Value() {
    override fun compute(x: Double) = a.compute(x) + b.compute(x)
    override fun toString() = "$a + $b"
    override fun differentiate(): Value {
        return PlusValue(a.differentiate(), b.differentiate())
    }
}

class MinusValue(val a: Value, val b: Value) : Value() {
    override fun compute(x: Double) = a.compute(x) - b.compute(x)
    override fun toString() = "$a - $b"
    override fun differentiate(): Value {
        return MinusValue(a.differentiate(), b.differentiate())
    }
}

class MultiplyValue(val a: Value, val b: Value) : Value() {
    override fun compute(x: Double) = a.compute(x) * b.compute(x)
    override fun toString() = "$a * $b"
    override fun differentiate(): Value {
        return PlusValue(MultiplyValue(a.differentiate(), b), MultiplyValue(a, b.differentiate()))
    }
}

class DivideValue(val a: Value, val b: Value) : Value() {
    override fun compute(x: Double): Double {
        val div = b.compute(x)
        if (div == 0.0) throw IllegalStateException("Division by zero")
        return a.compute(x) / div
    }

    override fun differentiate(): Value {
        return DivideValue(
            MinusValue(MultiplyValue(a.differentiate(), b), MultiplyValue(a, b.differentiate())),
            b.pow(2)
        )
    }

    override fun toString() = "$a / $b"
}

class PowerValue(val base: Value, val exponent: Value) : Value() {
    override fun compute(x: Double) = base.compute(x).pow(exponent.compute(x))
    override fun toString() = "$base^$exponent"
}

class LogValue(val value: Value, val base: Value) : Value() {
    override fun compute(x: Double) = log(value.compute(x), base.compute(x))
    override fun toString() = "log$base($value)"

}

class LnValue(val value: Value) : Value() {
    override fun compute(x: Double) = ln(value.compute(x))
    override fun toString() = "ln($value)"
    override fun differentiate(): Value {
        return DivideValue(value.differentiate(), value)
    }
}

class LdValue(val value: Value) : Value() {
    override fun compute(x: Double) = log2(value.compute(x))
    override fun toString() = "ld($value)"
}

class ExpValue(val value: Value) : Value() {
    override fun compute(x: Double) = exp(value.compute(x))
    override fun toString() = "exp($value)"
}

class SqrtValue(val value: Value) : Value() {
    override fun compute(x: Double) = sqrt(value.compute(x))
    override fun toString() = "sqrt($value)"
}

class CbrtValue(val value: Value) : Value() {
    override fun compute(x: Double) = cbrt(value.compute(x))
    override fun toString() = "cbrt($value)"
}

class NthRootValue(val value: Value, val n: Int) : Value() {
    override fun compute(x: Double) = MathUtil.nthRoot(value.compute(x), n)
    override fun toString() = "root($n)"
}

class SinValue(val inner: Value) : Value() {
    override fun compute(x: Double) = sin(inner.compute(x))
    override fun toString() = "sin($inner)"
}

class AsinValue(val inner: Value) : Value() {
    override fun compute(x: Double) = asin(inner.compute(x))
    override fun toString() = "asin($inner)"
}

class CosValue(val inner: Value) : Value() {
    override fun compute(x: Double) = cos(inner.compute(x))
    override fun toString() = "cos($inner)"
}

class AcosValue(val inner: Value) : Value() {
    override fun compute(x: Double) = acos(inner.compute(x))
    override fun toString() = "acos($inner)"
}

class TanValue(val inner: Value) : Value() {
    override fun compute(x: Double) = tan(inner.compute(x))
    override fun toString() = "tan($inner)"
}

class AtanValue(val inner: Value) : Value() {
    override fun compute(x: Double) = atan(inner.compute(x))
    override fun toString() = "atan($inner)"
}

