package com.example.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun Button3D(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    backgroundColor: Color = Color(0xFF3B82F6),
    shadowColor: Color = Color(0xFF1D4ED8),
    textColor: Color = Color.White
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // 3D tactile offset on press
    val offsetVal by animateDpAsState(
        targetValue = if (isPressed && enabled) 4.dp else 0.dp,
        label = "PressedOffset"
    )

    val finalBgColor = if (enabled) backgroundColor else Color(0xFF64748B)
    val finalShadowColor = if (enabled) shadowColor else Color(0xFF475569)

    Box(
        modifier = modifier
            .testTag("button_3d_container")
            .padding(bottom = 6.dp) // Leave safety space for shadow
    ) {
        // Shadow base shape
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(y = 6.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(finalShadowColor)
        )

        // Tactile button top
        Box(
            modifier = Modifier
                .offset(y = offsetVal)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    if (enabled) {
                        Brush.verticalGradient(
                            colors = listOf(finalBgColor, finalBgColor.copy(alpha = 0.85f))
                        )
                    } else {
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFF64748B), Color(0xFF475569))
                        )
                    }
                )
                .clickable(
                    enabled = enabled,
                    interactionSource = interactionSource,
                    indication = null, // No standard flat ripple to keep 3D realism
                    onClick = onClick
                )
                .padding(horizontal = 32.dp, vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text.uppercase(),
                color = if (enabled) textColor else Color(0xFFCBD5E1),
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
            )
        }
    }
}
