package com.example.nexusbusiness.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.AlertDialog
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nexusbusiness.data.model.Product
import com.example.nexusbusiness.data.model.SaleCartItem
import com.example.nexusbusiness.ui.components.CustomTextField
import com.example.nexusbusiness.ui.theme.CardBackground
import com.example.nexusbusiness.ui.theme.CyanSecondary
import com.example.nexusbusiness.ui.theme.DarkBackground
import com.example.nexusbusiness.ui.theme.PurplePrimary
import com.example.nexusbusiness.ui.theme.TextSecondary
import com.example.nexusbusiness.util.CurrencyUtils
import com.example.nexusbusiness.viewmodel.MainViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MakeSaleScreen(
    viewModel: MainViewModel = viewModel(),
    onBack: () -> Unit,
    onOpenCartReview: () -> Unit
) {
    val allProducts by viewModel.products.collectAsState()
    val isStockLocked by viewModel.isStockLocked.collectAsState()
    val pendingOrder by viewModel.pendingOrderForSale.collectAsState()
    val cartItems by viewModel.saleDraftItems.collectAsState()
    val clienteNome by viewModel.saleDraftCustomerName.collectAsState()
    val formaPagamento by viewModel.saleDraftPaymentMethod.collectAsState()

    var selectedProduct by remember { mutableStateOf<Product?>(null) }
    var selectedQty by remember { mutableStateOf("0") }

    var isProductDropdownExpanded by remember { mutableStateOf(false) }
    var isQtyDropdownExpanded by remember { mutableStateOf(false) }
    var isPaymentDropdownExpanded by remember { mutableStateOf(false) }
    var hasJustLockedManually by remember { mutableStateOf(false) }
    var productToRemove by remember { mutableStateOf<SaleCartItem?>(null) }

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(pendingOrder) {
        pendingOrder?.let { order ->
            viewModel.loadSaleDraftFromOrder(order)
            viewModel.setPendingOrderForSale(null)
        }
    }

    val totalVenda = cartItems.sumOf { item ->
        CurrencyUtils.parse(item.product.precoVenda) * item.quantity
    }

    val paymentMethods = listOf("À Vista", "PIX", "Débito", "Crédito", "À Prazo")

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
                text = "Realizar Venda",
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CustomTextField(
                        value = clienteNome,
                        onValueChange = { input ->
                            viewModel.setSaleDraftCustomerName(
                                input.split(" ").joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }
                            )
                        },
                        label = "Nome do Cliente",
                        placeholder = "Nome...",
                        modifier = Modifier.weight(1f)
                    )

                    val interactionSource = remember { MutableInteractionSource() }
                    val isPressed by interactionSource.collectIsPressedAsState()
                    val buttonScale by animateFloatAsState(if (isPressed) 0.92f else 1f, label = "scale")
                    val canAdd = selectedProduct != null && clienteNome.isNotBlank() && selectedQty.toInt() > 0

                    Surface(
                        onClick = {
                            if (canAdd) {
                                val qty = selectedQty.toInt()
                                viewModel.addSaleDraftItem(selectedProduct!!, qty)
                                coroutineScope.launch { listState.animateScrollToItem(0) }
                                selectedProduct = null
                                selectedQty = "0"
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
                            Icon(
                                Icons.Default.Add,
                                contentDescription = null,
                                tint = if (canAdd) Color.White else Color.White.copy(alpha = 0.5f),
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(modifier = Modifier.weight(1f).padding(top = 8.dp)) {
                        Surface(
                            onClick = { isProductDropdownExpanded = true },
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
                                    text = selectedProduct?.nome ?: "Selecionar Produto",
                                    color = if (selectedProduct == null) TextSecondary.copy(alpha = 0.5f) else Color.White,
                                    fontSize = 14.sp,
                                    maxLines = 1
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
                            Text("PRODUTO", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                        DropdownMenu(
                            expanded = isProductDropdownExpanded,
                            onDismissRequest = { isProductDropdownExpanded = false },
                            modifier = Modifier.fillMaxWidth(0.7f).background(CardBackground)
                        ) {
                            allProducts.filter { it.qtd > 0 }.forEach { product ->
                                DropdownMenuItem(
                                    text = { Text("${product.nome} (${product.qtd})", color = Color.White) },
                                    onClick = {
                                        selectedProduct = product
                                        selectedQty = "0"
                                        isProductDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Box(modifier = Modifier.width(90.dp).padding(top = 8.dp)) {
                        Surface(
                            onClick = { if (selectedProduct != null) isQtyDropdownExpanded = true },
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
                                Text(text = selectedQty, color = Color.White, fontSize = 14.sp)
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
                            Text("QTD", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                        DropdownMenu(
                            expanded = isQtyDropdownExpanded,
                            onDismissRequest = { isQtyDropdownExpanded = false },
                            modifier = Modifier.width(90.dp).background(CardBackground)
                        ) {
                            val maxQty = selectedProduct?.qtd ?: 0
                            for (i in 0..(if (maxQty > 999) 999 else maxQty)) {
                                DropdownMenuItem(
                                    text = { Text(i.toString(), color = Color.White) },
                                    onClick = {
                                        selectedQty = i.toString()
                                        isQtyDropdownExpanded = false
                                    }
                                )
                            }
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
            Text(
                text = "Carrinho",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.combinedClickable(
                    onClick = { },
                    onDoubleClick = onOpenCartReview
                )
            )
            Column(horizontalAlignment = Alignment.End) {
                Text(text = "TOTAL:", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Text(text = CurrencyUtils.format(totalVenda), color = CyanSecondary, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Box(modifier = Modifier.weight(1f)) {
            if (cartItems.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Carrinho vazio", color = TextSecondary, fontSize = 14.sp)
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(cartItems, key = { it.id }) { item ->
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
                                Column {
                                    Text(item.product.nome, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text(
                                        "QTD: ${item.quantity} | UNIT: ${item.product.precoVenda} | MARGEM: ${String.format("%.2f%%", item.product.margem)}",
                                        color = TextSecondary,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                IconButton(onClick = { productToRemove = item }) {
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
                    onClick = { viewModel.finalizeSaleDraft() },
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

    if (!isStockLocked) {
        Dialog(
            onDismissRequest = { },
            properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
        ) {
            Surface(
                color = CardBackground,
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.Red.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.Close, contentDescription = "Voltar", tint = CyanSecondary)
                        }
                    }
                    Icon(
                        imageVector = Icons.Default.LockOpen,
                        contentDescription = null,
                        tint = Color.Red,
                        modifier = Modifier
                            .size(64.dp)
                            .combinedClickable(onClick = {}, onDoubleClick = {
                                viewModel.toggleStockLock()
                                hasJustLockedManually = true
                            })
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Estoque aberto. Feche o estoque para continuar!",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Dê um duplo clique no cadeado acima para fechar.",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    } else if (hasJustLockedManually) {
        Dialog(onDismissRequest = { hasJustLockedManually = false }) {
            Surface(
                color = CardBackground,
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(64.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Estoque fechado. Venda liberada!",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { hasJustLockedManually = false },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("OK", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    if (productToRemove != null) {
        AlertDialog(
            onDismissRequest = { productToRemove = null },
            containerColor = CardBackground,
            title = { Text("Remover do Carrinho", color = Color.White) },
            text = { Text(text = "Deseja remover ${productToRemove?.product?.nome} do carrinho?", color = TextSecondary) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.removeSaleDraftItem(productToRemove!!.id)
                        productToRemove = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22C55E))
                ) { Text("OK", color = Color.White) }
            },
            dismissButton = {
                Button(
                    onClick = { productToRemove = null },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFCA5A5))
                ) { Text("Cancelar", color = Color(0xFF7F1D1D)) }
            }
        )
    }
}

