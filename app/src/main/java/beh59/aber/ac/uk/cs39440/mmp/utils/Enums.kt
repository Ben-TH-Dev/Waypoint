package beh59.aber.ac.uk.cs39440.mmp.utils

/**
 * JobStatus
 * An enum that defines three states a job status can be in
 * TO_DO, IN_PROGRESS, and COMPLETE
 */
enum class JobStatus {
    TO_DO, IN_PROGRESS, COMPLETE
}

/**
 * JobPriority
 * An enum that defines three states a job priority can be in
 * LOW, MEDIUM, and HIGH
 */
enum class JobPriority {
    LOW, MEDIUM, HIGH
}

/**
 * ViewMode
 * An enum that defines two states a view can be in on some specific screens
 * GRID, and LIST
 */
enum class ViewMode {
    GRID, LIST
}

/**
 * OnboardingState
 * Used in addToFirestore to signal to the UserViewModel the five states a user account can be
 * in at this stage to assist with the onboarding process.
 * NEW_USER_NEEDS_BOTH, EXISTING_USER_NEEDS_BOTH, EXISTING_USER_NEEDS_USERNAME,
 * EXISTING_USER_NEEDS_NUMBER, EXISTING_USER_COMPLETED_ONBOARDING, and an ERROR state
 */
enum class OnboardingState {
    //A newly created account will always need a new username.
    NEW_USER_NEEDS_BOTH,

    //Important if a user logs in with google successfully but closes the app before creating
    //a username.
    EXISTING_USER_NEEDS_BOTH,
    EXISTING_USER_NEEDS_USERNAME,
    EXISTING_USER_NEEDS_NUMBER,

    //If the user is in this state, then the app proceeds as to the map screen.
    EXISTING_USER_COMPLETED_ONBOARDING,
    ERROR
}