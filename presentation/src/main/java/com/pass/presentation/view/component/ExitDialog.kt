package com.pass.presentation.view.component

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

@Composable
fun ExitDialog(
    exitTitle: String,
    onDismissRequest: () -> Unit,
    onExitRequest: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        text = { Text(text = exitTitle) },
        confirmButton = {
            TextButton(
                onClick = onExitRequest
            ) {
                Text("종료")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismissRequest
            ) {
                Text("취소")
            }
        }
    )
}