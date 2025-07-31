package beh59.aber.ac.uk.cs39440.mmp.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import beh59.aber.ac.uk.cs39440.mmp.data.models.Message

/**
 * ChatItem
 * Displays a single message in a conversation
 * @param message The Message object containing the relevant data
 * @param isFromCurrentUser Boolean indicating if the message is from the current user
 */
@Composable
fun ChatItem(
    message: Message,
    isFromCurrentUser: Boolean = false
) {
    //Remembers the colours used on the page which may be liable to change if the user edits their
    //phone's theme.
    val primaryColour = MaterialTheme.colorScheme.primary
    val secondaryColour = MaterialTheme.colorScheme.secondary
    val onPrimary = MaterialTheme.colorScheme.onPrimary
    val onSecondary = MaterialTheme.colorScheme.onSecondary

    //These three subscribe to changes in isFromCurrentUser and updates value based on new value.
    val contentAlignment = remember(isFromCurrentUser) {
        if (isFromCurrentUser) Alignment.CenterEnd else Alignment.CenterStart
    }
    val containerColor = remember(isFromCurrentUser) {
        if (isFromCurrentUser) primaryColour else secondaryColour
    }
    val textColor = remember(isFromCurrentUser) {
        if (isFromCurrentUser) onPrimary else onSecondary
    }

    Box(
        contentAlignment = contentAlignment,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Card(
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(
                containerColor = containerColor
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 2.dp
            ),
            modifier = Modifier
                .padding(4.dp)
        ) {
            Text(
                text = message.content,
                color = textColor,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Start,
                overflow = TextOverflow.Visible,
                modifier = Modifier.padding(12.dp)
            )
        }
    }
}