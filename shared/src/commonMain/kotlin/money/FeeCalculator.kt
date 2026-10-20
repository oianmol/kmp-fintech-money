package money

/**
 * Fee rules for one product or corridor.
 *
 * fee = clamp(round(amount × percentBps / 10_000) + fixed, min, max)
 *
 * @param percentBps percentage fee in basis points (150 = 1.50%).
 * @param rounding how the percentage part is rounded to a whole minor unit. Fees are usually
 *   rounded [RoundingMode.UP] so the provider never undercharges by a fraction of a cent.
 */
class FeeRules(
    val currency: Currency,
    val percentBps: Long,
    val fixed: Money = Money.zero(currency),
    val min: Money? = null,
    val max: Money? = null,
    val rounding: RoundingMode = RoundingMode.UP,
) {
    init {
        require(percentBps >= 0) { "percentBps must be >= 0" }
        require(fixed.currency == currency) { "fixed fee currency must be $currency" }
        min?.let { require(it.currency == currency) { "min fee currency must be $currency" } }
        max?.let { require(it.currency == currency) { "max fee currency must be $currency" } }
        if (min != null && max != null) require(min <= max) { "min fee must be <= max fee" }
    }
}

class FeeCalculator(private val rules: FeeRules) {

    fun feeFor(amount: Money): Money {
        require(amount.currency == rules.currency) { "Amount is ${amount.currency}, rules are ${rules.currency}" }
        require(!amount.isNegative) { "Amount must not be negative: $amount" }

        val percentMinor = divide(multiplyExact(amount.minorUnits, rules.percentBps), BPS_PER_WHOLE, rules.rounding)
        var fee = Money.ofMinor(percentMinor, rules.currency) + rules.fixed

        rules.min?.let { if (fee < it) fee = it }
        rules.max?.let { if (fee > it) fee = it }
        return fee
    }

    /** What the sender pays in total: amount + fee. */
    fun totalDebit(amount: Money): Money = amount + feeFor(amount)

    private companion object {
        const val BPS_PER_WHOLE = 10_000L
    }
}
