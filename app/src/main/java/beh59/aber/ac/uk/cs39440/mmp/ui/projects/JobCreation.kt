package beh59.aber.ac.uk.cs39440.mmp.ui.projects

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import beh59.aber.ac.uk.cs39440.mmp.data.models.Project
import beh59.aber.ac.uk.cs39440.mmp.utils.JobPriority
import beh59.aber.ac.uk.cs39440.mmp.utils.JobStatus
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * JobCreation
 * Allows moderators of a project to create a job and configure its details before submitting it to
 * Firestore
 * @param project The project the moderator is currently viewing
 * @param projectViewModel Contains project related data and operations
 * @param navController Handles navigation between screens
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobCreation(
    project: Project,
    projectViewModel: ProjectViewModel,
    navController: NavController
) {
    //Captures the comprehensive user input required to submit a job to Firestore.
    var jobTitle by remember { mutableStateOf("") }
    var jobDescription by remember { mutableStateOf("") }
    var priorityExpanded by remember { mutableStateOf(false) }
    var selectedPriority by remember { mutableStateOf(JobPriority.MEDIUM) }
    var statusExpanded by remember { mutableStateOf(false) }
    var selectedStatus by remember { mutableStateOf(JobStatus.TO_DO) }
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Create Job for ${project.title}",
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
                    IconButton(onClick = { }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More Options"
                        )
                    }
                }
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding())
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
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
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                maxLines = 5
            )

            Spacer(modifier = Modifier.height(16.dp))

            //Lets the user select from different priorities
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

                        projectViewModel.submitJob(
                            jobTitle = jobTitle,
                            jobDescription = jobDescription,
                            projectID = project.projectID,
                            priority = selectedPriority,
                            status = selectedStatus,
                            deadline = timestamp
                        )

                        navController.popBackStack()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Create Job")
            }
        }
    }
}