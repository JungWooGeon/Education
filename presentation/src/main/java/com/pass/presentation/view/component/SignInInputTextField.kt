package com.pass.presentation.view.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignInInputTextField(
    modifier: Modifier,
    value: String,
    onChangeValue: (String) -> Unit,
    placeHolderValue: String,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer
) {
    TextField(
        modifier = modifier.fillMaxWidth(),
        value = value,
        onValueChange = {
            onChangeValue(it)
        },
        placeholder = {
            Text(text = placeHolderValue)
        },
        visualTransformation = visualTransformation,
        shape = RoundedCornerShape(8.dp),
        colors = TextFieldDefaults.textFieldColors(
            containerColor = containerColor,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent
        )
    )
}