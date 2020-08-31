package org.mifos.mobile.ui.fragments

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.mifos.mobile.passcode.utils.PasscodePreferencesHelper
import org.mifos.mobile.R
import org.mifos.mobile.ui.activities.PassCodeActivity
import org.mifos.mobile.utils.Constants
import org.mifos.mobile.utils.LanguageUtils

class SettingsFragment : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {

    companion object {
        fun newInstance() = SettingsFragment()
    }

    override fun onResume() {
        super.onResume()
        preferenceScreen.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        TODO("Not yet implemented")
    }

    override fun onPause() {
        super.onPause()
        preferenceScreen.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        val preference = findPreference<Preference>(key.toString())
        if (preference is ListPreference) {
            context?.let {
                LanguageUtils.setLocale(it, preference.value)
                activity?.recreate()
            }
        }
    }

    override fun onPreferenceTreeClick(preference: Preference): Boolean {
        when (preference.key) {
            getString(R.string.password) -> {
                //    TODO("create changePasswordActivity and implement the logic for password change")
            }

            getString(R.string.passcode) -> {
                activity?.let {
                    val passCodePreferencesHelper = PasscodePreferencesHelper(activity)
                    val currPassCode = passCodePreferencesHelper.passCode
                    passCodePreferencesHelper.savePassCode("")
                    val intent = Intent(it, PassCodeActivity::class.java).apply {
                        putExtra(Constants.CURR_PASSWORD, currPassCode)
                        putExtra(Constants.IS_TO_UPDATE_PASS_CODE, true)
                    }
                    startActivity(intent)
                }
            }
        }
        return super.onPreferenceTreeClick(preference)
    }
}