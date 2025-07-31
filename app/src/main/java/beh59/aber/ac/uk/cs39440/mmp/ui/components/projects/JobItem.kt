package beh59.aber.ac.uk.cs39440.mmp.ui.components.projects

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import beh59.aber.ac.uk.cs39440.mmp.data.models.Job
import beh59.aber.ac.uk.cs39440.mmp.utils.JobPriority
import beh59.aber.ac.uk.cs39440.mmp.utils.JobStatus
import beh59.aber.ac.uk.cs39440.mmp.utils.customBlue
import beh59.aber.ac.uk.cs39440.mmp.utils.customGreen
import beh59.aber.ac.uk.cs39440.mmp.utils.customOrange
import beh59.aber.ac.uk.cs39440.mmp.utils.customRed
import beh59.aber.ac.uk.cs39440.mmp.utils.formatTimeAgo

/**
 * JobItem
 * Displays information about a job in a project and allows the user to select it for further
 * information and management
 * @param job The job to display
 * @param onClick Callback that executes when the job item is clicked
 */
@Composable
fun JobItem(job: Job, onClick: () -> Unit) {
    val convertedDate = formatTimeAgo(job.deadline, true)

    Card(
        modifier = Modifier
            .clickable { onClick() }
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = job.jobTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "View Task Details"
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = job.jobDescription,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            /*if (task.imageRes != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Image(
                    painter = painterResource(id = task.imageRes),
                    contentDescription = task.title,
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            }*/

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                //Shows if the job has been assigned or not
                if (job.assignedUserUID.isEmpty()) {
                    JobStatusAlerts(
                        text = "Unassigned",
                        colour = customBlue
                    )

                    Spacer(modifier = Modifier.width(8.dp))
                }

                //Displays the priority of the job
                JobStatusAlerts(
                    text = when (job.priority) {
                        JobPriority.LOW -> "Low Priority"
                        JobPriority.MEDIUM -> "Medium Priority"
                        JobPriority.HIGH -> "High Priority"
                    },
                    colour = when (job.priority) {
                        JobPriority.LOW -> customGreen
                        JobPriority.MEDIUM -> customOrange
                        JobPriority.HIGH -> customRed
                    }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                //Displays the completion status of the job
                JobStatusAlerts(
                    text = when (job.status) {
                        JobStatus.TO_DO -> "To-Do"
                        JobStatus.IN_PROGRESS -> "In-Progress"
                        JobStatus.COMPLETE -> "Complete"
                    },
                    colour = when (job.status) {
                        JobStatus.TO_DO -> customRed
                        JobStatus.IN_PROGRESS -> customOrange
                        JobStatus.COMPLETE -> customGreen
                    }
                )

                Spacer(modifier = Modifier.width(8.dp))

                //Displays job deadlines
                JobStatusAlerts(
                    text = convertedDate,
                    colour = when {
                        convertedDate.contains("min") -> {
                            customRed
                        }

                        convertedDate.contains("hr") -> {
                            customOrange
                        }

                        convertedDate.contains("d") -> {
                            customOrange
                        }

                        convertedDate.contains("m") -> {
                            customGreen
                        }

                        else -> Color.Gray
                    }
                )
            }
        }
    }
}