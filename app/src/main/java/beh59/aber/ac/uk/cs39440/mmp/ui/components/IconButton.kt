package beh59.aber.ac.uk.cs39440.mmp.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import beh59.aber.ac.uk.cs39440.mmp.R

/**
 * IconButton
 * A circular button with an icon
 * @param onClick Callback that executes when the button is clicked
 * @param iconId The icon to display in the button
 * @param contentDescription Description of the icon
 * @param modifier Lets you customise the layout and appearance of the IconButton where you call it
 */
@Composable
fun IconButton(
    onClick: () -> Unit,
    iconId: Int,
    contentDescription: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
    ) {
        Button(
            onClick = { onClick() },
            shape = CircleShape,
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 6.dp,
                pressedElevation = 2.dp,
                hoveredElevation = 8.dp
            ),
            contentPadding = PaddingValues(0.dp),
            modifier = Modifier
                .width(50.dp)
                .height(50.dp)
                .align(Alignment.Center)
        ) {
            Icon(
                painter = painterResource(
                    id = iconId,
                ),
                contentDescription = contentDescription
            )
        }
    }
}

@Preview
@Composable
fun IconButtonPreview() {
    IconButton(
        onClick = {},
        iconId = R.drawable.filled_location_on_24px,
        contentDescription = "Location On Icon"
    )
}