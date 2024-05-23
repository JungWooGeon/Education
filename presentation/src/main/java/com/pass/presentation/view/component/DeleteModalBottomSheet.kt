package com.pass.presentation.view.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeleteModalBottomSheet(
    modifier: Modifier = Modifier,
    closeBottomSheet: () -> Unit,
    onClickDeleteItem: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = closeBottomSheet,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Row(
            modifier = modifier
                .padding(bottom = 30.dp, start = 30.dp, end = 30.dp)
                .clickable { onClickDeleteItem() }
        ) {
            Icon(imageVector = Icons.Default.Delete, contentDescription = "삭제")
            Spacer(modifier = Modifier.size(10.dp))
            Text(text = "삭제")
        }
    }
}