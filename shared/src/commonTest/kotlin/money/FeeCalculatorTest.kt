package money

import money.Currency.Companion.JPY
import money.Currency.Companion.KWD
import money.Currency.Companion.USD
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class FeeCalculatorTest {

    private fun usd(text: String) = Money.parse(text, USD)

    @Test
    fun percentagePlusFixed() {
        val calc = FeeCalculator(FeeRules(USD, percentBps = 150, fixed = usd("0.30")))
        // 1.5% of 100.00 = 1.50, + 0.30
        assertEquals(usd("1.80"), calc.feeFor(usd("100.00")))
    }

    @Test
    fun fractionalCentsAreRoundedUpByDefault() {
        val calc = FeeCalculator(FeeRules(USD, percentBps = 150))
        // 1.5% of 10.01 = 0.15015 -> 0.16, never undercharge
        assertEquals(usd("0.16"), calc.feeFor(usd("10.01")))
    }

    @Test
    fun roundingModeIsConfigurable() {
        val calc = FeeCalculator(FeeRules(USD, percentBps = 150, rounding = RoundingMode.HALF_EVEN))
        assertEquals(usd("0.15"), calc.feeFor(usd("10.01")))
    }

    @Test
    fun minAndMaxClampTheFee() {
        val calc = FeeCalculator(FeeRules(USD, percentBps = 100, min = usd("1.00"), max = usd("25.00")))
        assertEquals(usd("1.00"), calc.feeFor(usd("10.00")))      // 0.10 -> clamped up
        assertEquals(usd("5.00"), calc.feeFor(usd("500.00")))     // inside the band
        assertEquals(usd("25.00"), calc.feeFor(usd("10000.00")))  // 100.00 -> clamped down
    }

    @Test
    fun zeroDecimalCurrency() {
        val calc = FeeCalculator(FeeRules(JPY, percentBps = 150))
        // 1.5% of ¥1,001 = ¥15.015 -> ¥16
        assertEquals(Money.parse("16", JPY), calc.feeFor(Money.parse("1001", JPY)))
    }

    @Test
    fun threeDecimalCurrency() {
        val calc = FeeCalculator(FeeRules(KWD, percentBps = 150, fixed = Money.parse("0.250", KWD)))
        // 1.5% of 10.000 KWD = 0.150, + 0.250
        assertEquals(Money.parse("0.400", KWD), calc.feeFor(Money.parse("10.000", KWD)))
    }

    @Test
    fun totalDebitIsAmountPlusFee() {
        val calc = FeeCalculator(FeeRules(USD, percentBps = 150, fixed = usd("0.30")))
        assertEquals(usd("101.80"), calc.totalDebit(usd("100.00")))
    }

    @Test
    fun rejectsInvalidInput() {
        val calc = FeeCalculator(FeeRules(USD, percentBps = 150))
        assertFailsWith<IllegalArgumentException> { calc.feeFor(Money.parse("10", KWD)) }
        assertFailsWith<IllegalArgumentException> { calc.feeFor(usd("-1.00")) }
    }
}
