package beh59.aber.ac.uk.cs39440.mmp.data.models

import androidx.annotation.Keep

/**
 * optionItem
 * A simple data class that holds information used to construct the various option menus in the
 * application
 * @param text The textual description of the option
 * @param iconId Optionally give the option an icon
 * @param onClick The behaviour to occur when the option is clicked
 */
@Keep
data class OptionItem(
    val text: String,
    val iconId: Int? = null,
    val onClick: () -> Unit
)