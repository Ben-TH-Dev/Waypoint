package beh59.aber.ac.uk.cs39440.mmp.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import beh59.aber.ac.uk.cs39440.mmp.R

/**
 * ChatBox
 * Input field component for sending messages in the chat systems in the application
 * @param modifier Lets you customise the layout and appearance of the ChatBox where you call it
 * @param onSend Callback that executes when a message is sent, passing the message text as
 * parameter
 */
@Composable
fun ChatBox(
    modifier: Modifier = Modifier,
    onSend: (String) -> Unit
) {
    val (text, setText) = remember { mutableStateOf("") }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .padding(bottom = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.Bottom
    ) {
        OutlinedTextField(
            value = text,
            onValueChange = setText,
            modifier = Modifier
                .weight(1f)
                .height(56.dp),
            label = { Text("Type a message") },
            singleLine = true,
            shape = RoundedCornerShape(18.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        IconButton(
            onClick = {
                if (text.isNotBlank()) {
                    onSend(text)
                    setText("")
                }
            },
            iconId = R.drawable.send_24px,
            contentDescription = "Send Button",
            modifier = Modifier.padding(bottom = 0.dp)
        )
    }
}

@Preview
@Composable
fun ChatBoxPreview() {
    ChatBox(modifier = Modifier, onSend = {})
}