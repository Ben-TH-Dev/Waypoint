package beh59.aber.ac.uk.cs39440.mmp.ui.projects

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import beh59.aber.ac.uk.cs39440.mmp.data.models.Job
import beh59.aber.ac.uk.cs39440.mmp.data.models.OptionItem
import beh59.aber.ac.uk.cs39440.mmp.data.models.Project
import beh59.aber.ac.uk.cs39440.mmp.ui.components.OptionsMenu
import beh59.aber.ac.uk.cs39440.mmp.ui.components.projects.DeleteJobDialog
import beh59.aber.ac.uk.cs39440.mmp.ui.components.projects.JobStatusAlerts
import beh59.aber.ac.uk.cs39440.mmp.utils.JobPriority
import beh59.aber.ac.uk.cs39440.mmp.utils.JobStatus
import beh59.aber.ac.uk.cs39440.mmp.utils.customBlue
import beh59.aber.ac.uk.cs39440.mmp.utils.customGreen
import beh59.aber.ac.uk.cs39440.mmp.utils.customOrange
import beh59.aber.ac.uk.cs39440.mmp.utils.customRed
import beh59.aber.ac.uk.cs39440.mmp.utils.formatTimeAgo
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * JobDetail
 * Shows information about a job on the ProjectJobs screen and tries to convey the information
 * quickly and clearly using colour coding, cards and modern design
 * @param job The job to display information about
 * @param navController Handles navigation between screens
 * @param projectViewModel Contains project related data and operations
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobDetail(
    job: Job,
    navController: NavController,
    projectViewModel: ProjectViewModel
) {
    //Observes the exposed UI state
    val uiState by projectViewModel.uiState.collectAsState()
    //Finds the current project by comparing the job's projectID field in memory with each project's
    //projectID in memory
    val project = uiState.projects.find { it.projectID == job.projectID } ?: return
    val isModerator = project.role == "moderator"

    var showDeleteJobDialog by remember { mutableStateOf(false) }
    var isEditing by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = job.jobTitle,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    if (isModerator) {
                        OptionsMenu(
                            options = listOf(
                                OptionItem(
                                    text = "Edit job",
                                    onClick = { isEditing = true }
                                ),
                                OptionItem(
                                    text = "Delete job",
                                    onClick = { showDeleteJobDialog = true }
                                )
                            )
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
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
                    if (!isEditing) {
                        JobDetails(project, job, projectViewModel)
                    } else {
                        JobEdit(project, job, projectViewModel, navController)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (!isModerator) {
                if (job.assignedUserUID.isNotEmpty()) {
                    val assignedMember = project.members.find { it.uid == job.assignedUserUID }
                    val memberName = assignedMember?.displayName ?: "Unknown Member"

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Assigned Member",
                                tint = MaterialTheme.colorScheme.primary
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Assigned to: $memberName",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                } else {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            JobStatusAlerts(
                                text = "Unassigned",
                                colour = customBlue
                            )
                        }
                    }
                }
            }
        }

        if (showDeleteJobDialog) {
            DeleteJobDialog(
                job,
                onDismiss = { showDeleteJobDialog = false },
                onConfirm = {
                    projectViewModel.deleteJob(job)
                    navController.navigate("ProjectDetail/${project.projectID}")
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobDetails(project: Project, job: Job, projectViewModel: ProjectViewModel) {
    //Available members that can be assigned to this job with a simple validation check
    val availableMembers = project.members.filter { it.uid.isNotEmpty() }

    var showAssignMemberDropdown by remember { mutableStateOf(false) }
    var selectedMemberID by remember { mutableStateOf("") }

    Text(
        text = "Project: ${project.title}",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    Spacer(modifier = Modifier.height(16.dp))

    Text(
        text = "Description",
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold
    )

    Spacer(modifier = Modifier.height(4.dp))

    Text(
        text = job.jobDescription,
        style = MaterialTheme.typography.bodyLarge
    )

    Spacer(modifier = Modifier.height(16.dp))

    Text(
        text = "Job Status",
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold
    )

    Spacer(modifier = Modifier.height(8.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
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

        JobStatusAlerts(
            text = when (job.status) {
                JobStatus.TO_DO -> "To-Do"
                JobStatus.IN_PROGRESS -> "In-Progress"
                JobStatus.COMPLETE -> "Complete"
            },
            colour = when (job.status) {
                JobStatus.TO_DO -> customOrange
                JobStatus.IN_PROGRESS -> customOrange
                JobStatus.COMPLETE -> customGreen
            }
        )
    }

    Spacer(modifier = Modifier.height(8.dp))

    val convertedDate = formatTimeAgo(job.deadline, true)
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Deadline: ",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )

        JobStatusAlerts(
            text = convertedDate,
            colour = when {
                convertedDate.contains("min") -> customRed
                convertedDate.contains("hr") -> customOrange
                convertedDate.contains("d") -> customOrange
                convertedDate.contains("m") -> customGreen
                else -> Color.Gray
            }
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
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
            Text(
                text = "Job Assignment",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (job.assignedUserUID.isNotEmpty()) {
                val assignedMember =
                    project.members.find { it.uid == job.assignedUserUID }
                if (assignedMember != null) {
                    val memberName = assignedMember.displayName ?: "Unknown Member"
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Assigned Member",
                            tint = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Currently assigned to: $memberName",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Spacer(modifier = Modifier.weight(1f))

                        OutlinedButton(
                            onClick = { projectViewModel.unassignJob(job.jobID) }
                        ) {
                            Text("Remove")
                        }
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    JobStatusAlerts(
                        text = "Unassigned",
                        colour = customBlue
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                ExposedDropdownMenuBox(
                    expanded = showAssignMemberDropdown,
                    onExpandedChange = { showAssignMemberDropdown = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = if (selectedMemberID.isEmpty()) "Select a member" else {
                            val member =
                                project.members.find { it.uid == selectedMemberID }
                            member?.displayName ?: "Unknown Member"
                        },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Assign to") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.AddCircle,
                                contentDescription = "Assign Member"
                            )
                        },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = showAssignMemberDropdown)
                        },
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth()
                    )

                    DropdownMenu(
                        expanded = showAssignMemberDropdown,
                        onDismissRequest = { showAssignMemberDropdown = false },
                        modifier = Modifier.exposedDropdownSize()
                    ) {
                        if (availableMembers.isEmpty()) {
                            DropdownMenuItem(
                                text = { Text("No members available") },
                                onClick = { }
                            )
                        } else {
                            availableMembers.forEach { member ->
                                DropdownMenuItem(
                                    text = { Text(member.displayName) },
                                    onClick = {
                                        selectedMemberID = member.uid
                                        showAssignMemberDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        projectViewModel.assignJobToUser(
                            job.jobID,
                            selectedMemberID
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = selectedMemberID.isNotEmpty()
                ) {
                    Text("Assign Job")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobEdit(project: Project, job: Job, projectViewModel: ProjectViewModel, navController: NavController) {
    var jobTitle by remember { mutableStateOf(job.jobTitle) }
    var jobDescription by remember { mutableStateOf(job.jobDescription) }
    var priorityExpanded by remember { mutableStateOf(false) }
    var selectedPriority by remember { mutableStateOf(job.priority) }
    var statusExpanded by remember { mutableStateOf(false) }
    var selectedStatus by remember { mutableStateOf(job.status) }
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()
    val selectedDate = datePickerState.selectedDateMillis?.let { Date(it) }
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    var deadlineText by remember { mutableStateOf("Select Deadline") }

    if (selectedDate != null) {
        deadlineText = dateFormat.format(selectedDate)
    }

    //Allows the user to select a deadline using the Material DatePickerDialog component
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    showDatePicker = false
                    if (datePickerState.selectedDateMillis != null) {
                        deadlineText = dateFormat.format(Date(datePickerState.selectedDateMillis!!))
                    }
                }) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    OutlinedTextField(
        value = jobTitle,
        onValueChange = { jobTitle = it },
        label = { Text("Job Title") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
    )

    Spacer(modifier = Modifier.height(16.dp))

    OutlinedTextField(
        value = jobDescription,
        onValueChange = { jobDescription = it },
        label = { Text("Job Description") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
    )

    Spacer(modifier = Modifier.height(16.dp))

    ExposedDropdownMenuBox(
        expanded = priorityExpanded,
        onExpandedChange = { priorityExpanded = it },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = when (selectedPriority) {
                JobPriority.LOW -> "Low Priority"
                JobPriority.MEDIUM -> "Medium Priority"
                JobPriority.HIGH -> "High Priority"
            },
            onValueChange = {},
            readOnly = true,
            label = { Text("Priority") },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = priorityExpanded)
            },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )

        DropdownMenu(
            expanded = priorityExpanded,
            onDismissRequest = { priorityExpanded = false },
            modifier = Modifier.exposedDropdownSize()
        ) {
            DropdownMenuItem(
                text = { Text("Low Priority") },
                onClick = {
                    selectedPriority = JobPriority.LOW
                    priorityExpanded = false
                }
            )
            DropdownMenuItem(
                text = { Text("Medium Priority") },
                onClick = {
                    selectedPriority = JobPriority.MEDIUM
                    priorityExpanded = false
                }
            )
            DropdownMenuItem(
                text = { Text("High Priority") },
                onClick = {
                    selectedPriority = JobPriority.HIGH
                    priorityExpanded = false
                }
            )
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    //Lets the user select different completion statuses
    ExposedDropdownMenuBox(
        expanded = statusExpanded,
        onExpandedChange = { statusExpanded = it },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = when (selectedStatus) {
                JobStatus.TO_DO -> "To-Do"
                JobStatus.IN_PROGRESS -> "In Progress"
                JobStatus.COMPLETE -> "Complete"
            },
            onValueChange = {},
            readOnly = true,
            label = { Text("Status") },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusExpanded)
            },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )

        DropdownMenu(
            expanded = statusExpanded,
            onDismissRequest = { statusExpanded = false },
            modifier = Modifier.exposedDropdownSize()
        ) {
            DropdownMenuItem(
                text = { Text("To-Do") },
                onClick = {
                    selectedStatus = JobStatus.TO_DO
                    statusExpanded = false
                }
            )
            DropdownMenuItem(
                text = { Text("In Progress") },
                onClick = {
                    selectedStatus = JobStatus.IN_PROGRESS
                    statusExpanded = false
                }
            )
            DropdownMenuItem(
                text = { Text("Complete") },
                onClick = {
                    selectedStatus = JobStatus.COMPLETE
                    statusExpanded = false
                }
            )
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    OutlinedTextField(
        value = deadlineText,
        onValueChange = {},
        readOnly = true,
        label = { Text("Deadline") },
        trailingIcon = {
            IconButton(onClick = { showDatePicker = true }) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = "Select Date"
                )
            }
        },
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(24.dp))

    Button(
        onClick = {
            if (jobTitle.isNotEmpty() && jobDescription.isNotEmpty() && selectedDate != null) {
                val calendar = Calendar.getInstance()
                calendar.time = selectedDate
                val timestamp = Timestamp(calendar.time)

                projectViewModel.updateJob(
                    job,
                    mapOf(
                        "jobTitle" to jobTitle,
                        "jobDescription" to jobDescription,
                        "projectID" to project.projectID,
                        "priority" to selectedPriority,
                        "status" to selectedStatus,
                        "deadline" to timestamp
                    )
                )

                navController.popBackStack()
            }
        },
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Submit Changes")
    }
}