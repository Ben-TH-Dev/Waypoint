package beh59.aber.ac.uk.cs39440.mmp.ui.projects

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import beh59.aber.ac.uk.cs39440.mmp.data.models.Project
import beh59.aber.ac.uk.cs39440.mmp.ui.components.projects.JobItem
import beh59.aber.ac.uk.cs39440.mmp.ui.components.projects.ProgressBar
import beh59.aber.ac.uk.cs39440.mmp.utils.JobStatus

/**
 * ProjectJobsTab
 * Shows information about each job in a project in an efficient LazyColumn
 * @param project The project being viewed
 * @param projectsViewModel ViewModel containing project related data and operations
 * @param navController Handles navigation between screens
 */
@Composable
fun ProjectJobsTab(
    project: Project,
    projectsViewModel: ProjectViewModel,
    navController: NavController
) {
    val uiState by projectsViewModel.uiState.collectAsState()
    val jobs = uiState.jobs.filter { it.projectID == project.projectID }
    val isModerator = project.role == "moderator"

    if (!isModerator) {
        if (jobs.isNotEmpty()) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Total jobs: ${jobs.count()}",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Text(
                            text = "Completion:",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        if (jobs.isNotEmpty()) {
                            val completedJobs = jobs.count { it.status == JobStatus.COMPLETE }
                            val completionPercentage = completedJobs.toFloat() / jobs.size
                            ProgressBar(completionPercentage)
                        }
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    items(jobs) { job ->
                        JobItem(
                            job = job,
                            onClick = { navController.navigate("JobDetailScreen/${job.jobID}") })
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "No jobs yet for this project.",
                    textAlign = TextAlign.Center
                )
            }
        }
    } else {
        if (jobs.isNotEmpty()) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Total jobs: ${jobs.count()}",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Text(
                            text = "Completion:",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        if (jobs.isNotEmpty()) {
                            val completedJobs = jobs.count { it.status == JobStatus.COMPLETE }
                            val completionPercentage = completedJobs.toFloat() / jobs.size
                            ProgressBar(completionPercentage)
                        }
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    items(jobs) { job ->
                        JobItem(
                            job = job,
                            onClick = { navController.navigate("JobDetailScreen/${job.jobID}") })
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "No jobs yet for this project. Add some new members using the FAB button.",
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}