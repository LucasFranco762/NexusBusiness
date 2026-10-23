package com.example.nexusbusiness.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.nexusbusiness.data.model.Route
import com.example.nexusbusiness.ui.components.CustomTextField
import com.example.nexusbusiness.ui.components.NeonButton
import com.example.nexusbusiness.ui.components.RouteCard
import com.example.nexusbusiness.ui.components.PressEffectButton as Button
import com.example.nexusbusiness.ui.components.PressEffectIconButton as IconButton
import com.example.nexusbusiness.ui.theme.CyanSecondary
import com.example.nexusbusiness.ui.theme.PurplePrimary
import com.example.nexusbusiness.ui.theme.TextSecondary
import com.example.nexusbusiness.ui.theme.CardBackground
import com.example.nexusbusiness.util.CurrencyUtils
import com.example.nexusbusiness.viewmodel.MainViewModel

@Composable
fun RoutesScreen(viewModel: MainViewModel) {
    val routes by viewModel.routes.collectAsState()
    var showRouteDialog by remember { mutableStateOf(false) }
    var routeToEdit by remember { mutableStateOf<Route?>(null) }
    var routeToDelete by remember { mutableStateOf<Route?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 24.dp)
        ) {
            Icon(
                Icons.Default.Map,
                contentDescription = null,
                tint = Color(0xFFFB923C),
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Logística de Entrega",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                style = LocalTextStyle.current.copy(
                    brush = Brush.horizontalGradient(
                        listOf(PurplePrimary, CyanSecondary)
                    )
                )
            )
        }

        NeonButton(
            text = "Inserir Rota",
            icon = Icons.Default.Map,
            onClick = {
                routeToEdit = null
                showRouteDialog = true
            },
            modifier = Modifier.align(Alignment.CenterHorizontally),
            glassEffect = true
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (routes.isEmpty()) {
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Nenhuma rota cadastrada.",
                    color = TextSecondary,
                    fontSize = 14.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(routes, key = { it.id }) { route ->
                    RouteCard(
                        route = route,
                        onEdit = {
                            routeToEdit = it
                            showRouteDialog = true
                        },
                        onDelete = { routeToDelete = it },
                        onSetActive = { viewModel.toggleRouteActive(it.id) }
                    )
                }
            }
        }
    }

    // Modal Nova/Editar Rota
    if (showRouteDialog) {
        RouteDialog(
            route = routeToEdit,
            onDismiss = { showRouteDialog = false },
            onConfirm = { city, cost ->
                val newRoute = routeToEdit?.copy(cidade = city, custo = cost) 
                    ?: Route(cidade = city, custo = cost)
                viewModel.saveRoute(newRoute)
                showRouteDialog = false
            }
        )
    }

    // Modal Excluir Rota
    if (routeToDelete != null) {
        AlertDialog(
            onDismissRequest = { routeToDelete = null },
            containerColor = CardBackground,
            title = { Text("Excluir Rota", color = Color.White) },
            text = {
                Text(
                    text = "Você confirma a exclusão da rota ${routeToDelete?.cidade}?",
                    color = TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        routeToDelete?.let { viewModel.deleteRoute(it) }
                        routeToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22C55E))
                ) {
                    Text("OK", color = Color.White)
                }
            },
            dismissButton = {
                Button(
                    onClick = { routeToDelete = null },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFCA5A5))
                ) {
                    Text("Cancelar", color = Color(0xFF7F1D1D))
                }
            }
        )
    }
}

@Composable
fun RouteDialog(
    route: Route?,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var cidade by remember { mutableStateOf(route?.cidade ?: "") }
    var custo by remember { mutableStateOf(route?.custo ?: "") }
    var custoField by remember(route?.id) {
        mutableStateOf(
            TextFieldValue(
                text = route?.custo ?: "",
                selection = TextRange((route?.custo ?: "").length)
            )
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            color = CardBackground,
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, PurplePrimary.copy(alpha = 0.4f)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (route == null) "Nova Rota" else "Editar Rota",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        style = LocalTextStyle.current.copy(
                            brush = Brush.horizontalGradient(listOf(PurplePrimary, CyanSecondary))
                        )
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = null, tint = CyanSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                CustomTextField(
                    value = cidade,
                    onValueChange = { 
                        cidade = it.split(" ").joinToString(" ") { word ->
                            word.replaceFirstChar { char -> char.uppercase() }
                        }
                    },
                    label = "Cidade",
                    placeholder = "Ex: São Paulo"
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = custoField,
                        onValueChange = { input ->
                            val clean = input.text.replace("\\D".toRegex(), "")
                            if (clean.isEmpty()) {
                                custo = ""
                                custoField = TextFieldValue("")
                            } else {
                                val parsed = clean.toDouble() / 100.0
                                val formatted = CurrencyUtils.format(parsed)
                                custo = formatted
                                custoField = TextFieldValue(
                                    text = formatted,
                                    selection = TextRange(formatted.length)
                                )
                            }
                        },
                        modifier = Modifier.weight(1f),
                        label = {
                            Text(
                                text = "CUSTO OPERACIONAL",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary
                            )
                        },
                        placeholder = {
                            Text("R$ 0,00", color = TextSecondary.copy(alpha = 0.5f), fontSize = 14.sp)
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanSecondary,
                            unfocusedBorderColor = PurplePrimary.copy(alpha = 0.4f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = CyanSecondary,
                            focusedLabelColor = CyanSecondary,
                            unfocusedLabelColor = TextSecondary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        ),
                        singleLine = true
                    )

                    Button(
                        onClick = { if (cidade.isNotBlank()) onConfirm(cidade, custo) },
                        modifier = Modifier.size(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (route == null) Color(0xFF10B981) else Color(0xFF3B82F6)
                        ),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(
                            if (route == null) Icons.Default.Add else Icons.Default.Save,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
        }
    }
}
