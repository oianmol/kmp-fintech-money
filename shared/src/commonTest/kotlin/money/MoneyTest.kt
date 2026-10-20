package money

import money.Currency.Companion.BHD
import money.Currency.Companion.JPY
import money.Currency.Companion.KWD
import money.Currency.Companion.USD
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class MoneyTest {

    @Test
    fun parsesUsingTheCurrencyExponent() {
        assertEquals(1250L, Money.parse("12.50", USD).minorUnits)
        assertEquals(1250L, Money.parse("12.5", USD).minorUnits)
        assertEquals(1250L, Money.parse("1250", JPY).minorUnits)
        assertEquals(1250L, Money.parse("1.250", KWD).minorUnits)
        assertEquals(1_000L, Money.parse("1", BHD).minorUnits)
        assertEquals(-5L, Money.parse("-0.05", USD).minorUnits)
    }

    @Test
    fun rejectsMoreDecimalsThanTheCurrencyAllows() {
        assertFailsWith<IllegalArgumentException> { Money.parse("1.5", JPY) }
        assertFailsWith<IllegalArgumentException> { Money.parse("1.005", USD) }
    }

    @Test
    fun rejectsGarbage() {
        listOf("", "abc", "1,000.00", "1.", ".5", "1e3").forEach { input ->
            assertFailsWith<IllegalArgumentException>(input) { Money.parse(input, USD) }
        }
    }

    @Test
    fun formatsUsingTheCurrencyExponent() {
        assertEquals("12.50", Money.ofMinor(1250, USD).toPlainString())
        assertEquals("0.05", Money.ofMinor(5, USD).toPlainString())
        assertEquals("-0.05", Money.ofMinor(-5, USD).toPlainString())
        assertEquals("1250", Money.ofMinor(1250, JPY).toPlainString())
        assertEquals("1.250", Money.ofMinor(1250, KWD).toPlainString())
    }

    @Test
    fun parseAndFormatRoundTrip() {
        listOf("0.001" to KWD, "999.999" to BHD, "42" to JPY, "-10.01" to USD).forEach { (text, currency) ->
            assertEquals(text, Money.parse(text, currency).toPlainString())
        }
    }

    @Test
    fun arithmeticStaysInTheSameCurrency() {
        assertEquals(Money.ofMinor(300, USD), Money.ofMinor(100, USD) + Money.ofMinor(200, USD))
        assertEquals(Money.ofMinor(-100, USD), Money.ofMinor(100, USD) - Money.ofMinor(200, USD))
        assertFailsWith<IllegalArgumentException> { Money.ofMinor(100, USD) + Money.ofMinor(100, KWD) }
    }

    @Test
    fun overflowIsAnErrorNotAWrapAround() {
        assertFailsWith<ArithmeticException> { Money.ofMinor(Long.MAX_VALUE, USD) + Money.ofMinor(1, USD) }
        assertFailsWith<ArithmeticException> { Money.ofMinor(Long.MIN_VALUE, USD) - Money.ofMinor(1, USD) }
        assertFailsWith<ArithmeticException> { Money.parse("99999999999999999.99", USD) }
    }
}
