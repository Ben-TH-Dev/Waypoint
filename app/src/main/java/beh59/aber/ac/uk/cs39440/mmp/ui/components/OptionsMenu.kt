package beh59.aber.ac.uk.cs39440.mmp.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import beh59.aber.ac.uk.cs39440.mmp.R
import beh59.aber.ac.uk.cs39440.mmp.data.models.OptionItem

/**
 * OptionsMenu
 * Creates a dropdown menu with customizable options, accessible via an IconButton
 * @param options List of OptionItem objects to populate the menu with
 * @param modifier Lets you customise the layout and appearance of the OptionsMenu where you call it
 */
@Composable
fun OptionsMenu(
    options: List<OptionItem>,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        IconButton(
            onClick = { expanded = true },
            iconId = R.drawable.more_vert_24px,
            contentDescription = "Options"
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.text) },
                    onClick = {
                        option.onClick()
                        expanded = false
                    }
                )
            }
        }
    }
} 