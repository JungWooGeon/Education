package com.pass.presentation.view.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@Composable
fun EditNameDialog(
    onDismissRequest: () -> Unit,
    textFieldValue: String,
    onValueChange: (String) -> Unit,
    onClickSaveEditDialogButton: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.4f))  // 배경을 반투명한 검정색으로 설정
            .clickable { onDismissRequest() }
    )

    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
            ),
        ) {
            Column {
                CodeBridgeTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    value = textFieldValue,
                    onChangeValue = onValueChange,
                    placeHolderValue = "닉네임을 입력해주세요.",
                    containerColor = Color.White
                )

                Button(
                    onClick = onClickSaveEditDialogButton,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(text = "저장")
                }
            }
        }
    }
}