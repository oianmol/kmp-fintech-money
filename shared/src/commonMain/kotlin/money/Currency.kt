package money

/**
 * An ISO 4217 currency and its minor-unit exponent.
 *
 * The exponent is the whole point: not every currency has 2 decimal places.
 * JPY has 0, KWD and BHD have 3. Hardcoding "cents" is a classic fintech bug.
 */
class Currency private constructor(val code: String, val exponent: Int) {

    /** How many minor units make one major unit: 10^exponent. */
    val minorPerMajor: Long = pow10(exponent)

    override fun equals(other: Any?) = other is Currency && other.code == code
    override fun hashCode() = code.hashCode()
    override fun toString() = code

    companion object {
        val USD = Currency("USD", 2)
        val EUR = Currency("EUR", 2)
        val INR = Currency("INR", 2)
        val AED = Currency("AED", 2)
        val JPY = Currency("JPY", 0)
        val KWD = Currency("KWD", 3)
        val BHD = Currency("BHD", 3)

        private val known = listOf(USD, EUR, INR, AED, JPY, KWD, BHD).associateBy { it.code }

        fun of(code: String): Currency =
            requireNotNull(known[code]) { "Unsupported currency: $code" }
    }
}

internal fun pow10(exponent: Int): Long {
    require(exponent in 0..18) { "Exponent out of range: $exponent" }
    var result = 1L
    repeat(exponent) { result *= 10 }
    return result
}
