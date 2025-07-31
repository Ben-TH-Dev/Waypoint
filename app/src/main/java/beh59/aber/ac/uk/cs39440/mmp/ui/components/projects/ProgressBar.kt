package beh59.aber.ac.uk.cs39440.mmp.ui.components.projects

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

/**
 * ProgressBar
 * A UI component that shows progress visually
 * @param progress How much of the bar is full
 */
@Composable
fun ProgressBar(progress: Float) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(15.dp))
            .width(300.dp)
            .height(15.dp)
    ) {
        HorizontalDivider(
            modifier = Modifier
                .fillMaxWidth(),
            color = MaterialTheme.colorScheme.outlineVariant,
            thickness = 15.dp
        )

        HorizontalDivider(
            modifier = Modifier
                .width(progress.dp)
                .height(4.dp),
            color = MaterialTheme.colorScheme.primary,
            thickness = 15.dp
        )
    }
}