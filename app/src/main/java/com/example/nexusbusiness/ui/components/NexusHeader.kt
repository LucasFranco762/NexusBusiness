package com.example.nexusbusiness.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nexusbusiness.ui.theme.CardBackground
import com.example.nexusbusiness.ui.theme.CyanSecondary
import com.example.nexusbusiness.ui.theme.DarkBackground
import com.example.nexusbusiness.ui.theme.PurplePrimary
import com.example.nexusbusiness.ui.theme.StatusOffline
import com.example.nexusbusiness.ui.theme.StatusOnline

@Composable
fun NexusHeader(isOnline: Boolean, activeCity: String?) {
    Surface(
        color = DarkBackground.copy(alpha = 0.9f),
        modifier = Modifier.statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.ElectricBolt,
                        contentDescription = null,
                        tint = Color(0xFF818CF8),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "NexusBusiness",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        style = LocalTextStyle.current.copy(
                            brush = Brush.horizontalGradient(
                                listOf(PurplePrimary, CyanSecondary)
                            )
                        )
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (activeCity != null) {
                        Surface(
                            color = Color.Black,
                            shape = RoundedCornerShape(4.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, PurplePrimary),
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            Text(
                                text = activeCity.uppercase(),
                                color = PurplePrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Text(
                        text = "V 1.0",
                        color = CyanSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(start = 10.dp)
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 32.dp, top = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .background(
                            if (isOnline) StatusOnline else StatusOffline,
                            shape = RoundedCornerShape(50)
                        )
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isOnline) "STATUS ON LINE" else "STATUS OFF LINE",
                    color = if (isOnline) StatusOnline else StatusOffline,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            HorizontalDivider(
                color = CardBackground,
                thickness = 1.dp,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
}
