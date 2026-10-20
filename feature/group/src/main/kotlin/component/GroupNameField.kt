package com.mkulikov.taskb.feature.group.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import com.mkulikov.taskb.ui.icon.TaskBIcons

@Composable
internal fun GroupNameField(
    name: String,
    onNameChange: (String) -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = name,
        onValueChange = onNameChange,
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
        placeholder = { Text("Enter group name") },
        trailingIcon = {
            if (name.isNotEmpty()) {
                IconButton(onClick = { onNameChange("") }) {
                    Icon(TaskBIcons.Clear, contentDescription = "Clear name")
                }
            }
        },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done, capitalization = KeyboardCapitalization.Sentences),
        keyboardActions = KeyboardActions(onDone = { onSave() }),
    )
}
