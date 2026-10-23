package com.example.nexusbusiness.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.nexusbusiness.data.model.Order
import com.example.nexusbusiness.data.model.Product
import com.example.nexusbusiness.ui.components.CustomTextField
import com.example.nexusbusiness.ui.components.PressEffectButton as Button
import com.example.nexusbusiness.ui.components.PressEffectIconButton as IconButton
import com.example.nexusbusiness.ui.components.PressEffectTextButton as TextButton
import com.example.nexusbusiness.ui.theme.*
import com.example.nexusbusiness.util.CurrencyUtils
import com.example.nexusbusiness.viewmodel.MainViewModel

@Composable
fun OrdersScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit,
    onNavigateToVenda: () -> Unit
) {
    val orders by viewModel.orders.collectAsState()
    val products by viewModel.products.collectAsState()
    
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Pedidos atuais", "Histórico")
    
    var orderToDelete by remember { mutableStateOf<Order?>(null) }
    var orderToSell by remember { mutableStateOf<Order?>(null) }
    var showClearHistoryConfirm by remember { mutableStateOf(false) }
    var stockError by remember { mutableStateOf<String?>(null) }
    
    var itemToProcess by remember { mutableStateOf<Triple<String, Int, Order>?>(null) }
    var showUpdateQtyDialog by remember { mutableStateOf<Product?>(null) }
    var showNewProductDialog by remember { mutableStateOf<Triple<String, Int, Order>?>(null) }

    val filteredOrders = if (selectedTab == 0) {
        orders.filter { it.status != "VENDA REALIZADA" }
    } else {
        orders.filter { it.status == "VENDA REALIZADA" }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(text = "Lista de Pedidos", fontSize = 24.sp, fontWeight = FontWeight.Bold, style = LocalTextStyle.current.copy(brush = Brush.horizontalGradient(listOf(PurplePrimary, CyanSecondary))))
            IconButton(onClick = onBack) { Icon(Icons.Default.Close, contentDescription = "Fechar", tint = CyanSecondary) }
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        // ABAS
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.Transparent,
            contentColor = CyanSecondary,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = CyanSecondary
                )
            },
            divider = {}
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { 
                        Text(
                            text = title, 
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 14.sp
                        ) 
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(modifier = Modifier.weight(1f)) {
            if (filteredOrders.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { 
                    Text(
                        if (selectedTab == 0) "Nenhum pedido atual." else "Histórico vazio.", 
                        color = TextSecondary 
                    ) 
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(bottom = 80.dp)) {
                    items(filteredOrders) { order ->
                        OrderCard(
                            order = order,
                            onDelete = { orderToDelete = it },
                            onVenderClick = { targetOrder ->
                                if (targetOrder.status != "VENDA REALIZADA") {
                                    var errorFound = false
                                    val lines = targetOrder.productsSummary.split("\n")
                                    for (line in lines) {
                                        if (line.isNotBlank()) {
                                            val regex = """^(.*)\s\((\d+)\)$""".toRegex()
                                            val match = regex.find(line)
                                            val name = match?.groups?.get(1)?.value ?: line
                                            val qty = match?.groups?.get(2)?.value?.toIntOrNull() ?: 0
                                            
                                            val stockProduct = products.find { it.nome.equals(name, ignoreCase = true) }
                                            if (stockProduct == null) {
                                                stockError = "O produto '$name' não existe no estoque. Por favor, acrescente o produto no estoque primeiramente."
                                                errorFound = true
                                                break
                                            } else if (stockProduct.qtd < qty) {
                                                stockError = "O produto '$name' não possui quantidade suficiente em estoque para atender a esta venda."
                                                errorFound = true
                                                break
                                            }
                                        }
                                    }
                                    if (!errorFound) orderToSell = targetOrder
                                }
                            },
                            onItemClick = { name, qty ->
                                val existingProduct = products.find { it.nome.equals(name, ignoreCase = true) }
                                if (existingProduct != null) {
                                    itemToProcess = Triple(name, qty, order)
                                    showUpdateQtyDialog = existingProduct
                                } else {
                                    showNewProductDialog = Triple(name, qty, order)
                                }
                            }
                        )
                    }
                }
            }
        }

        // Botão Limpar Histórico na aba Histórico
        if (selectedTab == 1 && filteredOrders.isNotEmpty()) {
            Button(
                onClick = { showClearHistoryConfirm = true },
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF87171)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.DeleteSweep, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("LIMPAR HISTÓRICO", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }

    if (showClearHistoryConfirm) {
        AlertDialog(
            onDismissRequest = { showClearHistoryConfirm = false },
            containerColor = CardBackground,
            title = { Text("Limpar Histórico", color = Color.White, fontWeight = FontWeight.Bold) },
            text = { Text("Deseja apagar permanentemente todos os pedidos finalizados?", color = TextSecondary) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearOrdersHistory()
                        showClearHistoryConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) { Text("Confirmar", color = Color.White) }
            },
            dismissButton = {
                TextButton(onClick = { showClearHistoryConfirm = false }) {
                    Text("Cancelar", color = TextSecondary)
                }
            }
        )
    }

    if (stockError != null) {
        AlertDialog(
            onDismissRequest = { stockError = null },
            containerColor = CardBackground,
            title = { Text("Atenção", color = Color.Red, fontWeight = FontWeight.Bold) },
            text = { Text(text = stockError!!, color = TextSecondary) },
            confirmButton = { Button(onClick = { stockError = null }, colors = ButtonDefaults.buttonColors(containerColor = CyanSecondary)) { Text("OK", color = DarkBackground, fontWeight = FontWeight.Bold) } }
        )
    }

    if (orderToSell != null) {
        AlertDialog(
            onDismissRequest = { orderToSell = null },
            containerColor = CardBackground,
            title = { Text("Confirmar Venda", color = Color.White) },
            text = { Text("Deseja realizar a venda deste pedido?", color = TextSecondary) },
            confirmButton = {
                Button(onClick = {
                    viewModel.setPendingOrderForSale(orderToSell)
                    onNavigateToVenda()
                    orderToSell = null
                }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22C55E))) { Text("Sim", color = Color.White) }
            },
            dismissButton = { Button(onClick = { orderToSell = null }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFCA5A5))) { Text("Não", color = Color(0xFF7F1D1D)) } }
        )
    }

    if (showUpdateQtyDialog != null && itemToProcess != null) {
        AlertDialog(
            onDismissRequest = { showUpdateQtyDialog = null },
            containerColor = CardBackground,
            title = { Text("Produto em Estoque", color = Color.White) },
            text = { Text(text = "O produto '${itemToProcess!!.first}' já existe no estoque. Deseja adicionar as ${itemToProcess!!.second} unidades do pedido ao saldo atual?", color = TextSecondary) },
            confirmButton = {
                Button(onClick = {
                    val updatedProduct = showUpdateQtyDialog!!.copy(qtd = showUpdateQtyDialog!!.qtd + itemToProcess!!.second)
                    viewModel.saveProduct(updatedProduct)
                    // Persiste a marcação da seta cinza no banco
                    val updatedOrder = itemToProcess!!.third.let { o ->
                        o.copy(processedItems = if (o.processedItems.isEmpty()) itemToProcess!!.first else "${o.processedItems}\n${itemToProcess!!.first}")
                    }
                    viewModel.saveOrder(updatedOrder)
                    showUpdateQtyDialog = null; itemToProcess = null
                }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22C55E))) { Text("Sim, Atualizar", color = Color.White) }
            },
            dismissButton = { Button(onClick = { showUpdateQtyDialog = null }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFCA5A5))) { Text("Cancelar", color = Color(0xFF7F1D1D)) } }
        )
    }

    if (showNewProductDialog != null) {
        Dialog(onDismissRequest = { showNewProductDialog = null }) {
            OrderToStockRegistrationDialog(
                productName = showNewProductDialog!!.first,
                productQty = showNewProductDialog!!.second,
                onDismiss = { showNewProductDialog = null },
                onConfirm = { product ->
                    viewModel.saveProduct(product)
                    val updatedOrder = showNewProductDialog!!.third.let { o ->
                        o.copy(processedItems = if (o.processedItems.isEmpty()) product.nome else "${o.processedItems}\n${product.nome}")
                    }
                    viewModel.saveOrder(updatedOrder)
                    showNewProductDialog = null
                }
            )
        }
    }

    if (orderToDelete != null) {
        AlertDialog(
            onDismissRequest = { orderToDelete = null },
            containerColor = CardBackground,
            title = { Text("Excluir Pedido", color = Color.White) },
            text = { Text("Deseja excluir o pedido de ${orderToDelete?.customerName}?", color = TextSecondary) },
            confirmButton = {
                Button(onClick = { viewModel.deleteOrder(orderToDelete!!); orderToDelete = null }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22C55E))) { Text("OK", color = Color.White) }
            },
            dismissButton = { Button(onClick = { orderToDelete = null }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFCA5A5))) { Text("Cancelar", color = Color(0xFF7F1D1D)) } }
        )
    }
}

@Composable
fun OrderCard(order: Order, onDelete: (Order) -> Unit, onVenderClick: (Order) -> Unit, onItemClick: (String, Int) -> Unit) {
    val isAlreadySold = order.status == "VENDA REALIZADA"
    val processedItemsList = order.processedItems.split("\n")

    Surface(
        color = CardBackground.copy(alpha = 0.7f),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth().border(1.dp, PurplePrimary.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
    ) {
        Column {
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(text = order.customerName, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                IconButton(onClick = { onDelete(order) }, modifier = Modifier.size(24.dp)) { Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFF87171), modifier = Modifier.size(20.dp)) }
            }
            HorizontalDivider(color = PurplePrimary.copy(alpha = 0.2f), thickness = 1.dp)
            Box(modifier = Modifier.padding(16.dp)) {
                // Tarjeta VENDER com mudança de estado definitiva
                if (!isAlreadySold) {
                    Surface(
                        onClick = { onVenderClick(order) },
                        color = Color(0xFF2DD4BF).copy(alpha = 0.2f),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2DD4BF).copy(alpha = 0.6f)),
                        modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        Text(
                            text = "VENDER",
                            color = Color(0xFF2DD4BF),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }
                } else {
                    Surface(
                        color = Color.Gray.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.Gray),
                        modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        Text(
                            text = "VENDA REALIZADA",
                            color = Color.Gray,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }
                }

                Column(modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
                    Text(text = "ITENS DO PEDIDO:", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    order.productsSummary.split("\n").forEach { line ->
                        if (line.isNotBlank()) {
                            val regex = """^(.*)\s\((\d+)\)$""".toRegex()
                            val match = regex.find(line)
                            val name = match?.groups?.get(1)?.value ?: line
                            val qty = match?.groups?.get(2)?.value?.toIntOrNull() ?: 0
                            
                            val isItemProcessed = processedItemsList.contains(name)

                            Row(
                                verticalAlignment = Alignment.CenterVertically, 
                                modifier = Modifier.padding(vertical = 4.dp)
                                    .then(if (!isItemProcessed && !isAlreadySold) Modifier.clickable { onItemClick(name, qty) } else Modifier)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack, 
                                    contentDescription = null, 
                                    tint = if (isItemProcessed || isAlreadySold) Color.Gray else Color(0xFF10B981), 
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = line, color = if (isItemProcessed || isAlreadySold) Color.Gray else Color.White, fontSize = 14.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OrderToStockRegistrationDialog(productName: String, productQty: Int, onDismiss: () -> Unit, onConfirm: (Product) -> Unit) {
    var nome by remember { mutableStateOf(productName) }
    var qtd by remember { mutableStateOf(productQty.toString()) }
    var custo by remember { mutableStateOf("") }
    var margem by remember { mutableStateOf("") }
    Surface(color = CardBackground, shape = RoundedCornerShape(20.dp), border = androidx.compose.foundation.BorderStroke(1.dp, PurplePrimary.copy(alpha = 0.4f)), modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Cadastrar no Estoque", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, contentDescription = null, tint = CyanSecondary) }
            }
            Spacer(modifier = Modifier.height(24.dp))
            CustomTextField(value = nome, onValueChange = { nome = it.replaceFirstChar { char -> char.uppercase() } }, label = "Nome do Produto", placeholder = "Produto...")
            Spacer(modifier = Modifier.height(16.dp))
            CustomTextField(value = qtd, onValueChange = { if (it.all { char -> char.isDigit() }) qtd = it }, label = "Qtd", placeholder = "0", keyboardType = KeyboardType.Number)
            Spacer(modifier = Modifier.height(16.dp))
            CustomTextField(value = custo, onValueChange = { input -> val clean = input.replace("\\D".toRegex(), ""); custo = if (clean.isEmpty()) "" else CurrencyUtils.format(clean.toDouble() / 100.0) }, label = "Custo", placeholder = "R$ 0,00", keyboardType = KeyboardType.Number)
            Spacer(modifier = Modifier.height(16.dp))
            CustomTextField(value = margem, onValueChange = { margem = it }, label = "Margem (%)", placeholder = "0%", keyboardType = KeyboardType.Number)
            Spacer(modifier = Modifier.height(32.dp))
            Button(onClick = { if (nome.isNotBlank() && (qtd.toIntOrNull() ?: 0) > 0) { onConfirm(Product(nome = nome, qtd = qtd.toInt(), custo = custo, margem = margem.toDoubleOrNull() ?: 0.0)) } }, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)), enabled = nome.isNotBlank() && custo.isNotBlank() && margem.isNotBlank()) {
                Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.Add, contentDescription = null, tint = Color.White); Spacer(modifier = Modifier.width(8.dp)); Text("SALVAR NO ESTOQUE", fontWeight = FontWeight.Bold, color = Color.White) }
            }
        }
    }
}
