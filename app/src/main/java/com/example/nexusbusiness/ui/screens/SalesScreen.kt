package com.example.nexusbusiness.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.ViewList
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
fun SalesScreen(
    onNavigateToRealizarVenda: () -> Unit,
    onNavigateToPedidos: () -> Unit,
    onNavigateToListaPedidos: () -> Unit
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
                Icons.Default.MonetizationOn,
                contentDescription = null,
                tint = Color(0xFFFB923C), // orange-400
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Operações Comerciais",
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
                text = "Realizar Venda",
                icon = Icons.Default.AddShoppingCart,
                onClick = onNavigateToRealizarVenda,
                glassEffect = true
            )
            NeonButton(
                text = "Pedidos",
                icon = Icons.Default.ListAlt,
                onClick = onNavigateToPedidos,
                glassEffect = true
            )
            NeonButton(
                text = "Lista de Pedidos",
                icon = Icons.Default.ViewList,
                onClick = onNavigateToListaPedidos,
                glassEffect = true
            )
        }
    }
}
