package com.example.nexusbusiness.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nexusbusiness.ui.theme.CyanSecondary

@Composable
fun NeonButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth(0.8f)
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(2.dp, CyanSecondary),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = CyanSecondary,
            containerColor = CyanSecondary.copy(alpha = 0.05f)
        ),
        contentPadding = PaddingValues(16.dp)
    ) {
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
}
