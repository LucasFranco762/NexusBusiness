package com.example.nexusbusiness.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ImageNotSupported
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.nexusbusiness.ui.theme.CardBackground
import com.example.nexusbusiness.ui.theme.CyanSecondary
import com.example.nexusbusiness.ui.theme.PurplePrimary
import com.example.nexusbusiness.ui.theme.TextSecondary
import com.example.nexusbusiness.util.ProductImageStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ProductPhotoThumbnail(
    photoPath: String?,
    modifier: Modifier = Modifier,
    enableDoubleTapPreview: Boolean = false
) {
    var showDialog by remember(photoPath) { mutableStateOf(false) }
    val bitmap by loadProductBitmap(photoPath = photoPath, maxDimension = 512)

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap!!.asImageBitmap(),
                contentDescription = "Foto do produto",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, PurplePrimary.copy(alpha = 0.45f), RoundedCornerShape(12.dp))
                    .then(
                        if (enableDoubleTapPreview) {
                            Modifier.combinedClickable(
                                onClick = {},
                                onDoubleClick = { showDialog = true }
                            )
                        } else {
                            Modifier
                        }
                    )
            )
        } else {
            PhotoPlaceholder()
        }
    }

    if (showDialog && bitmap != null && !photoPath.isNullOrBlank()) {
        ProductPhotoDialog(photoPath = photoPath, onDismiss = { showDialog = false })
    }
}

@Composable
fun ProductPhotoDialog(photoPath: String, onDismiss: () -> Unit) {
    val bitmap by loadProductBitmap(photoPath = photoPath, maxDimension = 2048)
    if (bitmap == null) return

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            color = Color.Black.copy(alpha = 0.94f),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .padding(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Image(
                    bitmap = bitmap!!.asImageBitmap(),
                    contentDescription = "Foto do produto ampliada",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 720.dp)
                        .padding(top = 32.dp, bottom = 8.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, CyanSecondary.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                )

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Fechar",
                        tint = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun loadProductBitmap(photoPath: String?, maxDimension: Int): State<Bitmap?> {
    val bitmapState = remember(photoPath, maxDimension) { mutableStateOf<Bitmap?>(null) }
    LaunchedEffect(photoPath, maxDimension) {
        bitmapState.value = withContext(Dispatchers.IO) {
            photoPath
                ?.takeIf(String::isNotBlank)
                ?.let { ProductImageStorage.decodeOrientedBitmap(it, maxDimension, maxDimension) }
        }
    }
    return bitmapState
}

@Composable
private fun PhotoPlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(12.dp))
            .background(CardBackground.copy(alpha = 0.8f))
            .border(1.dp, PurplePrimary.copy(alpha = 0.35f), RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.ImageNotSupported,
            contentDescription = "Produto sem fotografia",
            tint = TextSecondary,
            modifier = Modifier.size(26.dp)
        )
    }
}
