package com.fediim.translatio.common

import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun OAuthButton(provider: String, onClick: () -> Unit) {
    OutlinedButton(onClick = onClick) { Text(provider) }
}

@Composable
fun LoginButton(onClick: () -> Unit = {}) {
    Button(onClick = onClick) { Text("Login") }
}

@Composable
fun RegisterButton(onClick: () -> Unit = {}) {
    Button(onClick = onClick) { Text("Register") }
}

@Composable
@Preview
private fun OAuthButtonPreview() {
    OAuthButton("GitHub") { }
}

@Composable
@Preview
private fun LoginButtonPreview() {
    LoginButton { }
}

@Composable
@Preview
private fun RegisterButtonPreview() {
    RegisterButton { }
}
