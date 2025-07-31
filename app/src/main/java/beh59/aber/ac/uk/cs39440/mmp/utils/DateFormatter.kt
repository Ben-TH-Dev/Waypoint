package beh59.aber.ac.uk.cs39440.mmp.utils

import com.google.firebase.Timestamp
import java.util.Date
import java.util.concurrent.TimeUnit

/**
 * formatTimeAgo
 * Formats a timestamp to show how long ago a message was sent
 * Used for displaying the last message time in chat overview
 * @param timestamp The Firebase timestamp to format
 * @param isDeadline Used to specify if this is a deadline, or a previous date
 */
fun formatTimeAgo(timestamp: Timestamp, isDeadline: Boolean): String {
    //Gets the current time from the Date() API
    val currentTime = Date().time
    //Converts the Firebase timestamp to the same format as currentTime
    val eventTime = timestamp.toDate().time

    val timeDiff = currentTime - eventTime
    val remainingTime = eventTime - currentTime

    if (isDeadline) {
        if (remainingTime <= 0) return "Deadline missed"
    } else {
        if (timeDiff < 0) return "Just now"
    }

    if (isDeadline) {
        //Calculates the minutes, hours, and days left before the deadline ends based on timeDiff.
        val minutes = TimeUnit.MILLISECONDS.toMinutes(remainingTime)
        val hours = TimeUnit.MILLISECONDS.toHours(remainingTime)
        val days = TimeUnit.MILLISECONDS.toDays(remainingTime)

        return when {
            minutes < 60 -> if (minutes == 1L) "1min" else "$minutes" + "min"

            hours < 24 -> if (hours == 1L) "1hr" else "$hours" + "hr"

            days < 30 -> if (days == 1L) "1d" else "$days" + "d"

            days < 365 -> {
                val months = (days / 30)
                if (months == 1L) "1m" else "$months" + "m"
            }

            else -> "A long time ago"

        }
    } else {
        //Calculates the minutes, hours, and days left before the deadline ends based on timeDiff.
        val minutes = TimeUnit.MILLISECONDS.toMinutes(timeDiff)
        val hours = TimeUnit.MILLISECONDS.toHours(timeDiff)
        val days = TimeUnit.MILLISECONDS.toDays(timeDiff)

        return when {
            minutes < 1 -> "Just now"
            minutes < 60 -> if (minutes == 1L) "1min" else "$minutes" + "min"
            hours < 24 -> if (hours == 1L) "1hr" else "$hours" + "hr"
            days < 30 -> if (days == 1L) "1d" else "$days" + "d"
            days < 365 -> {
                val months = (days / 30)
                if (months == 1L) "1m" else "$months" + "m"
            }

            else -> "A long time ago"
        }
    }
}