package money

/** Transfer limits for a customer tier. Both limits are inclusive: sending exactly the limit is allowed. */
class TransferLimits(val perTransaction: Money, val daily: Money) {
    init {
        require(perTransaction.currency == daily.currency) { "Limits must share a currency" }
    }
}

sealed interface LimitResult {
    data object Allowed : LimitResult
    data class ExceedsPerTransaction(val limit: Money) : LimitResult
    data class ExceedsDaily(val remainingToday: Money) : LimitResult
}

class LimitChecker(private val limits: TransferLimits) {

    /**
     * @param amount the amount the customer wants to send.
     * @param sentToday the total already sent today, before this transfer.
     */
    fun check(amount: Money, sentToday: Money): LimitResult {
        require(!amount.isNegative && !amount.isZero) { "Amount must be positive: $amount" }

        if (amount > limits.perTransaction) {
            return LimitResult.ExceedsPerTransaction(limits.perTransaction)
        }
        if (sentToday + amount > limits.daily) {
            val remaining = limits.daily - sentToday
            return LimitResult.ExceedsDaily(if (remaining.isNegative) Money.zero(remaining.currency) else remaining)
        }
        return LimitResult.Allowed
    }
}
