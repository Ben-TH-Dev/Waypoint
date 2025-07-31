package beh59.aber.ac.uk.cs39440.mmp.ui.components.projects

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * JobStatusAlerts
 * Displays a colour coded card with text for displaying job priority and status
 * @param text The text content to display in the card
 * @param colour The background color of the card to indicate status or priority type
 */
@Composable
fun JobStatusAlerts(
    text: String,
    colour: Color,
) {
    if (text.isNotEmpty()) {
        Card(
            colors = CardColors(
                containerColor = colour,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                disabledContainerColor = Color.Transparent,
                disabledContentColor = Color.Transparent
            )
        ) {
            Text(
                text = text,
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}