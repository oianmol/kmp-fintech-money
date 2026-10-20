package money

import money.Currency.Companion.USD
import kotlin.test.Test
import kotlin.test.assertEquals

class LimitCheckerTest {

    private fun usd(text: String) = Money.parse(text, USD)

    private val checker = LimitChecker(TransferLimits(perTransaction = usd("1000.00"), daily = usd("2500.00")))

    @Test
    fun allowsWithinLimits() {
        assertEquals(LimitResult.Allowed, checker.check(usd("500.00"), sentToday = usd("0")))
    }

    @Test
    fun perTransactionLimitIsInclusive() {
        assertEquals(LimitResult.Allowed, checker.check(usd("1000.00"), sentToday = usd("0")))
        assertEquals(
            LimitResult.ExceedsPerTransaction(usd("1000.00")),
            checker.check(usd("1000.01"), sentToday = usd("0")),
        )
    }

    @Test
    fun dailyLimitIsInclusive() {
        assertEquals(LimitResult.Allowed, checker.check(usd("500.00"), sentToday = usd("2000.00")))
        assertEquals(
            LimitResult.ExceedsDaily(remainingToday = usd("500.00")),
            checker.check(usd("500.01"), sentToday = usd("2000.00")),
        )
    }

    @Test
    fun remainingNeverGoesNegative() {
        // e.g. the limit was lowered after the customer had already sent more
        assertEquals(
            LimitResult.ExceedsDaily(remainingToday = usd("0")),
            checker.check(usd("1.00"), sentToday = usd("3000.00")),
        )
    }
}
