package com.example.nexusbusiness.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nexusbusiness.data.model.Route
import com.example.nexusbusiness.ui.theme.*

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RouteCard(
    route: Route,
    onEdit: (Route) -> Unit,
    onDelete: (Route) -> Unit,
    onSetActive: (Route) -> Unit
) {
    Surface(
        color = CardBackground.copy(alpha = 0.7f),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .shadow(
                elevation = if (route.ativa) 8.dp else 2.dp,
                spotColor = if (route.ativa) PurplePrimary else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            )
            .border(
                width = 1.dp,
                color = PurplePrimary.copy(alpha = 0.5f),
                shape = RoundedCornerShape(12.dp)
            )
            .combinedClickable(
                onClick = { /* Single click can be used for details later */ },
                onDoubleClick = { onSetActive(route) }
            )
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            // Barra lateral de status (Ativa/Inativa)
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .fillMaxHeight()
                    .background(
                        if (route.ativa) PurplePrimary else CyanSecondary,
                        shape = RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp)
                    )
            )

            // Conteúdo do Card
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = route.cidade, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        if (route.ativa) {
                            Spacer(modifier = Modifier.width(12.dp))
                            Surface(
                                color = Color.Black,
                                shape = RoundedCornerShape(4.dp),
                                border = BorderStroke(1.dp, PurplePrimary)
                            ) {
                                Text(
                                    text = "ATIVA",
                                    color = PurplePrimary,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    Text(
                        text = "Custo Op: ${route.custo}",
                        color = CyanSecondary,
                        fontSize = 12.sp,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    PressEffectIconButton(onClick = { onEdit(route) }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar", tint = Color(0xFF818CF8), modifier = Modifier.size(18.dp))
                    }
                    PressEffectIconButton(onClick = { onDelete(route) }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Excluir", tint = Color(0xFFF87171), modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}
