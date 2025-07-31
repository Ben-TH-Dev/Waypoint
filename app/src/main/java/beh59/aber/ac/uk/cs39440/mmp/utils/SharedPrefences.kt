package beh59.aber.ac.uk.cs39440.mmp.utils

import android.content.Context
import android.preference.PreferenceManager
import beh59.aber.ac.uk.cs39440.mmp.R

/**
 * PreferenceHelper
 * A very simple implementation of SharedPreferences which persists and retrieves the value of
 * key:value pairs from the app's storage. Used to save the state of the user's location
 * sharing preference since it is important for data safety reasons and retrieve it quickly when the
 * app loads again.
 * @param context The context needed by the preference helper
 */
class PreferenceHelper(val context: Context) {
    private val prefs = PreferenceManager.getDefaultSharedPreferences(context)

    enum class BooleanPref(val prefId: Int, val defaultId: Int) {
        LocationToggle(R.string.pref_location_sharing, R.bool.pref_location_sharing_default),
    }

    fun getBooleanPref(pref: BooleanPref) =
        prefs.getBoolean(
            context.getString(pref.prefId),
            context.resources.getBoolean(pref.defaultId)
        )

    fun setBooleanPref(pref: BooleanPref, value: Boolean) =
        prefs.edit().putBoolean(context.getString(pref.prefId), value).commit()
}