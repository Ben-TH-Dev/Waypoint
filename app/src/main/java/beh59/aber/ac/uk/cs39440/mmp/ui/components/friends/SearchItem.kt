package beh59.aber.ac.uk.cs39440.mmp.ui.components.friends

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import beh59.aber.ac.uk.cs39440.mmp.R
import beh59.aber.ac.uk.cs39440.mmp.data.models.User
import beh59.aber.ac.uk.cs39440.mmp.ui.components.IconButton
import beh59.aber.ac.uk.cs39440.mmp.ui.friends.FriendViewModel
import beh59.aber.ac.uk.cs39440.mmp.ui.theme.outlineLight
import coil.compose.AsyncImage

/**
 * SearchItem
 * UI component displayed when searching for users to add as friends
 * @param user The user data to display in the search result
 * @param friendViewModel ViewModel that handles friend related data and operations
 * @param navController Handles navigation between screens
 */
@Composable
fun SearchItem(user: User, friendViewModel: FriendViewModel, navController: NavController) {
    val displayName = user.displayName
    val username = user.username
    val photourl = user.photourl

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(90.dp)
            .clickable {},
        shape = RoundedCornerShape(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
            ) {
                AsyncImage(
                    model = photourl,
                    contentDescription = "Profile Picture",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Column(
                modifier = Modifier
                    .padding(4.dp)
                    .weight(1f)
            ) {
                Text(
                    text = displayName,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = username,
                    style = MaterialTheme.typography.bodyMedium,
                    color = outlineLight
                )
            }

            IconButton(
                onClick = {
                    friendViewModel.sendFriendRequest(user.uid)
                    navController.navigate("Friends")
                },
                iconId = R.drawable.filled_person_add_24px,
                contentDescription = "Add User as Friend"
            )
        }
    }
}