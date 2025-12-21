package org.kurodev.value

import org.kurodev.MathUtil
import kotlin.math.*

private val ZERO: Value = 0.toValue()
private val ONE: Value = 1.toValue()

fun Double.toValue(): Value {
    return ConstantValue(this)
}

fun Int.toValue(): Value {
    return ConstantValue(this)
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

    override fun differentiate(): Value = ONE
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

}

class ConstantValue(val num: Double) : Value() {
    constructor(num: Int) : this(num.toDouble())

    override fun compute(vars: Map<Char, Value>) = num
    override fun toString(): String = if (num % 1.0 == 0.0) num.toInt().toString() else num.toString()
    override fun isConstant(): Boolean = true
    override fun differentiate(): Value = ZERO;
}


abstract class Value() {

    override fun toString(): String = "undefined";
    abstract fun compute(vars: Map<Char, Value>): Double;

    fun compute(x: Int): Double = compute(x.toValue())
    fun compute(x: Double): Double = compute(x.toValue())
    fun compute(x: Value): Double {
        val vars = HashMap<Char, Value>()
        vars['x'] = x;
        return compute(vars)
    }

    abstract fun isConstant(): Boolean;

    abstract fun differentiate(): Value;

    /**
     * Simplifies all constant values by precomputing them
     */
    open fun simplify(): Value {
        if (isConstant()) {
            return compute(0).toValue()
        }
        return this
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

class PlusValue(val a: Value, val b: Value) : Value() {
    override fun compute(vars: Map<Char, Value>) = a.compute(vars) + b.compute(vars)
    override fun toString() = "$a + $b"
    override fun isConstant(): Boolean = a.isConstant() && b.isConstant()

    override fun simplify(): Value {
        if (isConstant()) {
            return compute(0).toValue()
        }
        val tempA = a.simplify()
        val tempB = b.simplify()
        if (tempA.isConstant() && tempB.isConstant()) {
            return compute(0).toValue()
        }
        if (tempA == ZERO) return tempB
        if (tempB == ZERO) return tempA
        return PlusValue(tempA, tempB)
    }

    override fun differentiate(): Value {
        return PlusValue(a.differentiate(), b.differentiate())
    }
}

class MinusValue(val a: Value, val b: Value) : Value() {
    override fun compute(vars: Map<Char, Value>) = a.compute(vars) - b.compute(vars)
    override fun toString() = "$a - $b"
    override fun isConstant(): Boolean = a.isConstant() && b.isConstant()
    override fun simplify(): Value {
        if (isConstant()) {
            return compute(0).toValue()
        }
        val tempA = a.simplify()
        val tempB = b.simplify()
        if (tempA.isConstant() && tempB.isConstant()) {
            return compute(0).toValue()
        }
        if (tempA == tempB) {
            return ZERO
        }
        return MinusValue(tempA, tempB)
    }

    override fun differentiate(): Value {
        return MinusValue(a.differentiate(), b.differentiate())
    }
}

class MultiplyValue(val a: Value, val b: Value) : Value() {
    override fun compute(vars: Map<Char, Value>) = a.compute(vars) * b.compute(vars)
    override fun toString() = "$a * $b"
    override fun isConstant(): Boolean = a.isConstant() && b.isConstant()
    override fun simplify(): Value {
        if (isConstant()) {
            return compute(0).toValue()
        }
        val tempA = a.simplify()
        val tempB = b.simplify()

        if (tempA.isConstant() && tempB.isConstant()) {
            return compute(0).toValue()
        }

        if (tempA == ZERO && tempB == ZERO) {
            return ZERO
        }
        return MultiplyValue(tempA, tempB)
    }

    override fun differentiate(): Value {
        return PlusValue(MultiplyValue(a.differentiate(), b), MultiplyValue(a, b.differentiate()))
    }
}

class DivideValue(val a: Value, val b: Value) : Value() {
    override fun compute(vars: Map<Char, Value>): Double {
        val div = b.compute(vars)
        if (div == 0.0) throw IllegalStateException("Division by zero")
        return a.compute(vars) / div
    }

    override fun simplify(): Value {
        if (isConstant()) {
            return compute(0).toValue()
        }
        val tempA = a.simplify()
        val tempB = b.simplify()

        if (tempA.isConstant() && tempB.isConstant()) {
            return compute(0).toValue()
        }

        if (tempA == tempB) {
            return ONE
        }
        return DivideValue(tempA, tempB)
    }

    override fun isConstant(): Boolean = a.isConstant() && b.isConstant()
    override fun differentiate(): Value {
        return DivideValue(
            MinusValue(MultiplyValue(a.differentiate(), b), MultiplyValue(a, b.differentiate())),
            b.pow(2)
        )
    }

    override fun toString() = "$a / $b"
}

class PowerValue(val base: Value, val exponent: Value) : Value() {
    override fun isConstant(): Boolean = base.isConstant() && exponent.isConstant()
    override fun differentiate(): Value {
        TODO("Not yet implemented")
    }

    override fun compute(vars: Map<Char, Value>) = base.compute(vars).pow(exponent.compute(vars))
    override fun toString() = "$base^$exponent"
    override fun simplify(): Value {
        if (isConstant()) {
            return compute(0).toValue()
        }
        val tempA = base.simplify()
        val tempB = exponent.simplify()

        return when {
            tempA.isConstant() && tempB.isConstant() -> compute(0).toValue()
            tempB == ZERO -> ONE
            else -> PowerValue(base.simplify(), exponent.simplify())
        }
    }
}

class LogValue(val value: Value, val base: Value) : Value() {
    override fun isConstant(): Boolean = value.isConstant() && base.isConstant()
    override fun differentiate(): Value {
        TODO("Not yet implemented")
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

    override fun differentiate(): Value {
        return DivideValue(value.differentiate(), value)
    }

    override fun simplify(): Value {
        if (isConstant()) {
            return compute(0).toValue()
        }
        return LnValue(value.simplify())
    }
}

class LdValue(val value: Value) : Value() {
    override fun isConstant(): Boolean = value.isConstant()
    override fun differentiate(): Value {
        TODO("Not yet implemented")
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
    override fun differentiate(): Value {
        TODO("Not yet implemented")
    }

    override fun compute(vars: Map<Char, Value>) = exp(value.compute(vars))
    override fun toString() = "exp($value)"

    override fun simplify(): Value {
        if (isConstant()) {
            return compute(0).toValue()
        }
        return ExpValue(value.simplify())
    }
}

class SqrtValue(val value: Value) : Value() {
    override fun isConstant(): Boolean = value.isConstant()
    override fun differentiate(): Value {
        TODO("Not yet implemented")
    }

    override fun compute(vars: Map<Char, Value>) = sqrt(value.compute(vars))
    override fun toString() = "sqrt($value)"
    override fun simplify(): Value {
        if (isConstant()) {
            return compute(0).toValue()
        }
        return SqrtValue(value.simplify())
    }
}

class CbrtValue(val value: Value) : Value() {
    override fun isConstant(): Boolean = value.isConstant()
    override fun differentiate(): Value {
        TODO("Not yet implemented")
    }

    override fun compute(vars: Map<Char, Value>) = cbrt(value.compute(vars))
    override fun toString() = "cbrt($value)"
    override fun simplify(): Value {
        if (isConstant()) {
            return compute(0).toValue()
        }
        return CbrtValue(value.simplify())
    }
}

class NthRootValue(val value: Value, val n: Int) : Value() {
    override fun isConstant(): Boolean = value.isConstant()
    override fun differentiate(): Value {
        TODO("Not yet implemented")
    }

    override fun compute(vars: Map<Char, Value>) = MathUtil.nthRoot(value.compute(vars), n)
    override fun toString() = "root$n($value)"
    override fun simplify(): Value {
        if (isConstant()) {
            return compute(0).toValue()
        }
        return NthRootValue(value.simplify(), n)
    }
}

class SinValue(val inner: Value) : Value() {
    override fun isConstant(): Boolean = inner.isConstant()
    override fun differentiate(): Value {
        TODO("Not yet implemented")
    }

    override fun compute(vars: Map<Char, Value>) = sin(inner.compute(vars))
    override fun toString() = "sin($inner)"
    override fun simplify(): Value {
        if (isConstant()) {
            return compute(0).toValue()
        }
        return SinValue(inner.simplify())
    }
}

class AsinValue(val inner: Value) : Value() {
    override fun isConstant(): Boolean = inner.isConstant()
    override fun differentiate(): Value {
        TODO("Not yet implemented")
    }

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
    override fun differentiate(): Value {
        TODO("Not yet implemented")
    }

    override fun compute(vars: Map<Char, Value>) = cos(inner.compute(vars))
    override fun toString() = "cos($inner)"
    override fun simplify(): Value {
        if (isConstant()) {
            return compute(0).toValue()
        }
        return CosValue(inner.simplify())
    }
}

class AcosValue(val inner: Value) : Value() {
    override fun isConstant(): Boolean = inner.isConstant()
    override fun differentiate(): Value {
        TODO("Not yet implemented")
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
    override fun differentiate(): Value {
        TODO("Not yet implemented")
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
    override fun differentiate(): Value {
        TODO("Not yet implemented")
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

