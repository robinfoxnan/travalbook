package com.bird2fish.travelbook.ui.setting

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.bird2fish.travelbook.R

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
    }
}