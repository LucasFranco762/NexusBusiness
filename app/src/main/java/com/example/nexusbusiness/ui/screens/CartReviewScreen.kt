package com.example.nexusbusiness.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nexusbusiness.data.model.SaleCartItem
import com.example.nexusbusiness.ui.theme.CardBackground
import com.example.nexusbusiness.ui.theme.CyanSecondary
import com.example.nexusbusiness.ui.theme.DarkBackground
import com.example.nexusbusiness.ui.theme.PurplePrimary
import com.example.nexusbusiness.ui.theme.TextSecondary
import com.example.nexusbusiness.util.CurrencyUtils
import com.example.nexusbusiness.viewmodel.MainViewModel

@Composable
fun CartReviewScreen(
    viewModel: MainViewModel = viewModel(),
    onBack: () -> Unit
) {
    val cartItems by viewModel.saleDraftItems.collectAsState()
    val clienteNome by viewModel.saleDraftCustomerName.collectAsState()
    val formaPagamento by viewModel.saleDraftPaymentMethod.collectAsState()

    var isPaymentDropdownExpanded by remember { mutableStateOf(false) }
    val paymentMethods = listOf("À Vista", "PIX", "Débito", "Crédito", "À Prazo")

    val totalVenda = cartItems.sumOf { item ->
        CurrencyUtils.parse(item.product.precoVenda) * item.quantity
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Carrinho",
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

        Spacer(modifier = Modifier.height(8.dp))

        Surface(
            color = CardBackground.copy(alpha = 0.7f),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, PurplePrimary.copy(alpha = 0.6f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Cliente: ${if (clienteNome.isBlank()) "-" else clienteNome}",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Itens do carrinho",
                    color = CyanSecondary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(10.dp))

                if (cartItems.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp), contentAlignment = Alignment.Center) {
                        Text("Carrinho vazio", color = TextSecondary, fontSize = 14.sp)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().height(280.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 8.dp)
                    ) {
                        items(cartItems, key = { it.id }) { item ->
                            SaleItemReviewRow(item = item)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(text = "TOTAL:", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Text(text = CurrencyUtils.format(totalVenda), color = CyanSecondary, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
        }

        Spacer(modifier = Modifier.height(12.dp))

        Surface(color = DarkBackground, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                Box(modifier = Modifier.padding(top = 8.dp, bottom = 12.dp)) {
                    Surface(
                        onClick = { isPaymentDropdownExpanded = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = Color.Transparent,
                        border = androidx.compose.foundation.BorderStroke(1.dp, PurplePrimary.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (formaPagamento.isEmpty()) "Selecionar Pagamento" else formaPagamento,
                                color = if (formaPagamento.isEmpty()) TextSecondary.copy(alpha = 0.5f) else Color.White,
                                fontSize = 14.sp
                            )
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = CyanSecondary)
                        }
                    }
                    Box(
                        modifier = Modifier
                            .padding(start = 12.dp)
                            .offset(y = (-8).dp)
                            .background(Color(0xFF1e293b))
                            .padding(horizontal = 4.dp)
                    ) {
                        Text("FORMA DE PAGAMENTO", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                    DropdownMenu(
                        expanded = isPaymentDropdownExpanded,
                        onDismissRequest = { isPaymentDropdownExpanded = false },
                        modifier = Modifier.fillMaxWidth(0.85f).background(CardBackground)
                    ) {
                        paymentMethods.forEach { method ->
                            DropdownMenuItem(
                                text = { Text(method, color = Color.White) },
                                onClick = {
                                    viewModel.setSaleDraftPaymentMethod(method)
                                    isPaymentDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Button(
                    onClick = {
                        viewModel.finalizeSaleDraft()
                        onBack()
                    },
                    enabled = cartItems.isNotEmpty() && formaPagamento.isNotEmpty() && clienteNome.isNotBlank(),
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CyanSecondary,
                        disabledContainerColor = CyanSecondary.copy(alpha = 0.3f)
                    )
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = DarkBackground)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("FINALIZAR VENDA", color = DarkBackground, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun SaleItemReviewRow(item: SaleCartItem) {
    Surface(
        color = CardBackground.copy(alpha = 0.5f),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, PurplePrimary.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(item.product.nome, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(
                    "QTD: ${item.quantity} | UNIT: ${item.product.precoVenda}",
                    color = TextSecondary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                CurrencyUtils.format(CurrencyUtils.parse(item.product.precoVenda) * item.quantity),
                color = CyanSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.End
            )
        }
    }
}
