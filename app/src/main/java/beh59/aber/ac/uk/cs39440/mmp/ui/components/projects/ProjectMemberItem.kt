package beh59.aber.ac.uk.cs39440.mmp.ui.components.projects

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import beh59.aber.ac.uk.cs39440.mmp.R
import beh59.aber.ac.uk.cs39440.mmp.data.models.Friend
import coil.compose.AsyncImage

/**
 * ProjectMemberItem
 * Displays information about a friend that is a member of a project
 * @param friend The friend to display as a project member
 * @param isSelected Whether this member is currently selected
 * @param onClick Callback that executes when the member item is clicked
 */
@Composable
fun ProjectMemberItem(
    friend: Friend,
    isSelected: Boolean = false,
    onClick: () -> Unit
) {
    val backgroundColor = when (isSelected) {
        true -> MaterialTheme.colorScheme.outlineVariant
        false -> MaterialTheme.colorScheme.surface
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .background(backgroundColor),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
            ) {
                if (friend.photourl.isNotEmpty()) {
                    AsyncImage(
                        model = friend.photourl,
                        contentDescription = "Profile Picture",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        placeholder = painterResource(R.drawable.generic_avatar)
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.generic_avatar),
                        contentDescription = "Profile Picture",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = friend.displayName,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Preview
@Composable
fun ProjectMemberItemPreview() {
    val fakeFriend =
        Friend(uid = "test", "test", "test", "test", "test", "test", "test")

    ProjectMemberItem(fakeFriend, true, onClick = {})
}