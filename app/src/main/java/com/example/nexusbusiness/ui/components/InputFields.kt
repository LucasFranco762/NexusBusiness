package com.example.nexusbusiness.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nexusbusiness.ui.theme.CyanSecondary
import com.example.nexusbusiness.ui.theme.PurplePrimary
import com.example.nexusbusiness.ui.theme.TextSecondary

@Composable
fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { 
            Text(
                text = label.uppercase(), 
                fontSize = 10.sp, 
                fontWeight = FontWeight.Bold,
                color = TextSecondary
            ) 
        },
        placeholder = { Text(placeholder, color = TextSecondary.copy(alpha = 0.5f), fontSize = 14.sp) },
        modifier = modifier.fillMaxWidth().padding(top = 4.dp),
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
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        singleLine = true
    )
}
