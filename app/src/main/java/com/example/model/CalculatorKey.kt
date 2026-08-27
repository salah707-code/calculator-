package com.example.model

sealed class CalculatorKey(val symbol: String) {
    // Digits
    data class Digit(val value: String) : CalculatorKey(value)
    
    // Special zeroes
    data object DoubleZero : CalculatorKey("00")
    data object TripleZero : CalculatorKey("000")
    
    // Dot
    data object DecimalDot : CalculatorKey(".")
    
    // Operations
    data object Add : CalculatorKey("+")
    data object Subtract : CalculatorKey("−")
    data object Multiply : CalculatorKey("×")
    data object Divide : CalculatorKey("÷")
    data object Equals : CalculatorKey("=")
    data object Percent : CalculatorKey("%")
    data object PlusMinus : CalculatorKey("±")
    
    // Utilities
    data object AllClear : CalculatorKey("AC")
    data object Backspace : CalculatorKey("⌫")
}

enum class KeyType {
    NUMBER,
    OPERATOR,
    UTILITY,
    EQUALS
}
