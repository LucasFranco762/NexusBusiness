package com.example.nexusbusiness.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nexusbusiness.data.model.Product
import com.example.nexusbusiness.ui.theme.*
import java.util.Locale

@Composable
fun ProductCard(
    product: Product,
    onEdit: (Product) -> Unit,
    onDelete: (Product) -> Unit,
    onPriceAdjustment: (Product) -> Unit
) {
    Surface(
        color = CardBackground.copy(alpha = 0.7f),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .border(
                width = 1.dp,
                color = if (product.qtd == 0) Color.Red.copy(alpha = 0.5f) else PurplePrimary.copy(alpha = 0.5f),
                shape = RoundedCornerShape(16.dp)
            )
    ) {
        Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max)) {
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .fillMaxHeight()
                    .background(
                        if (product.qtd == 0) Color.Red else PurplePrimary,
                        shape = RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp)
                    )
            )

            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ProductPhotoThumbnail(
                    photoPath = product.photoPath,
                    modifier = Modifier.size(72.dp),
                    enableDoubleTapPreview = product.photoPath.isNotBlank()
                )

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = product.nome,
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )

                            if (product.qtd == 0) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    color = Color.Red,
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "ESGOTADO",
                                        color = Color.Black,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        Text(
                            text = product.precoVenda,
                            color = CyanSecondary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable { onPriceAdjustment(product) }
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Qtd: ", color = CyanSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Text(product.qtd.toString(), color = TextPrimary, fontSize = 10.sp)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("|", color = Color(0xFF334155), fontSize = 10.sp)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("Rateio: ", color = Color(0xFFF97316), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Text(product.rateio, color = TextPrimary, fontSize = 10.sp)
                            }
                            IconButton(onClick = { onDelete(product) }, modifier = Modifier.size(24.dp)) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Excluir",
                                    tint = Color(0xFFF87171),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Margem: ", color = CyanSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Text(String.format(Locale.getDefault(), "%.1f%%", product.margem), color = TextPrimary, fontSize = 10.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("|", color = Color(0xFF334155), fontSize = 10.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Custo: ", color = CyanSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Text(product.custo, color = TextPrimary, fontSize = 10.sp)
                            }
                            IconButton(onClick = { onEdit(product) }, modifier = Modifier.size(24.dp)) {
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = "Editar",
                                    tint = Color(0xFF818CF8),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
