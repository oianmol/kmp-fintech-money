package money

import money.RoundingMode.DOWN
import money.RoundingMode.HALF_EVEN
import money.RoundingMode.HALF_UP
import money.RoundingMode.UP
import kotlin.test.Test
import kotlin.test.assertEquals

class RoundingTest {

    // numerator / 10, so each case is "x.y" -> rounded
    private fun r(numerator: Long, mode: RoundingMode) = divide(numerator, 10, mode)

    @Test
    fun exactDivisionIsUnchangedInEveryMode() {
        RoundingMode.entries.forEach { assertEquals(3, r(30, it)) }
    }

    @Test
    fun down() {
        assertEquals(1, r(19, DOWN))
        assertEquals(-1, r(-19, DOWN))
    }

    @Test
    fun up() {
        assertEquals(2, r(11, UP))
        assertEquals(-2, r(-11, UP))
    }

    @Test
    fun halfUp() {
        assertEquals(1, r(14, HALF_UP))
        assertEquals(2, r(15, HALF_UP))
        assertEquals(3, r(25, HALF_UP))
        assertEquals(-2, r(-15, HALF_UP))
    }

    @Test
    fun halfEven() {
        assertEquals(2, r(15, HALF_EVEN))
        assertEquals(2, r(25, HALF_EVEN))
        assertEquals(3, r(26, HALF_EVEN))
        assertEquals(-2, r(-25, HALF_EVEN))
    }
}
