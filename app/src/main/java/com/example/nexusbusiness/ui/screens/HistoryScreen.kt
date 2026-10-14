package com.example.nexusbusiness.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nexusbusiness.ui.components.NeonButton
import com.example.nexusbusiness.ui.theme.CyanSecondary
import com.example.nexusbusiness.ui.theme.PurplePrimary

@Composable
fun HistoryScreen(
    onNavigateToFluxoCaixa: () -> Unit,
    onNavigateToHistoricoVendas: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 40.dp)
        ) {
            Icon(
                Icons.Default.ListAlt,
                contentDescription = null,
                tint = Color(0xFFFB923C),
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Gestão Financeira",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                style = LocalTextStyle.current.copy(
                    brush = Brush.horizontalGradient(
                        listOf(PurplePrimary, CyanSecondary)
                    )
                )
            )
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            NeonButton(
                text = "Fluxo de Caixa",
                icon = Icons.Default.AccountBalanceWallet,
                onClick = onNavigateToFluxoCaixa
            )
            NeonButton(
                text = "Histórico de Vendas",
                icon = Icons.Default.History,
                onClick = onNavigateToHistoricoVendas
            )
        }
    }
}
