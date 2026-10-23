package com.example.nexusbusiness.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nexusbusiness.data.model.Order
import com.example.nexusbusiness.ui.components.CustomTextField
import com.example.nexusbusiness.ui.components.PressEffectButton as Button
import com.example.nexusbusiness.ui.components.PressEffectIconButton as IconButton
import com.example.nexusbusiness.ui.theme.*
import com.example.nexusbusiness.viewmodel.MainViewModel
import kotlinx.coroutines.launch

@Composable
fun MakeOrderScreen(
    viewModel: MainViewModel = viewModel(),
    onBack: () -> Unit
) {
    var clienteNome by remember { mutableStateOf("") }
    var produtoNome by remember { mutableStateOf("") }
    var qtd by remember { mutableStateOf("0") }
    
    val orderItems = remember { mutableStateListOf<Pair<String, String>>() }
    var itemToRemove by remember { mutableStateOf<Pair<String, String>?>(null) }

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Otimização: estados derivados para evitar cálculos durante a digitação
    val canAdd by remember {
        derivedStateOf {
            clienteNome.isNotBlank() && produtoNome.isNotBlank() && (qtd.toIntOrNull() ?: 0) > 0
        }
    }

    val canFinalize by remember {
        derivedStateOf {
            orderItems.isNotEmpty() && clienteNome.isNotBlank()
        }
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
                text = "Pedidos",
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

        Spacer(modifier = Modifier.height(12.dp))

        Surface(
            color = CardBackground.copy(alpha = 0.7f),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, PurplePrimary.copy(alpha = 0.6f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CustomTextField(
                        value = clienteNome,
                        onValueChange = { clienteNome = it },
                        label = "Nome do Cliente",
                        placeholder = "Nome...",
                        modifier = Modifier.weight(1f)
                    )

                    val interactionSource = remember { MutableInteractionSource() }
                    val isPressed by interactionSource.collectIsPressedAsState()
                    val buttonScale by animateFloatAsState(if (isPressed) 0.92f else 1f, label = "scale")

                    Surface(
                        onClick = {
                            if (canAdd) {
                                // Formata o produto apenas no momento de adicionar
                                val formattedProduct = produtoNome.trim().split(" ").joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }
                                orderItems.add(0, formattedProduct to qtd)
                                coroutineScope.launch { listState.animateScrollToItem(0) }
                                produtoNome = ""
                                qtd = "0"
                            }
                        },
                        enabled = canAdd,
                        modifier = Modifier.size(56.dp).scale(buttonScale).padding(top = 8.dp),
                        shape = RoundedCornerShape(14.dp),
                        color = if (canAdd) Color(0xFF10B981) else Color(0xFF10B981).copy(alpha = 0.3f),
                        shadowElevation = if (canAdd && isPressed) 0.dp else 8.dp,
                        interactionSource = interactionSource
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = if (canAdd) Color.White else Color.White.copy(alpha = 0.5f), modifier = Modifier.size(32.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CustomTextField(
                        value = produtoNome,
                        onValueChange = { produtoNome = it },
                        label = "Produto",
                        placeholder = "Produto...",
                        modifier = Modifier.weight(1f)
                    )

                    CustomTextField(
                        value = qtd,
                        onValueChange = { input ->
                            if (input.isEmpty()) {
                                qtd = "0"
                            } else if (input.all { it.isDigit() } && input.length <= 3) {
                                qtd = if (input.startsWith("0") && input.length > 1) input.substring(1) else input
                            }
                        },
                        label = "QTD",
                        placeholder = "0",
                        modifier = Modifier.width(90.dp),
                        keyboardType = KeyboardType.Number
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "Lista de Itens", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(bottom = 8.dp))

        Box(modifier = Modifier.weight(1f)) {
            if (orderItems.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Nenhum item adicionado", color = TextSecondary, fontSize = 14.sp)
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(orderItems) { item ->
                        val (prod, q) = item
                        Surface(
                            color = CardBackground.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().border(1.dp, PurplePrimary.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        ) {
                            Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column {
                                    Text(prod, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("QTD: $q", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                                IconButton(onClick = { itemToRemove = item }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Remover", tint = Color(0xFFF87171), modifier = Modifier.size(20.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        Surface(color = DarkBackground, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                Button(
                    onClick = {
                        if (canFinalize) {
                            // Formata o cliente apenas ao salvar
                            val formattedCustomer = clienteNome.trim().split(" ").joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }
                            val productsSummary = orderItems.joinToString("\n") { "${it.first} (${it.second})" }
                            val order = Order(
                                customerName = formattedCustomer,
                                productsSummary = productsSummary,
                                totalValue = "N/A",
                                status = "PENDENTE"
                            )
                            viewModel.saveOrder(order)
                            orderItems.clear(); clienteNome = ""; produtoNome = ""; qtd = "0"
                        }
                    },
                    enabled = canFinalize,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CyanSecondary, disabledContainerColor = CyanSecondary.copy(alpha = 0.3f))
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = DarkBackground)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("FINALIZAR PEDIDO", color = DarkBackground, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
                    }
                }
            }
        }
    }

    if (itemToRemove != null) {
        AlertDialog(
            onDismissRequest = { itemToRemove = null },
            containerColor = CardBackground,
            title = { Text("Remover Item", color = Color.White) },
            text = { Text(text = "Deseja remover ${itemToRemove?.first} do pedido?", color = TextSecondary) },
            confirmButton = {
                Button(
                    onClick = {
                        orderItems.remove(itemToRemove)
                        itemToRemove = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22C55E))
                ) { Text("OK", color = Color.White) }
            },
            dismissButton = {
                Button(
                    onClick = { itemToRemove = null },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFCA5A5))
                ) { Text("Cancelar", color = Color(0xFF7F1D1D)) }
            }
        )
    }
}
