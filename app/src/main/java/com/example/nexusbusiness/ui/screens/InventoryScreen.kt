package com.example.nexusbusiness.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.nexusbusiness.data.model.Product
import com.example.nexusbusiness.ui.components.ProductCard
import com.example.nexusbusiness.ui.components.PressEffectButton as Button
import com.example.nexusbusiness.ui.components.PressEffectIconButton as IconButton
import com.example.nexusbusiness.ui.theme.*
import com.example.nexusbusiness.util.CurrencyUtils
import com.example.nexusbusiness.viewmodel.MainViewModel
import kotlinx.coroutines.delay
import kotlin.math.max
import kotlin.math.round

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun InventoryScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit,
    onEdit: (Product) -> Unit
) {
    val products by viewModel.products.collectAsState()
    val routes by viewModel.routes.collectAsState()
    val isLocked by viewModel.isStockLocked.collectAsState()
    val activeRoute = routes.find { it.ativa }
    
    var productToDelete by remember { mutableStateOf<Product?>(null) }
    var productToAdjust by remember { mutableStateOf<Product?>(null) }
    var showLockDialog by remember { mutableStateOf<String?>(null) }
    var showNoRouteDialog by remember { mutableStateOf(false) }

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
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Estoque",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    style = LocalTextStyle.current.copy(
                        brush = Brush.horizontalGradient(listOf(PurplePrimary, CyanSecondary))
                    )
                )
                Spacer(modifier = Modifier.width(12.dp))
                
                // ÍCONE DE CADEADO COM DOUBLE CLICK
                Icon(
                    imageVector = if (isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                    contentDescription = "Trava de Estoque",
                    tint = if (isLocked) Color(0xFF10B981) else Color.Red,
                    modifier = Modifier
                        .size(28.dp)
                        .combinedClickable(
                            onClick = { /* Clique simples ignorado */ },
                            onDoubleClick = {
                                if (!isLocked && activeRoute == null) {
                                    showNoRouteDialog = true
                                } else {
                                    viewModel.toggleStockLock()
                                    showLockDialog = if (!isLocked) "ESTOQUE FECHADO" else "ESTOQUE ABERTO"
                                }
                            }
                        )
                )
            }
            IconButton(onClick = onBack) {
                Icon(Icons.Default.Close, contentDescription = "Fechar", tint = CyanSecondary)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (products.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Estoque vazio.", color = TextSecondary)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(products, key = { it.id }) { product ->
                    ProductCard(
                        product = product,
                        onEdit = { onEdit(it) },
                        onDelete = { productToDelete = it },
                        onPriceAdjustment = { productToAdjust = it }
                    )
                }
            }
        }
    }

    // Modal de Notificação da Trava
    if (showNoRouteDialog) {
        AlertDialog(
            onDismissRequest = { showNoRouteDialog = false },
            containerColor = CardBackground,
            title = {
                Text(
                    text = "Nenhuma rota ativa",
                    color = Color(0xFFFBBF24),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp
                )
            },
            text = {
                Text(
                    text = "Para fechar o estoque, ative primeiro uma rota. Depois disso, tente fechar novamente.",
                    color = TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = { showNoRouteDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanSecondary)
                ) {
                    Text("OK", color = DarkBackground, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    if (showLockDialog != null) {
        AlertDialog(
            onDismissRequest = { showLockDialog = null },
            containerColor = CardBackground,
            title = {
                Text(
                    text = showLockDialog!!,
                    color = if (showLockDialog == "ESTOQUE FECHADO") Color(0xFF10B981) else Color.Red,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = { showLockDialog = null },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanSecondary)
                ) {
                    Text("OK", color = DarkBackground, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // Modal de Exclusão
    if (productToDelete != null) {
        AlertDialog(
            onDismissRequest = { productToDelete = null },
            containerColor = CardBackground,
            title = { Text("Excluir Item", color = Color.White) },
            text = { Text(text = "Você confirma a exclusão de ${productToDelete?.nome}?", color = TextSecondary) },
            confirmButton = {
                Button(
                    onClick = {
                        productToDelete?.let { viewModel.deleteProduct(it) }
                        productToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22C55E))
                ) { Text("OK", color = Color.White) }
            },
            dismissButton = {
                Button(
                    onClick = { productToDelete = null },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFCA5A5))
                ) { Text("Cancelar", color = Color(0xFF7F1D1D)) }
            }
        )
    }

    // Modal de Ajuste Fino
    if (productToAdjust != null) {
        FineAdjustmentDialog(
            product = productToAdjust!!,
            onDismiss = { productToAdjust = null },
            onConfirm = { newPrice, newMargin ->
                viewModel.saveProduct(productToAdjust!!.copy(precoVenda = newPrice, margem = newMargin))
                productToAdjust = null
            }
        )
    }
}

@Composable
fun FineAdjustmentDialog(
    product: Product,
    onDismiss: () -> Unit,
    onConfirm: (String, Double) -> Unit
) {
    val initialPrice = CurrencyUtils.parse(product.precoVenda)
    val custo = CurrencyUtils.parse(product.custo)
    val rateio = CurrencyUtils.parse(product.rateio)
    var currentPrice by remember { mutableStateOf(initialPrice) }
    val currentMargin = if (custo > 0) ((currentPrice - custo - rateio) / custo) * 100 else 0.0

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            color = CardBackground,
            shape = RoundedCornerShape(20.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, PurplePrimary.copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Ajuste Fino", fontSize = 18.sp, fontWeight = FontWeight.Bold, style = LocalTextStyle.current.copy(brush = Brush.horizontalGradient(listOf(PurplePrimary, CyanSecondary))))
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, contentDescription = null, tint = CyanSecondary) }
                }
                Spacer(modifier = Modifier.height(24.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("MARGEM", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text(text = String.format("%.2f%%", currentMargin), color = CyanSecondary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("PREÇO ATUAL", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text(text = CurrencyUtils.format(currentPrice), color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    RepeatingButton(icon = Icons.Default.Remove, tint = Color(0xFFF43F5E), onAction = { currentPrice = max(0.0, round((currentPrice - 0.01) * 100) / 100.0) })
                    Slider(value = currentPrice.toFloat(), onValueChange = { currentPrice = it.toDouble() }, valueRange = (initialPrice * 0.5f).toFloat()..(initialPrice * 1.5f).toFloat(), modifier = Modifier.weight(1f), colors = SliderDefaults.colors(thumbColor = CyanSecondary, activeTrackColor = CyanSecondary, inactiveTrackColor = PurplePrimary.copy(alpha = 0.2f)))
                    RepeatingButton(icon = Icons.Default.Add, tint = Color(0xFF10B981), onAction = { currentPrice = round((currentPrice + 0.01) * 100) / 100.0 })
                }
                Spacer(modifier = Modifier.height(32.dp))
                Button(onClick = { onConfirm(CurrencyUtils.format(currentPrice), currentMargin) }, modifier = Modifier.fillMaxWidth().height(48.dp), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))) {
                    Text("Salvar Preço", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun RepeatingButton(icon: ImageVector, tint: Color, onAction: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    LaunchedEffect(isPressed) { if (isPressed) { onAction(); delay(1000); while (isPressed) { onAction(); delay(50) } } }
    Surface(onClick = { }, interactionSource = interactionSource, modifier = Modifier.size(40.dp), shape = RoundedCornerShape(50), color = Color.Transparent, border = androidx.compose.foundation.BorderStroke(1.dp, tint.copy(alpha = 0.5f))) {
        Box(contentAlignment = Alignment.Center) { Icon(icon, contentDescription = null, tint = tint) }
    }
}
