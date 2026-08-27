package com.example.ui.components

import android.view.View
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CalculatorKey
import com.example.model.KeyType
import com.example.ui.theme.CleanBlue
import com.example.ui.theme.CleanRed
import com.example.ui.theme.CleanRedDark
import com.example.ui.theme.CleanRedSoft
import com.example.ui.theme.CleanRedSoftDark
import com.example.ui.theme.DarkNumKeyBg
import com.example.ui.theme.DarkNumKeyBorder
import com.example.ui.theme.DarkNumKeyText
import com.example.ui.theme.DarkOperatorKeyActiveBg
import com.example.ui.theme.DarkOperatorKeyActiveText
import com.example.ui.theme.DarkOperatorKeyBg
import com.example.ui.theme.DarkOperatorKeyText
import com.example.ui.theme.DarkUtilityKeyBg
import com.example.ui.theme.DarkUtilityKeyText
import com.example.ui.theme.LightNumKeyBg
import com.example.ui.theme.LightNumKeyBorder
import com.example.ui.theme.LightNumKeyText
import com.example.ui.theme.LightOperatorKeyActiveBg
import com.example.ui.theme.LightOperatorKeyActiveText
import com.example.ui.theme.LightOperatorKeyBg
import com.example.ui.theme.LightOperatorKeyText
import com.example.ui.theme.LightUtilityKeyBg
import com.example.ui.theme.LightUtilityKeyText

@Composable
fun CalculatorKeyButton(
    key: CalculatorKey,
    keyType: KeyType,
    onClick: (View) -> Unit,
    modifier: Modifier = Modifier,
    isActiveOperator: Boolean = false,
    isDarkTheme: Boolean = isSystemInDarkTheme()
) {
    val view = LocalView.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1.0f,
        label = "key_scale"
    )

    val isAllClear = key is CalculatorKey.AllClear

    val containerColor: Color = when {
        isAllClear -> if (isDarkTheme) CleanRedSoftDark else CleanRedSoft
        keyType == KeyType.OPERATOR -> if (isActiveOperator) {
            if (isDarkTheme) DarkOperatorKeyActiveBg else LightOperatorKeyActiveBg
        } else {
            if (isDarkTheme) DarkOperatorKeyBg else LightOperatorKeyBg
        }
        keyType == KeyType.EQUALS -> CleanBlue
        keyType == KeyType.UTILITY -> if (isDarkTheme) DarkUtilityKeyBg else LightUtilityKeyBg
        else -> if (isDarkTheme) DarkNumKeyBg else LightNumKeyBg
    }

    val contentColor: Color = when {
        isAllClear -> if (isDarkTheme) CleanRedDark else CleanRed
        keyType == KeyType.OPERATOR -> if (isActiveOperator) {
            if (isDarkTheme) DarkOperatorKeyActiveText else LightOperatorKeyActiveText
        } else {
            if (isDarkTheme) DarkOperatorKeyText else LightOperatorKeyText
        }
        keyType == KeyType.EQUALS -> Color.White
        keyType == KeyType.UTILITY -> if (isDarkTheme) DarkUtilityKeyText else LightUtilityKeyText
        else -> if (isDarkTheme) DarkNumKeyText else LightNumKeyText
    }

    val border: BorderStroke? = when {
        keyType == KeyType.NUMBER -> BorderStroke(
            1.dp,
            if (isDarkTheme) DarkNumKeyBorder else LightNumKeyBorder
        )
        else -> null
    }

    val elevation = when (keyType) {
        KeyType.EQUALS -> 4.dp
        KeyType.NUMBER -> if (isDarkTheme) 0.dp else 1.dp
        else -> 0.dp
    }

    val testTag = when (key) {
        is CalculatorKey.Digit -> "btn_${key.value}"
        is CalculatorKey.DoubleZero -> "btn_00"
        is CalculatorKey.TripleZero -> "btn_000"
        is CalculatorKey.DecimalDot -> "btn_dot"
        is CalculatorKey.Add -> "btn_add"
        is CalculatorKey.Subtract -> "btn_sub"
        is CalculatorKey.Multiply -> "btn_mul"
        is CalculatorKey.Divide -> "btn_div"
        is CalculatorKey.Equals -> "btn_equals"
        is CalculatorKey.Percent -> "btn_percent"
        is CalculatorKey.PlusMinus -> "btn_pm"
        is CalculatorKey.AllClear -> "btn_ac"
        is CalculatorKey.Backspace -> "btn_backspace"
    }

    val fontSize = when {
        key.symbol.length >= 3 -> 18.sp
        key.symbol.length == 2 -> 20.sp
        keyType == KeyType.OPERATOR || keyType == KeyType.EQUALS -> 26.sp
        else -> 24.sp
    }

    Card(
        onClick = { onClick(view) },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        border = border,
        elevation = CardDefaults.cardElevation(
            defaultElevation = elevation,
            pressedElevation = 0.dp
        ),
        interactionSource = interactionSource,
        modifier = modifier
            .scale(scale)
            .testTag(testTag)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = key.symbol,
                fontSize = fontSize,
                fontWeight = if (keyType == KeyType.NUMBER) FontWeight.Medium else FontWeight.Bold,
                color = contentColor
            )
        }
    }
}
