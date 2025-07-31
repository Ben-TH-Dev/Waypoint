package beh59.aber.ac.uk.cs39440.mmp.ui.other

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import beh59.aber.ac.uk.cs39440.mmp.R
import beh59.aber.ac.uk.cs39440.mmp.ui.components.auth.GoogleSignInButton
import beh59.aber.ac.uk.cs39440.mmp.ui.viewmodel.UserViewModel

/**
 * SplashPage
 * The very first page a new user sees and sometimes existing users. Simply introduces the
 * application and its intent and allows the user to initiate the sign-in with Google process
 * @param userViewModel Contains user related data and operations
 * @param onSignInClick Callback that lets you define custom behaviour where SplashPage is called
 */
@Composable
fun SplashPage(
    userViewModel: UserViewModel,
    onSignInClick: () -> Unit = {}
) {
    val primaryColor = MaterialTheme.colorScheme.primary

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .width(130.dp)
                    .height(130.dp)
                    .padding(top = 15.dp)
            ) {
                Canvas(
                    modifier = Modifier.matchParentSize()
                ) {
                    drawCircle(
                        color = primaryColor,
                        radius = size.minDimension / 2
                    )
                }

                Image(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .requiredSize(80.dp),
                    painter = painterResource(
                        id = R.drawable.filled_location_on_24px
                    ),
                    contentDescription = "Search Icon"
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.waypointTitle),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.waypointDesc),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
        }

        Column(
            modifier = Modifier.align(Alignment.BottomCenter),
        ) {
            GoogleSignInButton(
                onClick = onSignInClick,
                userViewModel
            )

            Spacer(modifier = Modifier.height(64.dp))
        }
    }
}