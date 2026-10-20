package money

/**
 * An amount of money: an integer count of minor units (cents, paise, fils) plus its currency.
 *
 * Never a Double. Floating point can't represent 0.1 exactly.
 * Arithmetic is overflow-checked and refuses to mix currencies.
 */
class Money private constructor(val minorUnits: Long, val currency: Currency) : Comparable<Money> {

    operator fun plus(other: Money): Money {
        requireSameCurrency(other)
        return Money(addExact(minorUnits, other.minorUnits), currency)
    }

    operator fun minus(other: Money): Money {
        requireSameCurrency(other)
        return Money(subtractExact(minorUnits, other.minorUnits), currency)
    }

    override fun compareTo(other: Money): Int {
        requireSameCurrency(other)
        return minorUnits.compareTo(other.minorUnits)
    }

    val isNegative: Boolean get() = minorUnits < 0
    val isZero: Boolean get() = minorUnits == 0L

    /** Plain decimal string with exactly [Currency.exponent] fraction digits, e.g. "1250.50", "1250", "1.250". */
    fun toPlainString(): String {
        val scale = currency.minorPerMajor
        val sign = if (minorUnits < 0) "-" else ""
        val major = (minorUnits / scale).let { if (it < 0) -it else it }
        val minor = (minorUnits % scale).let { if (it < 0) -it else it }
        if (currency.exponent == 0) return "$sign$major"
        return "$sign$major.${minor.toString().padStart(currency.exponent, '0')}"
    }

    override fun equals(other: Any?) =
        other is Money && other.minorUnits == minorUnits && other.currency == currency

    override fun hashCode() = 31 * minorUnits.hashCode() + currency.hashCode()

    override fun toString() = "${toPlainString()} ${currency.code}"

    private fun requireSameCurrency(other: Money) {
        require(currency == other.currency) { "Currency mismatch: $currency vs ${other.currency}" }
    }

    companion object {
        fun ofMinor(minorUnits: Long, currency: Currency) = Money(minorUnits, currency)

        fun zero(currency: Currency) = Money(0, currency)

        /**
         * Parses a decimal amount in major units, e.g. "12.5" USD -> 1250 minor units, "1.250" KWD -> 1250.
         * More fraction digits than the currency allows is an error, never a silent rounding.
         */
        fun parse(text: String, currency: Currency): Money {
            val trimmed = text.trim()
            require(Regex("""-?\d+(\.\d+)?""").matches(trimmed)) { "Not a decimal amount: '$text'" }

            val negative = trimmed.startsWith("-")
            val unsigned = trimmed.removePrefix("-")
            val majorPart = unsigned.substringBefore('.')
            val fractionPart = unsigned.substringAfter('.', missingDelimiterValue = "")

            val scale = currency.minorPerMajor
            require(fractionPart.length <= currency.exponent) {
                "'$text' has more decimal places than ${currency.code} allows (${currency.exponent})"
            }

            val major = majorPart.toLongOrNull() ?: throw ArithmeticException("Amount too large: '$text'")
            val minor = if (fractionPart.isEmpty()) 0L else fractionPart.padEnd(currency.exponent, '0').toLong()
            val magnitude = addExact(multiplyExact(major, scale), minor)
            return Money(if (negative) -magnitude else magnitude, currency)
        }
    }
}
