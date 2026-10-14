package com.example.nexusbusiness.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.nexusbusiness.ui.theme.*
import com.example.nexusbusiness.util.CurrencyUtils
import com.example.nexusbusiness.viewmodel.MainViewModel

@Composable
fun CashFlowScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val sales by viewModel.sales.collectAsState()
    val routeInvestment by viewModel.routeInvestment.collectAsState()
    
    var showCloseConfirm by remember { mutableStateOf(false) }

    // OTIMIZAÇÃO: Memoriza os cálculos para não travar a UI ao digitar no modal
    val totalVendidoVal = remember(sales) {
        sales.sumOf { CurrencyUtils.parse(it.totalValue) }
    }
    val totalLucroAcumulado = remember(sales) {
        sales.sumOf { it.totalProfit }
    }
    val lucroLiquidoReal = remember(totalLucroAcumulado, routeInvestment) {
        totalLucroAcumulado - routeInvestment
    }

    val salesByPayment = remember(sales) {
        sales.groupBy { it.paymentMethod }
            .mapValues { entry -> entry.value.sumOf { CurrencyUtils.parse(it.totalValue) } }
    }

    val paymentMethods = remember { listOf("À Vista", "PIX", "Débito", "Crédito", "À Prazo") }
    
    val activePaymentMethods = remember(salesByPayment) {
        paymentMethods.filter { (salesByPayment[it] ?: 0.0) > 0.0 }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Fluxo de Caixa",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                style = LocalTextStyle.current.copy(
                    brush = Brush.horizontalGradient(listOf(PurplePrimary, CyanSecondary))
                )
            )
            IconButton(onClick = onBack) {
                Icon(Icons.Default.Close, contentDescription = "Fechar", tint = CyanSecondary)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // LINHA 1: CUSTO ROTA (E) | TOTAL VENDIDO (D)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SummaryCardSmall(
                label = "CUSTO DA ROTA", 
                value = CurrencyUtils.format(routeInvestment), 
                color = Color(0xFFFBBF24),
                modifier = Modifier.weight(1f)
            )
            SummaryCardSmall(
                label = "TOTAL VENDIDO", 
                value = CurrencyUtils.format(totalVendidoVal), 
                color = Color.White,
                modifier = Modifier.weight(1f),
                alignment = Alignment.End
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // LINHA 2: LUCRO LÍQUIDO (E) | FECHAR CAIXA (D)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val isPositive = lucroLiquidoReal > 0
            SummaryCardSmall(
                label = "LUCRO LÍQUIDO", 
                value = CurrencyUtils.format(lucroLiquidoReal), 
                color = if (isPositive) Color(0xFF10B981) else Color(0xFFF87171),
                modifier = Modifier.weight(1f)
            )

            Button(
                onClick = { showCloseConfirm = true },
                modifier = Modifier
                    .height(64.dp)
                    .weight(0.7f),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF87171))
            ) {
                Text("FECHAR\nCAIXA", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center, lineHeight = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        if (activePaymentMethods.isNotEmpty()) {
            Text(
                text = "DASHBOARD POR PAGAMENTO",
                color = TextSecondary,
                fontSize = 10.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(activePaymentMethods) { method ->
                    val amount = salesByPayment[method] ?: 0.0
                    PaymentCard(method, CurrencyUtils.format(amount))
                }
            }
        }
    }

    // DIÁLOGO DE SEGURANÇA PARA FECHAR CAIXA
    if (showCloseConfirm) {
        var confirmText by remember { mutableStateOf("") }
        
        Dialog(onDismissRequest = { showCloseConfirm = false }) {
            Surface(
                color = CardBackground,
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.Red.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Confirmar Fechamento", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Para zerar todos os dados do fluxo de caixa, digite FECHAR abaixo:",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    OutlinedTextField(
                        value = confirmText,
                        onValueChange = { confirmText = it.uppercase() },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("FECHAR", color = TextSecondary.copy(alpha = 0.3f)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Red,
                            unfocusedBorderColor = Color.Red.copy(alpha = 0.5f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        TextButton(onClick = { showCloseConfirm = false }, modifier = Modifier.weight(1f)) {
                            Text("Cancelar", color = TextSecondary)
                        }
                        Button(
                            onClick = {
                                if (confirmText == "FECHAR") {
                                    viewModel.resetCashFlow()
                                    showCloseConfirm = false
                                }
                            },
                            enabled = confirmText == "FECHAR",
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Red,
                                disabledContainerColor = Color.Red.copy(alpha = 0.3f)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Confirmar", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SummaryCardSmall(
    label: String, 
    value: String, 
    color: Color, 
    modifier: Modifier = Modifier,
    alignment: Alignment.Horizontal = Alignment.Start
) {
    Surface(
        color = CardBackground.copy(alpha = 0.7f),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, PurplePrimary.copy(alpha = 0.2f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = alignment
        ) {
            Text(text = label, color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            Text(text = value, color = color, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
fun PaymentCard(method: String, amount: String) {
    Surface(
        color = CardBackground.copy(alpha = 0.5f),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.height(80.dp).border(1.dp, CyanSecondary.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = method.uppercase(), color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            Text(text = amount, color = CyanSecondary, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
        }
    }
}
