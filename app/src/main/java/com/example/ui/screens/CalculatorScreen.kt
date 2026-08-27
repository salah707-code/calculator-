package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SettingsBrightness
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.AppTheme
import com.example.model.CalculatorKey
import com.example.model.CalculatorState
import com.example.model.KeyType
import com.example.ui.components.AutoResizeText
import com.example.ui.components.CalculatorKeyButton
import com.example.ui.theme.CleanBlue

@Composable
fun CalculatorScreen(
    state: CalculatorState,
    currentTheme: AppTheme,
    onKeyClick: (CalculatorKey) -> Unit,
    onOpenHistory: () -> Unit,
    onOpenSettings: () -> Unit,
    onToggleTheme: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isDark = when (currentTheme) {
        AppTheme.SYSTEM -> isSystemInDarkTheme()
        AppTheme.LIGHT -> false
        AppTheme.DARK -> true
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(top = 8.dp)
                .widthIn(max = 600.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Navigation & Action Bar
            TopActionBar(
                currentTheme = currentTheme,
                onOpenHistory = onOpenHistory,
                onOpenSettings = onOpenSettings,
                onToggleTheme = onToggleTheme,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            )

            // Display Screen Area (Secondary Expression + Primary Result)
            DisplayScreen(
                expression = state.secondaryDisplay,
                displayValue = state.primaryDisplay,
                isError = state.isError,
                onCopy = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("Calculator Value", state.primaryDisplay)
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(
                        context,
                        context.getString(R.string.copied_to_clipboard),
                        Toast.LENGTH_SHORT
                    ).show()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 24.dp, vertical = 12.dp)
            )

            // Keypad Card Area with elevated top-rounded clean container
            Card(
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = if (isDark) 0.dp else 4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                KeypadGrid(
                    activeOperator = state.activeOperator,
                    onKeyClick = onKeyClick,
                    isDarkTheme = isDark,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 20.dp)
                )
            }
        }
    }
}

@Composable
fun TopActionBar(
    currentTheme: AppTheme,
    onOpenHistory: () -> Unit,
    onOpenSettings: () -> Unit,
    onToggleTheme: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.height(52.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // App Brand & Logo Pill
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(CleanBlue),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "±",
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = stringResource(R.string.app_name),
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        // Action Pill Buttons
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Theme toggle
            IconButton(
                onClick = onToggleTheme,
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .testTag("btn_toggle_theme")
            ) {
                val icon = when (currentTheme) {
                    AppTheme.SYSTEM -> Icons.Default.SettingsBrightness
                    AppTheme.LIGHT -> Icons.Default.LightMode
                    AppTheme.DARK -> Icons.Default.DarkMode
                }
                Icon(
                    imageVector = icon,
                    contentDescription = stringResource(R.string.settings_appearance_section),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }

            // History Button
            IconButton(
                onClick = onOpenHistory,
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .testTag("btn_open_history")
            ) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = stringResource(R.string.history_title),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }

            // Settings Button
            IconButton(
                onClick = onOpenSettings,
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .testTag("btn_open_settings")
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = stringResource(R.string.settings_title),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun DisplayScreen(
    expression: String,
    displayValue: String,
    isError: Boolean,
    onCopy: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onCopy
            )
            .testTag("calculator_display"),
        contentAlignment = Alignment.BottomEnd
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.End
        ) {
            // Secondary Expression Display
            AnimatedContent(
                targetState = expression,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "expression_anim"
            ) { targetExpr ->
                Text(
                    text = targetExpr.ifEmpty { " " },
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Primary Result Display (Clean Auto-resizing Display)
            AutoResizeText(
                text = displayValue,
                maxFontSize = 58.sp,
                minFontSize = 24.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun KeypadGrid(
    activeOperator: String?,
    onKeyClick: (CalculatorKey) -> Unit,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Row 1: AC | ⌫ | % | ÷
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            CalculatorKeyButton(
                key = CalculatorKey.AllClear,
                keyType = KeyType.UTILITY,
                onClick = { onKeyClick(CalculatorKey.AllClear) },
                isDarkTheme = isDarkTheme,
                modifier = Modifier.weight(1f).height(62.dp)
            )
            CalculatorKeyButton(
                key = CalculatorKey.Backspace,
                keyType = KeyType.UTILITY,
                onClick = { onKeyClick(CalculatorKey.Backspace) },
                isDarkTheme = isDarkTheme,
                modifier = Modifier.weight(1f).height(62.dp)
            )
            CalculatorKeyButton(
                key = CalculatorKey.Percent,
                keyType = KeyType.UTILITY,
                onClick = { onKeyClick(CalculatorKey.Percent) },
                isDarkTheme = isDarkTheme,
                modifier = Modifier.weight(1f).height(62.dp)
            )
            CalculatorKeyButton(
                key = CalculatorKey.Divide,
                keyType = KeyType.OPERATOR,
                isActiveOperator = activeOperator == "÷",
                onClick = { onKeyClick(CalculatorKey.Divide) },
                isDarkTheme = isDarkTheme,
                modifier = Modifier.weight(1f).height(62.dp)
            )
        }

        // Row 2: 7 | 8 | 9 | ×
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            CalculatorKeyButton(
                key = CalculatorKey.Digit("7"),
                keyType = KeyType.NUMBER,
                onClick = { onKeyClick(CalculatorKey.Digit("7")) },
                isDarkTheme = isDarkTheme,
                modifier = Modifier.weight(1f).height(62.dp)
            )
            CalculatorKeyButton(
                key = CalculatorKey.Digit("8"),
                keyType = KeyType.NUMBER,
                onClick = { onKeyClick(CalculatorKey.Digit("8")) },
                isDarkTheme = isDarkTheme,
                modifier = Modifier.weight(1f).height(62.dp)
            )
            CalculatorKeyButton(
                key = CalculatorKey.Digit("9"),
                keyType = KeyType.NUMBER,
                onClick = { onKeyClick(CalculatorKey.Digit("9")) },
                isDarkTheme = isDarkTheme,
                modifier = Modifier.weight(1f).height(62.dp)
            )
            CalculatorKeyButton(
                key = CalculatorKey.Multiply,
                keyType = KeyType.OPERATOR,
                isActiveOperator = activeOperator == "×",
                onClick = { onKeyClick(CalculatorKey.Multiply) },
                isDarkTheme = isDarkTheme,
                modifier = Modifier.weight(1f).height(62.dp)
            )
        }

        // Row 3: 4 | 5 | 6 | −
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            CalculatorKeyButton(
                key = CalculatorKey.Digit("4"),
                keyType = KeyType.NUMBER,
                onClick = { onKeyClick(CalculatorKey.Digit("4")) },
                isDarkTheme = isDarkTheme,
                modifier = Modifier.weight(1f).height(62.dp)
            )
            CalculatorKeyButton(
                key = CalculatorKey.Digit("5"),
                keyType = KeyType.NUMBER,
                onClick = { onKeyClick(CalculatorKey.Digit("5")) },
                isDarkTheme = isDarkTheme,
                modifier = Modifier.weight(1f).height(62.dp)
            )
            CalculatorKeyButton(
                key = CalculatorKey.Digit("6"),
                keyType = KeyType.NUMBER,
                onClick = { onKeyClick(CalculatorKey.Digit("6")) },
                isDarkTheme = isDarkTheme,
                modifier = Modifier.weight(1f).height(62.dp)
            )
            CalculatorKeyButton(
                key = CalculatorKey.Subtract,
                keyType = KeyType.OPERATOR,
                isActiveOperator = activeOperator == "−",
                onClick = { onKeyClick(CalculatorKey.Subtract) },
                isDarkTheme = isDarkTheme,
                modifier = Modifier.weight(1f).height(62.dp)
            )
        }

        // Row 4: 1 | 2 | 3 | +
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            CalculatorKeyButton(
                key = CalculatorKey.Digit("1"),
                keyType = KeyType.NUMBER,
                onClick = { onKeyClick(CalculatorKey.Digit("1")) },
                isDarkTheme = isDarkTheme,
                modifier = Modifier.weight(1f).height(62.dp)
            )
            CalculatorKeyButton(
                key = CalculatorKey.Digit("2"),
                keyType = KeyType.NUMBER,
                onClick = { onKeyClick(CalculatorKey.Digit("2")) },
                isDarkTheme = isDarkTheme,
                modifier = Modifier.weight(1f).height(62.dp)
            )
            CalculatorKeyButton(
                key = CalculatorKey.Digit("3"),
                keyType = KeyType.NUMBER,
                onClick = { onKeyClick(CalculatorKey.Digit("3")) },
                isDarkTheme = isDarkTheme,
                modifier = Modifier.weight(1f).height(62.dp)
            )
            CalculatorKeyButton(
                key = CalculatorKey.Add,
                keyType = KeyType.OPERATOR,
                isActiveOperator = activeOperator == "+",
                onClick = { onKeyClick(CalculatorKey.Add) },
                isDarkTheme = isDarkTheme,
                modifier = Modifier.weight(1f).height(62.dp)
            )
        }

        // Row 5: 00 | 0 | 000 | ±
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            CalculatorKeyButton(
                key = CalculatorKey.DoubleZero,
                keyType = KeyType.NUMBER,
                onClick = { onKeyClick(CalculatorKey.DoubleZero) },
                isDarkTheme = isDarkTheme,
                modifier = Modifier.weight(1f).height(62.dp)
            )
            CalculatorKeyButton(
                key = CalculatorKey.Digit("0"),
                keyType = KeyType.NUMBER,
                onClick = { onKeyClick(CalculatorKey.Digit("0")) },
                isDarkTheme = isDarkTheme,
                modifier = Modifier.weight(1f).height(62.dp)
            )
            CalculatorKeyButton(
                key = CalculatorKey.TripleZero,
                keyType = KeyType.NUMBER,
                onClick = { onKeyClick(CalculatorKey.TripleZero) },
                isDarkTheme = isDarkTheme,
                modifier = Modifier.weight(1f).height(62.dp)
            )
            CalculatorKeyButton(
                key = CalculatorKey.PlusMinus,
                keyType = KeyType.UTILITY,
                onClick = { onKeyClick(CalculatorKey.PlusMinus) },
                isDarkTheme = isDarkTheme,
                modifier = Modifier.weight(1f).height(62.dp)
            )
        }

        // Row 6: . (weight 1) | = (weight 3)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            CalculatorKeyButton(
                key = CalculatorKey.DecimalDot,
                keyType = KeyType.NUMBER,
                onClick = { onKeyClick(CalculatorKey.DecimalDot) },
                isDarkTheme = isDarkTheme,
                modifier = Modifier.weight(1f).height(62.dp)
            )
            CalculatorKeyButton(
                key = CalculatorKey.Equals,
                keyType = KeyType.EQUALS,
                onClick = { onKeyClick(CalculatorKey.Equals) },
                isDarkTheme = isDarkTheme,
                modifier = Modifier.weight(3f).height(62.dp)
            )
        }
    }
}
