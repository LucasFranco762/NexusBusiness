package com.example.nexusbusiness.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nexusbusiness.ui.theme.CyanSecondary
import com.example.nexusbusiness.ui.theme.PurplePrimary

@Composable
fun NeonButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    glassEffect: Boolean = false
) {
    val buttonModifier = modifier
        .fillMaxWidth(0.8f)
        .padding(vertical = 8.dp)
    val buttonShape = RoundedCornerShape(16.dp)

    if (glassEffect) {
        Surface(
            onClick = onClick,
            modifier = buttonModifier,
            shape = buttonShape,
            color = Color.Transparent,
            contentColor = CyanSecondary,
            border = BorderStroke(
                1.dp,
                Brush.horizontalGradient(
                    listOf(
                        Color.White.copy(alpha = 0.42f),
                        CyanSecondary.copy(alpha = 0.85f),
                        PurplePrimary.copy(alpha = 0.75f)
                    )
                )
            ),
            shadowElevation = 8.dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.White.copy(alpha = 0.16f),
                                Color(0xFF1E293B).copy(alpha = 0.72f),
                                PurplePrimary.copy(alpha = 0.18f)
                            )
                        )
                    )
                    .padding(16.dp)
            ) {
                NeonButtonContent(text = text, icon = icon)
            }
        }
    } else {
        OutlinedButton(
            onClick = onClick,
            modifier = buttonModifier,
            shape = buttonShape,
            border = BorderStroke(2.dp, CyanSecondary),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = CyanSecondary,
                containerColor = CyanSecondary.copy(alpha = 0.05f)
            ),
            contentPadding = PaddingValues(16.dp)
        ) {
            NeonButtonContent(text = text, icon = icon)
        }
    }
}

@Composable
private fun NeonButtonContent(text: String, icon: ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text.uppercase(),
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            fontSize = 14.sp
        )
    }
}
