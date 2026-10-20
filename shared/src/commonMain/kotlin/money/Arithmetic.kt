package money

// java.lang.Math.addExact / multiplyExact are JVM-only, so they can't be used in commonMain.
// These are the multiplatform equivalents.

internal fun addExact(a: Long, b: Long): Long {
    val result = a + b
    // Overflow iff both operands have the same sign and the result's sign differs.
    if (((a xor result) and (b xor result)) < 0) throw ArithmeticException("Long overflow: $a + $b")
    return result
}

internal fun subtractExact(a: Long, b: Long): Long {
    val result = a - b
    // Overflow iff the operands have different signs and the result's sign differs from a.
    if (((a xor b) and (a xor result)) < 0) throw ArithmeticException("Long overflow: $a - $b")
    return result
}

internal fun multiplyExact(a: Long, b: Long): Long {
    if (a == 0L || b == 0L) return 0L
    val result = a * b
    if (result / b != a || (a == -1L && b == Long.MIN_VALUE) || (b == -1L && a == Long.MIN_VALUE)) {
        throw ArithmeticException("Long overflow: $a * $b")
    }
    return result
}

enum class RoundingMode {
    /** Toward zero. */
    DOWN,

    /** Away from zero. For fees, this means never undercharge by a fraction of a minor unit. */
    UP,

    /** Nearest; ties away from zero. */
    HALF_UP,

    /** Nearest; ties to the even neighbour ("banker's rounding"). */
    HALF_EVEN,
}

/** Divides [numerator] by a positive [denominator], rounding the quotient with [mode]. */
internal fun divide(numerator: Long, denominator: Long, mode: RoundingMode): Long {
    require(denominator > 0) { "Denominator must be positive: $denominator" }
    val quotient = numerator / denominator // truncates toward zero
    val remainder = numerator % denominator
    if (remainder == 0L) return quotient

    val awayFromZero = if (numerator < 0) quotient - 1 else quotient + 1
    val absRemainder = if (remainder < 0) -remainder else remainder
    // Compare 2*|r| with d without overflowing: |r| vs d - |r|.
    val distanceToNext = denominator - absRemainder

    return when (mode) {
        RoundingMode.DOWN -> quotient
        RoundingMode.UP -> awayFromZero
        RoundingMode.HALF_UP -> if (absRemainder >= distanceToNext) awayFromZero else quotient
        RoundingMode.HALF_EVEN -> when {
            absRemainder > distanceToNext -> awayFromZero
            absRemainder < distanceToNext -> quotient
            quotient % 2 == 0L -> quotient
            else -> awayFromZero
        }
    }
}
