package com.example.nexusbusiness.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nexusbusiness.security.AppSecurityManager
import com.example.nexusbusiness.security.AuthenticationResult
import com.example.nexusbusiness.ui.theme.CardBackground
import com.example.nexusbusiness.ui.theme.CyanSecondary
import com.example.nexusbusiness.ui.theme.DarkBackground
import com.example.nexusbusiness.ui.theme.PurplePrimary
import com.example.nexusbusiness.ui.theme.TextPrimary
import com.example.nexusbusiness.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import kotlin.math.ceil

@Composable
fun AppLockScreen(
    securityManager: AppSecurityManager,
    onUnlocked: () -> Unit
) {
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var currentTimeMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var securityStatus by remember { mutableStateOf(securityManager.getStatus(currentTimeMillis)) }
    var message by remember { mutableStateOf<String?>(null) }

    val lockedUntil = securityStatus.lockedUntilMillis
    val isLocked = lockedUntil != null

    BackHandler(enabled = true) {}

    LaunchedEffect(lockedUntil) {
        while (lockedUntil != null) {
            currentTimeMillis = System.currentTimeMillis()
            securityStatus = securityManager.getStatus(currentTimeMillis)
            if (!securityStatus.isLocked) {
                message = "Bloqueio encerrado. Você possui 5 novas tentativas."
                break
            }
            delay(1_000)
        }
    }

    fun authenticate() {
        if (password.isBlank() || isLocked) return
        when (val result = securityManager.authenticate(password)) {
            AuthenticationResult.Success -> onUnlocked()
            is AuthenticationResult.InvalidPassword -> {
                password = ""
                securityStatus = securityManager.getStatus()
                message = "Senha incorreta. Restam ${result.remainingAttempts} tentativa(s)."
            }
            is AuthenticationResult.Locked -> {
                password = ""
                securityStatus = securityManager.getStatus()
                message = "Limite de tentativas atingido."
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = CardBackground,
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, PurplePrimary.copy(alpha = 0.65f)),
            shadowElevation = 12.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = Color(0xFFFB923C),
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "NexusBusiness",
                        fontSize = 25.sp,
                        fontWeight = FontWeight.Bold,
                        style = LocalTextStyle.current.copy(
                            brush = Brush.horizontalGradient(
                                listOf(PurplePrimary, CyanSecondary)
                            )
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = if (isLocked) "Aplicativo temporariamente bloqueado" else "Informe sua senha para continuar",
                    color = TextSecondary,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    enabled = !isLocked,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Senha") },
                    singleLine = true,
                    visualTransformation = if (passwordVisible) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(onDone = { authenticate() }),
                    trailingIcon = {
                        IconButton(
                            onClick = { passwordVisible = !passwordVisible },
                            enabled = !isLocked
                        ) {
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
                        unfocusedTextColor = TextPrimary,
                        disabledTextColor = TextSecondary
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))
                if (isLocked) {
                    Text(
                        text = "Tente novamente em ${formatRemainingTime(lockedUntil!! - currentTimeMillis)}",
                        color = Color(0xFFF87171),
                        fontWeight = FontWeight.SemiBold
                    )
                } else {
                    Text(
                        text = "Tentativas disponíveis: ${securityStatus.remainingAttempts}",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }

                message?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = it,
                        color = if (isLocked) Color(0xFFF87171) else CyanSecondary,
                        fontSize = 13.sp
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = ::authenticate,
                    enabled = password.isNotBlank() && !isLocked,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Desbloquear", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

private fun formatRemainingTime(remainingMillis: Long): String {
    val totalSeconds = ceil(remainingMillis.coerceAtLeast(0L) / 1_000.0).toLong()
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d".format(minutes, seconds)
}
