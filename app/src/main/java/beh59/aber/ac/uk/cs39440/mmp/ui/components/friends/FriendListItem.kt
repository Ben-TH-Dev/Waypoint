package beh59.aber.ac.uk.cs39440.mmp.ui.components.friends

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import beh59.aber.ac.uk.cs39440.mmp.data.models.Friend

/**
 * FriendListItem
 * A UI component that displays each friend on the Friends screen. Specialised for list view
 * @param friend The friend data to display in this item
 * @param navController Handles navigation between screens
 */
@Composable
fun FriendListItem(friend: Friend, navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { navController.navigate("FriendProfile/${friend.uid}") }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = friend.displayName,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.weight(1f))
        }

        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
    }
}