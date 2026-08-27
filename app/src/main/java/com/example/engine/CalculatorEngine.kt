package com.example.engine

import com.example.model.CalculationHistory
import com.example.model.CalculatorKey
import com.example.model.CalculatorState
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

class CalculatorEngine(
    private var useThousandsSeparator: Boolean = true,
    private val divideByZeroText: String = "لا يمكن القسمة على صفر",
    private val invalidText: String = "غير صالح"
) {
    private val mathContext = MathContext.DECIMAL128
    
    private var currentInput: String = "0"
    private var storedOperand: BigDecimal? = null
    private var currentOperator: String? = null
    private var secondaryDisplay: String = ""
    private var isNewInput: Boolean = true
    private var isError: Boolean = false
    private var errorMessage: String? = null
    
    // For repeating '=' functionality
    private var lastOperator: String? = null
    private var lastSecondOperand: BigDecimal? = null
    
    // Last successful calculated history item
    private var lastHistoryItem: CalculationHistory? = null

    fun setThousandsSeparator(enabled: Boolean) {
        useThousandsSeparator = enabled
    }

    fun getState(): CalculatorState {
        return CalculatorState(
            primaryDisplay = if (isError) (errorMessage ?: invalidText) else formatForDisplay(currentInput),
            secondaryDisplay = secondaryDisplay,
            activeOperator = currentOperator,
            isError = isError,
            errorMessage = errorMessage,
            lastCalculation = lastHistoryItem
        )
    }

    fun onKeyPress(key: CalculatorKey): CalculatorState {
        lastHistoryItem = null // reset per-event
        when (key) {
            is CalculatorKey.Digit -> handleDigit(key.value)
            is CalculatorKey.DoubleZero -> handleMultipleZeroes("00")
            is CalculatorKey.TripleZero -> handleMultipleZeroes("000")
            is CalculatorKey.DecimalDot -> handleDecimalDot()
            is CalculatorKey.Add -> handleOperator("+")
            is CalculatorKey.Subtract -> handleOperator("−")
            is CalculatorKey.Multiply -> handleOperator("×")
            is CalculatorKey.Divide -> handleOperator("÷")
            is CalculatorKey.Equals -> handleEquals()
            is CalculatorKey.Percent -> handlePercent()
            is CalculatorKey.PlusMinus -> handlePlusMinus()
            is CalculatorKey.AllClear -> handleAllClear()
            is CalculatorKey.Backspace -> handleBackspace()
        }
        return getState()
    }

    fun resetWithInitialValue(value: String) {
        handleAllClear()
        currentInput = sanitizeNumberString(value)
        isNewInput = false
    }

    private fun handleDigit(digit: String) {
        if (isError) {
            handleAllClear()
        }

        if (isNewInput) {
            currentInput = digit
            isNewInput = false
        } else {
            if (currentInput == "0") {
                currentInput = digit
            } else if (currentInput == "-0") {
                currentInput = "-$digit"
            } else if (currentInput.replace("-", "").replace(".", "").length < 16) {
                currentInput += digit
            }
        }
    }

    private fun handleMultipleZeroes(zeros: String) {
        if (isError) {
            handleAllClear()
        }

        if (isNewInput) {
            currentInput = "0"
            isNewInput = false
        } else {
            if (currentInput == "0" || currentInput == "-0") {
                // Keep as 0
                return
            }
            if (currentInput.replace("-", "").replace(".", "").length + zeros.length <= 16) {
                currentInput += zeros
            }
        }
    }

    private fun handleDecimalDot() {
        if (isError) {
            handleAllClear()
        }

        if (isNewInput) {
            currentInput = "0."
            isNewInput = false
        } else {
            if (!currentInput.contains(".")) {
                currentInput = if (currentInput.isEmpty()) "0." else "$currentInput."
            }
        }
    }

    private fun handleOperator(op: String) {
        if (isError) {
            return
        }

        val currentVal = parseToBigDecimal(currentInput)

        if (storedOperand != null && currentOperator != null && !isNewInput) {
            // Consecutive calculation (e.g. 5 + 5 + 5)
            val result = executeOperation(storedOperand!!, currentVal, currentOperator!!)
            if (result == null) {
                // Error (e.g. divide by zero)
                return
            }
            storedOperand = result
            currentInput = formatBigDecimalToString(result)
            secondaryDisplay = "${formatBigDecimalForDisplay(result)} $op"
        } else {
            storedOperand = currentVal
            secondaryDisplay = "${formatBigDecimalForDisplay(currentVal)} $op"
        }

        currentOperator = op
        isNewInput = true
        lastOperator = null
        lastSecondOperand = null
    }

    private fun handleEquals() {
        if (isError) return

        if (currentOperator != null) {
            val op1 = storedOperand ?: BigDecimal.ZERO
            val op2 = if (!isNewInput) parseToBigDecimal(currentInput) else op1
            val op = currentOperator!!

            val result = executeOperation(op1, op2, op)
            if (result == null) return

            val exprDisplay = "${formatBigDecimalForDisplay(op1)} $op ${formatBigDecimalForDisplay(op2)}"
            val resDisplay = formatBigDecimalForDisplay(result)

            secondaryDisplay = "$exprDisplay ="
            currentInput = formatBigDecimalToString(result)
            storedOperand = result
            lastOperator = op
            lastSecondOperand = op2
            currentOperator = null
            isNewInput = true

            // Record history
            lastHistoryItem = CalculationHistory(
                expression = exprDisplay,
                result = resDisplay,
                timestamp = System.currentTimeMillis()
            )
        } else if (lastOperator != null && lastSecondOperand != null) {
            // Repeated '=' press!
            val op1 = parseToBigDecimal(currentInput)
            val op2 = lastSecondOperand!!
            val op = lastOperator!!

            val result = executeOperation(op1, op2, op)
            if (result == null) return

            val exprDisplay = "${formatBigDecimalForDisplay(op1)} $op ${formatBigDecimalForDisplay(op2)}"
            val resDisplay = formatBigDecimalForDisplay(result)

            secondaryDisplay = "$exprDisplay ="
            currentInput = formatBigDecimalToString(result)
            storedOperand = result
            isNewInput = true

            lastHistoryItem = CalculationHistory(
                expression = exprDisplay,
                result = resDisplay,
                timestamp = System.currentTimeMillis()
            )
        }
    }

    private fun handlePercent() {
        if (isError) return

        val currentVal = parseToBigDecimal(currentInput)
        val percentFraction = currentVal.divide(BigDecimal("100"), 10, RoundingMode.HALF_UP).stripTrailingZeros()

        if (storedOperand != null && (currentOperator == "+" || currentOperator == "−")) {
            // Percent of stored operand, e.g., 200 + 10% = 200 + (200 * 0.10)
            val percentVal = storedOperand!!.multiply(percentFraction, mathContext).stripTrailingZeros()
            currentInput = formatBigDecimalToString(percentVal)
        } else {
            // Direct percentage (e.g. 50% = 0.5)
            currentInput = formatBigDecimalToString(percentFraction)
        }
        isNewInput = false
    }

    private fun handlePlusMinus() {
        if (isError) return

        if (currentInput == "0" || currentInput.isEmpty()) {
            return
        }

        currentInput = if (currentInput.startsWith("-")) {
            currentInput.substring(1)
        } else {
            "-$currentInput"
        }
    }

    private fun handleBackspace() {
        if (isError) {
            handleAllClear()
            return
        }

        if (isNewInput) {
            // If just finished an operation, backspace does nothing or clears
            return
        }

        if (currentInput.length > 1) {
            currentInput = currentInput.dropLast(1)
            if (currentInput == "-" || currentInput.isEmpty()) {
                currentInput = "0"
            }
        } else {
            currentInput = "0"
        }
    }

    private fun handleAllClear() {
        currentInput = "0"
        storedOperand = null
        currentOperator = null
        secondaryDisplay = ""
        isNewInput = true
        isError = false
        errorMessage = null
        lastOperator = null
        lastSecondOperand = null
        lastHistoryItem = null
    }

    private fun executeOperation(op1: BigDecimal, op2: BigDecimal, op: String): BigDecimal? {
        return try {
            val res = when (op) {
                "+" -> op1.add(op2, mathContext)
                "−", "-" -> op1.subtract(op2, mathContext)
                "×", "*" -> op1.multiply(op2, mathContext)
                "÷", "/" -> {
                    if (op2.compareTo(BigDecimal.ZERO) == 0) {
                        isError = true
                        errorMessage = divideByZeroText
                        return null
                    }
                    op1.divide(op2, 12, RoundingMode.HALF_UP)
                }
                else -> op2
            }
            res.stripTrailingZeros()
        } catch (e: ArithmeticException) {
            isError = true
            errorMessage = invalidText
            null
        } catch (e: Exception) {
            isError = true
            errorMessage = invalidText
            null
        }
    }

    private fun parseToBigDecimal(str: String): BigDecimal {
        return try {
            val sanitized = sanitizeNumberString(str)
            if (sanitized.isEmpty() || sanitized == "-" || sanitized == ".") {
                BigDecimal.ZERO
            } else {
                BigDecimal(sanitized)
            }
        } catch (e: Exception) {
            BigDecimal.ZERO
        }
    }

    private fun sanitizeNumberString(str: String): String {
        return str.replace(",", "")
            .replace("،", "")
            .replace(" ", "")
            .replace("−", "-")
    }

    private fun formatBigDecimalToString(bd: BigDecimal): String {
        val plain = bd.stripTrailingZeros().toPlainString()
        return if (plain.length > 20 || (plain.contains(".") && plain.indexOf(".") > 15)) {
            bd.toString()
        } else {
            plain
        }
    }

    fun formatForDisplay(rawString: String): String {
        if (!useThousandsSeparator) return rawString
        if (rawString.isEmpty() || rawString == "-" || rawString == ".") return rawString

        return try {
            val isNegative = rawString.startsWith("-")
            val unsigned = if (isNegative) rawString.substring(1) else rawString

            val parts = unsigned.split(".")
            val integerPart = parts[0]
            val decimalPart = if (parts.size > 1) parts[1] else null
            val hasTrailingDot = unsigned.endsWith(".")

            val formattedInteger = if (integerPart.isNotEmpty()) {
                val symbols = DecimalFormatSymbols(Locale.US).apply {
                    groupingSeparator = ','
                }
                val formatter = DecimalFormat("#,###", symbols)
                formatter.format(BigDecimal(integerPart))
            } else {
                "0"
            }

            val result = buildString {
                if (isNegative) append("-")
                append(formattedInteger)
                if (hasTrailingDot) {
                    append(".")
                } else if (decimalPart != null) {
                    append(".")
                    append(decimalPart)
                }
            }
            result
        } catch (e: Exception) {
            rawString
        }
    }

    fun formatBigDecimalForDisplay(bd: BigDecimal): String {
        val str = formatBigDecimalToString(bd)
        return formatForDisplay(str)
    }
}
