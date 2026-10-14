package com.example.nexusbusiness.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Swipe
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.nexusbusiness.security.AppSecurityManager
import com.example.nexusbusiness.ui.theme.CardBackground
import com.example.nexusbusiness.ui.theme.CyanSecondary
import com.example.nexusbusiness.ui.theme.PurplePrimary
import com.example.nexusbusiness.ui.theme.TextPrimary
import com.example.nexusbusiness.ui.theme.TextSecondary

private data class AppInformation(val label: String, val value: String)

@Composable
fun SettingsScreen(
    securityManager: AppSecurityManager,
    screenSlideEnabled: Boolean,
    onScreenSlideEnabledChange: (Boolean) -> Unit
) {
    var showAboutDialog by remember { mutableStateOf(false) }
    var securityEnabled by remember { mutableStateOf(securityManager.isPasswordEnabled()) }
    var securitySwitchChecked by remember { mutableStateOf(securityEnabled) }
    var newPassword by remember { mutableStateOf("") }
    var newPasswordVisible by remember { mutableStateOf(false) }
    var showDisableDialog by remember { mutableStateOf(false) }
    var feedbackMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = null,
                tint = Color(0xFFFB923C),
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Ajustes",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                style = LocalTextStyle.current.copy(
                    brush = Brush.horizontalGradient(
                        listOf(PurplePrimary, CyanSecondary)
                    )
                )
            )
        }
        Spacer(modifier = Modifier.height(16.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = CardBackground,
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, PurplePrimary.copy(alpha = 0.55f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = Color(0xFFFB923C),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Segurança",
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                text = if (securityEnabled) {
                                    "Senha exigida ao iniciar o aplicativo"
                                } else {
                                    "Proteja o acesso ao aplicativo com uma senha"
                                },
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }
                    Switch(
                        checked = securitySwitchChecked,
                        onCheckedChange = { checked ->
                            if (checked) {
                                securitySwitchChecked = true
                                newPassword = ""
                            } else if (securityEnabled) {
                                showDisableDialog = true
                            } else {
                                securitySwitchChecked = false
                                newPassword = ""
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = CyanSecondary,
                            uncheckedThumbColor = TextSecondary,
                            uncheckedTrackColor = Color.Transparent,
                            uncheckedBorderColor = TextSecondary
                        )
                    )
                }

                if (securitySwitchChecked && !securityEnabled) {
                    Spacer(modifier = Modifier.height(16.dp))
                    PasswordField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        passwordVisible = newPasswordVisible,
                        onVisibilityChange = { newPasswordVisible = !newPasswordVisible },
                        label = "Nova senha"
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "A senha deve possuir pelo menos 6 caracteres.",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            if (newPassword.isBlank() ||
                                newPassword.length < AppSecurityManager.MIN_PASSWORD_LENGTH
                            ) {
                                feedbackMessage =
                                    "A senha mínima deverá ter pelo menos 6 caracteres."
                            } else {
                                securityManager.savePassword(newPassword)
                                securityEnabled = true
                                securitySwitchChecked = true
                                newPassword = ""
                                newPasswordVisible = false
                                feedbackMessage = "Senha de inicialização ativada com sucesso."
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Gravar senha", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = CardBackground,
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, PurplePrimary.copy(alpha = 0.55f))
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Swipe,
                        contentDescription = null,
                        tint = Color(0xFFFB923C),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Slide de Tela",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = if (screenSlideEnabled) {
                                "Deslize para alternar entre as telas"
                            } else {
                                "Use o Menu de Rodapé para mudar de tela"
                            },
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }
                Switch(
                    checked = screenSlideEnabled,
                    onCheckedChange = onScreenSlideEnabledChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = CyanSecondary,
                        uncheckedThumbColor = TextSecondary,
                        uncheckedTrackColor = Color.Transparent,
                        uncheckedBorderColor = TextSecondary
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        OutlinedButton(
            onClick = { showAboutDialog = true },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, PurplePrimary)
        ) {
            Icon(Icons.Default.Info, contentDescription = null, tint = CyanSecondary)
            Spacer(modifier = Modifier.width(10.dp))
            Text("Sobre o App", color = TextPrimary, fontWeight = FontWeight.SemiBold)
        }
    }

    if (showAboutDialog) {
        AboutAppDialog(onDismiss = { showAboutDialog = false })
    }

    if (showDisableDialog) {
        DisableSecurityDialog(
            onDismiss = { showDisableDialog = false },
            onConfirm = { password ->
                if (securityManager.disablePassword(password)) {
                    securityEnabled = false
                    securitySwitchChecked = false
                    showDisableDialog = false
                    feedbackMessage = "Senha de inicialização desativada."
                    true
                } else {
                    false
                }
            }
        )
    }

    feedbackMessage?.let { message ->
        AlertDialog(
            onDismissRequest = { feedbackMessage = null },
            title = { Text("Segurança") },
            text = { Text(message) },
            confirmButton = {
                Button(onClick = { feedbackMessage = null }) {
                    Text("OK")
                }
            },
            containerColor = CardBackground,
            titleContentColor = CyanSecondary,
            textContentColor = TextPrimary
        )
    }
}

@Composable
private fun PasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    passwordVisible: Boolean,
    onVisibilityChange: () -> Unit,
    label: String
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        visualTransformation = if (passwordVisible) {
            VisualTransformation.None
        } else {
            PasswordVisualTransformation()
        },
        trailingIcon = {
            IconButton(onClick = onVisibilityChange) {
                Icon(
                    imageVector = if (passwordVisible) {
                        Icons.Default.VisibilityOff
                    } else {
                        Icons.Default.Visibility
                    },
                    contentDescription = if (passwordVisible) {
                        "Ocultar senha"
                    } else {
                        "Mostrar senha"
                    }
                )
            }
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = CyanSecondary,
            focusedLabelColor = CyanSecondary,
            cursorColor = CyanSecondary,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary
        )
    )
}

@Composable
private fun DisableSecurityDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Boolean
) {
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var invalidPassword by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Desativar segurança") },
        text = {
            Column {
                Text(
                    text = "Informe a senha atual para desligar a proteção do aplicativo.",
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(12.dp))
                PasswordField(
                    value = password,
                    onValueChange = {
                        password = it
                        invalidPassword = false
                    },
                    passwordVisible = passwordVisible,
                    onVisibilityChange = { passwordVisible = !passwordVisible },
                    label = "Senha atual"
                )
                if (invalidPassword) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Senha incorreta. A segurança permanece ativada.",
                        color = Color(0xFFF87171),
                        fontSize = 12.sp
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (!onConfirm(password)) {
                        invalidPassword = true
                        password = ""
                    }
                },
                enabled = password.isNotBlank()
            ) {
                Text("Desativar")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        },
        containerColor = CardBackground,
        titleContentColor = CyanSecondary,
        textContentColor = TextPrimary
    )
}

@Composable
private fun AboutAppDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val version = remember(context) {
        runCatching {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName
        }.getOrNull() ?: "1.0"
    }
    val information = remember(version) {
        listOf(
            AppInformation("Aplicativo", "NexusBusiness"),
            AppInformation("Versão", version),
            AppInformation("Plataforma", "Android nativo"),
            AppInformation("Desenvolvimento", "Kotlin"),
            AppInformation("Interface Gráfica", "Jetpack Compose / Material 3"),
            AppInformation("Arquitetura", "MVVM + Repository"),
            AppInformation("Banco de dados", "Room com banco SQLite local"),
            AppInformation("Navegação", "Navigation Compose"),
            AppInformation("Funcionamento", "Offline"),
            AppInformation("Android mínimo", "Android 7.0 / API 24"),
            AppInformation("Desenvolvedor", "Lucas Pires Franco")
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.90f)
                .fillMaxHeight(0.86f)
                .padding(vertical = 24.dp),
            color = CardBackground,
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, PurplePrimary.copy(alpha = 0.65f))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Sobre o App",
                        color = CyanSecondary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Fechar", tint = Color.White)
                    }
                }

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(information) { item ->
                        Column {
                            Text(
                                text = item.label,
                                color = TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = item.value,
                                color = TextPrimary,
                                fontSize = 15.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Fechar", color = Color.White)
                }
            }
        }
    }
}
