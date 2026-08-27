package com.example

import com.example.engine.CalculatorEngine
import com.example.model.CalculatorKey
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CalculatorEngineTest {

    private lateinit var engine: CalculatorEngine

    @Before
    fun setup() {
        engine = CalculatorEngine(useThousandsSeparator = true)
    }

    private fun pressKeys(vararg keys: CalculatorKey) {
        for (key in keys) {
            engine.onKeyPress(key)
        }
    }

    @Test
    fun testAddition_OnePlusOne() {
        // 1 + 1 = 2
        pressKeys(
            CalculatorKey.Digit("1"),
            CalculatorKey.Add,
            CalculatorKey.Digit("1"),
            CalculatorKey.Equals
        )
        val state = engine.getState()
        assertEquals("2", state.primaryDisplay)
        assertFalse(state.isError)
        assertNotNull(state.lastCalculation)
    }

    @Test
    fun testSubtraction_TenMinusThree() {
        // 10 - 3 = 7
        pressKeys(
            CalculatorKey.Digit("1"),
            CalculatorKey.Digit("0"),
            CalculatorKey.Subtract,
            CalculatorKey.Digit("3"),
            CalculatorKey.Equals
        )
        val state = engine.getState()
        assertEquals("7", state.primaryDisplay)
    }

    @Test
    fun testMultiplication_FiveTimesFive() {
        // 5 * 5 = 25
        pressKeys(
            CalculatorKey.Digit("5"),
            CalculatorKey.Multiply,
            CalculatorKey.Digit("5"),
            CalculatorKey.Equals
        )
        val state = engine.getState()
        assertEquals("25", state.primaryDisplay)
    }

    @Test
    fun testDivision_HundredDividedByFour() {
        // 100 / 4 = 25
        pressKeys(
            CalculatorKey.Digit("1"),
            CalculatorKey.DoubleZero,
            CalculatorKey.Divide,
            CalculatorKey.Digit("4"),
            CalculatorKey.Equals
        )
        val state = engine.getState()
        assertEquals("25", state.primaryDisplay)
    }

    @Test
    fun testPercentage_FifteenPercent() {
        // 15 % = 0.15
        pressKeys(
            CalculatorKey.Digit("1"),
            CalculatorKey.Digit("5"),
            CalculatorKey.Percent
        )
        val state = engine.getState()
        assertEquals("0.15", state.primaryDisplay)
    }

    @Test
    fun testDecimalPrecision_PointOnePlusPointTwo() {
        // 0.1 + 0.2 = 0.3 (Ensuring exact BigDecimal decimal precision, no 0.30000000004)
        pressKeys(
            CalculatorKey.DecimalDot,
            CalculatorKey.Digit("1"),
            CalculatorKey.Add,
            CalculatorKey.DecimalDot,
            CalculatorKey.Digit("2"),
            CalculatorKey.Equals
        )
        val state = engine.getState()
        assertEquals("0.3", state.primaryDisplay)
    }

    @Test
    fun testLargeMultiplication_NineNineNineTimesNineNineNine() {
        // 999 * 999 = 998,001
        pressKeys(
            CalculatorKey.Digit("9"),
            CalculatorKey.Digit("9"),
            CalculatorKey.Digit("9"),
            CalculatorKey.Multiply,
            CalculatorKey.Digit("9"),
            CalculatorKey.Digit("9"),
            CalculatorKey.Digit("9"),
            CalculatorKey.Equals
        )
        val state = engine.getState()
        assertEquals("998,001", state.primaryDisplay)
    }

    @Test
    fun testDivisionByZero() {
        // 10 / 0 = Error
        pressKeys(
            CalculatorKey.Digit("1"),
            CalculatorKey.Digit("0"),
            CalculatorKey.Divide,
            CalculatorKey.Digit("0"),
            CalculatorKey.Equals
        )
        val state = engine.getState()
        assertTrue(state.isError)
        assertEquals("لا يمكن القسمة على صفر", state.primaryDisplay)
    }

    @Test
    fun testZeroMultiplication_ZeroTimesHundred() {
        // 0 * 100 = 0
        pressKeys(
            CalculatorKey.Digit("0"),
            CalculatorKey.Multiply,
            CalculatorKey.Digit("1"),
            CalculatorKey.DoubleZero,
            CalculatorKey.Equals
        )
        val state = engine.getState()
        assertEquals("0", state.primaryDisplay)
    }

    @Test
    fun testCustomZeros_TripleZeroAndDoubleZero() {
        // 125 -> 000 = 125,000
        pressKeys(
            CalculatorKey.Digit("1"),
            CalculatorKey.Digit("2"),
            CalculatorKey.Digit("5"),
            CalculatorKey.TripleZero
        )
        val state1 = engine.getState()
        assertEquals("125,000", state1.primaryDisplay)

        // Add 00 -> 12,500,000
        pressKeys(CalculatorKey.DoubleZero)
        val state2 = engine.getState()
        assertEquals("12,500,000", state2.primaryDisplay)
    }

    @Test
    fun testConsecutiveOperations() {
        // 125 + 25 - 50 = 100
        pressKeys(
            CalculatorKey.Digit("1"),
            CalculatorKey.Digit("2"),
            CalculatorKey.Digit("5"),
            CalculatorKey.Add,
            CalculatorKey.Digit("2"),
            CalculatorKey.Digit("5"),
            CalculatorKey.Subtract,
            CalculatorKey.Digit("5"),
            CalculatorKey.Digit("0"),
            CalculatorKey.Equals
        )
        val state = engine.getState()
        assertEquals("100", state.primaryDisplay)
    }

    @Test
    fun testRepeatedEquals() {
        // 5 + 3 = 8, then = 11, then = 14
        pressKeys(
            CalculatorKey.Digit("5"),
            CalculatorKey.Add,
            CalculatorKey.Digit("3"),
            CalculatorKey.Equals
        )
        assertEquals("8", engine.getState().primaryDisplay)

        pressKeys(CalculatorKey.Equals)
        assertEquals("11", engine.getState().primaryDisplay)

        pressKeys(CalculatorKey.Equals)
        assertEquals("14", engine.getState().primaryDisplay)
    }

    @Test
    fun testPlusMinusToggle() {
        // 125 -> ± -> -125 -> ± -> 125
        pressKeys(
            CalculatorKey.Digit("1"),
            CalculatorKey.Digit("2"),
            CalculatorKey.Digit("5"),
            CalculatorKey.PlusMinus
        )
        assertEquals("-125", engine.getState().primaryDisplay)

        pressKeys(CalculatorKey.PlusMinus)
        assertEquals("125", engine.getState().primaryDisplay)
    }

    @Test
    fun testBackspace() {
        // 1234 -> ⌫ -> 123 -> ⌫ -> 12
        pressKeys(
            CalculatorKey.Digit("1"),
            CalculatorKey.Digit("2"),
            CalculatorKey.Digit("3"),
            CalculatorKey.Digit("4"),
            CalculatorKey.Backspace
        )
        assertEquals("123", engine.getState().primaryDisplay)

        pressKeys(CalculatorKey.Backspace)
        assertEquals("12", engine.getState().primaryDisplay)
    }

    @Test
    fun testAllClear() {
        pressKeys(
            CalculatorKey.Digit("1"),
            CalculatorKey.Digit("2"),
            CalculatorKey.Add,
            CalculatorKey.Digit("3"),
            CalculatorKey.AllClear
        )
        val state = engine.getState()
        assertEquals("0", state.primaryDisplay)
        assertEquals("", state.secondaryDisplay)
        assertFalse(state.isError)
    }
}
