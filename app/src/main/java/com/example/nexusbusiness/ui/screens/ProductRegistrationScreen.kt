package com.example.nexusbusiness.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.BackHandler
import androidx.core.content.FileProvider
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.nexusbusiness.data.model.Product
import com.example.nexusbusiness.ui.components.CustomTextField
import com.example.nexusbusiness.ui.components.ProductPhotoThumbnail
import com.example.nexusbusiness.ui.theme.*
import com.example.nexusbusiness.util.CurrencyUtils
import com.example.nexusbusiness.util.ProductImageStorage
import com.example.nexusbusiness.viewmodel.MainViewModel
import kotlinx.coroutines.launch
import java.io.File
import java.util.Locale

@Composable
fun ProductRegistrationScreen(
    viewModel: MainViewModel,
    productToEdit: Product? = null,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val allProducts by viewModel.products.collectAsState()

    var nome by remember(productToEdit?.id) { mutableStateOf(productToEdit?.nome ?: "") }
    var qtd by remember(productToEdit?.id) { mutableStateOf(productToEdit?.qtd?.toString() ?: "") }
    var custo by remember(productToEdit?.id) { mutableStateOf(productToEdit?.custo ?: "") }
    var custoField by remember(productToEdit?.id) {
        mutableStateOf(
            TextFieldValue(
                text = productToEdit?.custo ?: "",
                selection = TextRange((productToEdit?.custo ?: "").length)
            )
        )
    }
    var margem by remember(productToEdit?.id) { mutableStateOf(productToEdit?.margem?.toString() ?: "") }
    var photoPath by rememberSaveable(productToEdit?.id) {
        mutableStateOf(productToEdit?.photoPath ?: "")
    }
    var stagedPhotoPath by rememberSaveable(productToEdit?.id) { mutableStateOf<String?>(null) }
    var pendingCapturePath by rememberSaveable(productToEdit?.id) { mutableStateOf<String?>(null) }
    var isProcessingPhoto by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }

    var showConflictDialog by remember { mutableStateOf(false) }
    var conflictingProduct by remember { mutableStateOf<Product?>(null) }
    var pendingProduct by remember { mutableStateOf<Product?>(null) }

    val sessionProducts = remember { mutableStateListOf<Product>() }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { captured ->
        val capturePath = pendingCapturePath
        pendingCapturePath = null
        if (captured && !capturePath.isNullOrBlank()) {
            coroutineScope.launch {
                isProcessingPhoto = true
                val persistedPath = ProductImageStorage.persistCapturedPhoto(context, File(capturePath))
                if (!persistedPath.isNullOrBlank()) {
                    stagedPhotoPath?.let { ProductImageStorage.deleteManagedPhoto(context, it) }
                    stagedPhotoPath = persistedPath
                    photoPath = persistedPath
                }
                isProcessingPhoto = false
            }
        } else {
            ProductImageStorage.deleteCameraFile(capturePath)
        }
    }

    fun discardUncommittedPhoto() {
        stagedPhotoPath?.let { ProductImageStorage.deleteManagedPhoto(context, it) }
        stagedPhotoPath = null
        ProductImageStorage.deleteCameraFile(pendingCapturePath)
        pendingCapturePath = null
    }

    fun cancelAndGoBack() {
        discardUncommittedPhoto()
        onBack()
    }

    fun launchCamera() {
        val captureFile = ProductImageStorage.createCameraFile(context)
        pendingCapturePath = captureFile.absolutePath
        val captureUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            captureFile
        )
        runCatching { takePictureLauncher.launch(captureUri) }
            .onFailure {
                ProductImageStorage.deleteCameraFile(pendingCapturePath)
                pendingCapturePath = null
            }
    }

    BackHandler(onBack = ::cancelAndGoBack)

    fun clearForm() {
        nome = ""
        qtd = ""
        custo = ""
        custoField = TextFieldValue("")
        margem = ""
        photoPath = ""
        stagedPhotoPath = null
    }

    fun handleSaveProduct(product: Product) {
        if (isSaving) return
        isSaving = true
        val previousPhotoPath = allProducts.firstOrNull { it.id == product.id }?.photoPath
        viewModel.saveProduct(product, previousPhotoPath) {
            if (stagedPhotoPath == product.photoPath) {
                stagedPhotoPath = null
            }
            isSaving = false
            if (productToEdit == null) {
                sessionProducts.add(0, product)
                coroutineScope.launch { listState.animateScrollToItem(0) }
                clearForm()
            } else {
                onBack()
            }
        }
    }

    val isEnabled = nome.isNotBlank() &&
            qtd.isNotBlank() && (qtd.toIntOrNull() ?: 0) > 0 &&
            custo.isNotBlank() && custo != "R$ 0,00" &&
            margem.isNotBlank()

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
                text = if (productToEdit == null) "Cadastro de Produtos" else "Editar Produto",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                style = LocalTextStyle.current.copy(
                    brush = Brush.horizontalGradient(listOf(PurplePrimary, CyanSecondary))
                )
            )
            IconButton(onClick = ::cancelAndGoBack) {
                Icon(Icons.Default.Close, contentDescription = "Fechar", tint = CyanSecondary)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Surface(
            color = CardBackground.copy(alpha = 0.7f),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, PurplePrimary.copy(alpha = 0.6f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    CustomTextField(
                        value = nome,
                        onValueChange = { nome = it.capitalizeProductName() },
                        label = "Nome do Produto",
                        placeholder = "Produto...",
                        modifier = Modifier.weight(1f)
                    )
                    CustomTextField(
                        value = qtd,
                        onValueChange = {
                            if (it.length <= 3 && (it.isEmpty() || it.all { char -> char.isDigit() })) {
                                qtd = it
                            }
                        },
                        label = "Qtd",
                        placeholder = "0",
                        modifier = Modifier.width(80.dp),
                        keyboardType = KeyboardType.Number
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
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
                                text = "CUSTO",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary
                            )
                        },
                        placeholder = { Text("R$ 0,00", color = TextSecondary.copy(alpha = 0.5f), fontSize = 14.sp) },
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
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                    CustomTextField(
                        value = margem,
                        onValueChange = { margem = it },
                        label = "Margem (%)",
                        placeholder = "0%",
                        modifier = Modifier.weight(1f),
                        keyboardType = KeyboardType.Number
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Foto do produto (opcional)",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(10.dp))

                val interactionSource = remember { MutableInteractionSource() }
                val isPressed by interactionSource.collectIsPressedAsState()
                val buttonScale by animateFloatAsState(if (isPressed) 0.92f else 1f, label = "scale")

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(80.dp)) {
                        ProductPhotoThumbnail(
                            photoPath = photoPath,
                            modifier = Modifier.fillMaxSize(),
                            enableDoubleTapPreview = photoPath.isNotBlank()
                        )
                        if (photoPath.isNotBlank()) {
                            IconButton(
                                onClick = {
                                    if (photoPath == stagedPhotoPath) {
                                        ProductImageStorage.deleteManagedPhoto(context, photoPath)
                                        stagedPhotoPath = null
                                    }
                                    photoPath = ""
                                },
                                enabled = !isProcessingPhoto && !isSaving,
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .size(32.dp)
                            ) {
                                Icon(
                                    Icons.Default.DeleteOutline,
                                    contentDescription = "Remover foto",
                                    tint = Color(0xFFF87171),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (photoPath.isBlank()) {
                            Text("Sem foto", color = TextSecondary, fontSize = 12.sp)
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = ::launchCamera,
                                enabled = !isProcessingPhoto && !isSaving,
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, CyanSecondary)
                            ) {
                                Icon(Icons.Default.PhotoCamera, contentDescription = null, tint = CyanSecondary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Foto", color = Color.White)
                            }

                        }
                    }

                    Surface(
                        onClick = {
                            if (isEnabled && !isProcessingPhoto && !isSaving) {
                                val qtdInt = qtd.toIntOrNull() ?: 0
                                val margemDouble = margem.toDoubleOrNull() ?: 0.0
                                val existing = allProducts.find {
                                    it.nome.equals(nome, ignoreCase = true) && it.id != (productToEdit?.id ?: 0L)
                                }

                                if (productToEdit == null && existing != null) {
                                    conflictingProduct = existing
                                    pendingProduct = Product(
                                        id = existing.id,
                                        nome = nome,
                                        qtd = qtdInt,
                                        custo = custo,
                                        margem = margemDouble,
                                        photoPath = photoPath.ifBlank { existing.photoPath },
                                        precoVenda = "R$ 0,00",
                                        rateio = "R$ 0,00"
                                    )
                                    showConflictDialog = true
                                } else {
                                    val product = Product(
                                        id = productToEdit?.id ?: 0L,
                                        nome = nome,
                                        qtd = qtdInt,
                                        custo = custo,
                                        margem = margemDouble,
                                        precoVenda = productToEdit?.precoVenda ?: "R$ 0,00",
                                        rateio = productToEdit?.rateio ?: "R$ 0,00",
                                        photoPath = photoPath
                                    )
                                    handleSaveProduct(product)
                                }
                            }
                        },
                        enabled = isEnabled && !isProcessingPhoto && !isSaving,
                        modifier = Modifier
                            .size(56.dp)
                            .scale(buttonScale),
                        shape = RoundedCornerShape(14.dp),
                        color = if (isEnabled && !isProcessingPhoto && !isSaving) Color(0xFF10B981) else Color(0xFF10B981).copy(alpha = 0.3f),
                        shadowElevation = if (isEnabled && isPressed) 0.dp else 8.dp,
                        interactionSource = interactionSource
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                if (productToEdit == null) Icons.Default.Add else Icons.Default.Save,
                                contentDescription = null,
                                tint = if (isEnabled && !isProcessingPhoto && !isSaving) Color.White else Color.White.copy(alpha = 0.5f),
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (productToEdit == null) {
            LazyColumn(
                state = listState,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(sessionProducts) { product ->
                    Surface(
                        color = CardBackground.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFF10B981), RoundedCornerShape(12.dp))
                    ) {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                ProductPhotoThumbnail(
                                    photoPath = product.photoPath,
                                    modifier = Modifier.size(52.dp),
                                    enableDoubleTapPreview = product.photoPath.isNotBlank()
                                )

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        product.nome,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        modifier = Modifier.padding(end = 20.dp)
                                    )
                                    Text(
                                        "QTD: ${product.qtd}  |  CUSTO: ${product.custo}  |  MARGEM: ${product.margem}%",
                                        color = TextSecondary,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }
                            }
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = "Produto cadastrado",
                                tint = Color(0xFF10B981),
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(10.dp)
                                    .size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    if (showConflictDialog && conflictingProduct != null && pendingProduct != null) {
        val isEsgotado = conflictingProduct!!.qtd == 0
        val hasDivergence = conflictingProduct!!.custo != pendingProduct!!.custo ||
                conflictingProduct!!.margem != pendingProduct!!.margem

        Dialog(onDismissRequest = { showConflictDialog = false }) {
            Surface(
                color = CardBackground,
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, PurplePrimary.copy(alpha = 0.4f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        if (isEsgotado) Icons.Default.Warning else Icons.Default.Info,
                        contentDescription = null,
                        tint = if (isEsgotado) Color(0xFFFBBF24) else CyanSecondary,
                        modifier = Modifier.size(48.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = if (isEsgotado) "Produto Esgotado!" else "Produto em Estoque",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (isEsgotado) {
                            "Este produto já está cadastrado, porém está esgotado. A quantidade será atualizada no estoque."
                        } else {
                            "Este produto já está cadastrado com a quantidade ${conflictingProduct!!.qtd}. Deseja cancelar o cadastro ou atualizar o valor em estoque?"
                        },
                        color = TextSecondary,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )

                    if (hasDivergence) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Houve divergência nos valores de custo e margem. Qual valor deseja manter?",
                            color = Color(0xFFF87171),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    if (!isEsgotado && !hasDivergence) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Button(
                                onClick = { showConflictDialog = false },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFCA5A5))
                            ) {
                                Text("Cancelar", color = Color(0xFF7F1D1D))
                            }
                            Button(
                                onClick = {
                                    handleSaveProduct(pendingProduct!!)
                                    showConflictDialog = false
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22C55E))
                            ) {
                                Text("Atualizar", color = Color.White)
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = {
                                    val finalProduct = pendingProduct!!.copy(
                                        custo = conflictingProduct!!.custo,
                                        margem = conflictingProduct!!.margem,
                                        photoPath = if (pendingProduct!!.photoPath.isBlank()) conflictingProduct!!.photoPath else pendingProduct!!.photoPath
                                    )
                                    handleSaveProduct(finalProduct)
                                    showConflictDialog = false
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("ESTOQUE", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    Text("${conflictingProduct!!.custo}", fontSize = 12.sp)
                                    Text("${conflictingProduct!!.margem}%", fontSize = 12.sp)
                                }
                            }

                            Button(
                                onClick = {
                                    handleSaveProduct(pendingProduct!!)
                                    showConflictDialog = false
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = CyanSecondary)
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("NOVO", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = DarkBackground)
                                    Text("${pendingProduct!!.custo}", fontSize = 12.sp, color = DarkBackground)
                                    Text("${pendingProduct!!.margem}%", fontSize = 12.sp, color = DarkBackground)
                                }
                            }
                        }

                        if (!isEsgotado) {
                            Spacer(modifier = Modifier.height(12.dp))
                            TextButton(onClick = { showConflictDialog = false }) {
                                Text("Cancelar Cadastro", color = Color(0xFFFCA5A5))
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun String.capitalizeProductName(): String = replaceFirstChar { firstCharacter ->
    if (firstCharacter.isLowerCase()) {
        firstCharacter.titlecase(Locale.getDefault())
    } else {
        firstCharacter.toString()
    }
}
