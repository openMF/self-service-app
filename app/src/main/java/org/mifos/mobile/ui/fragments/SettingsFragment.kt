package org.mifos.mobile.ui.fragments

import android.content.Intent
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import org.mifos.mobile.R
import org.mifos.mobile.ui.activities.base.BaseActivity
import org.mifos.mobile.utils.ConfigurationDialogFragmentCompat
import org.mifos.mobile.utils.ConfigurationPreference
import org.mifos.mobile.utils.Constants
import org.mifos.mobile.utils.LanguageHelper

/**
 * Created by dilpreet on 02/10/17.
 */
class SettingsFragment : PreferenceFragmentCompat(), OnSharedPreferenceChangeListener {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.settings_preference)
    }

    override fun onResume() {
        super.onResume()
        activity?.title = getString(R.string.settings)
        preferenceScreen.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        super.onPause()
        preferenceScreen.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onDisplayPreferenceDialog(preference: Preference) {
        var dialogFragment: DialogFragment? = null
        if (preference is ConfigurationPreference) {
            dialogFragment = ConfigurationDialogFragmentCompat()
            val bundle = Bundle(1)
            bundle.putString("key", preference.getKey())
            dialogFragment.setArguments(bundle)
        }
        if (dialogFragment != null) {
            dialogFragment.setTargetFragment(this, 0)
            dialogFragment.show(this.fragmentManager,
                    "android.support.v7.preference.PreferenceFragment.DIALOG")
        } else {
            super.onDisplayPreferenceDialog(preference)
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, s: String) {
        val preference = findPreference(s)
        if (preference is ListPreference) {
            LanguageHelper.setLocale(context, preference.value)
            val intent = Intent(activity, activity?.javaClass)
            intent.putExtra(Constants.HAS_SETTINGS_CHANGED, true)
            startActivity(intent)
            activity?.finish()
        }
    }

    override fun onPreferenceTreeClick(preference: Preference): Boolean {
        when (preference.key) {
            Constants.PASSWORD -> (activity as BaseActivity?)?.replaceFragment(UpdatePasswordFragment.newInstance(), true, R.id.container)
        }
        return super.onPreferenceTreeClick(preference)
    }

    companion object {
        @kotlin.jvm.JvmStatic
        fun newInstance(): SettingsFragment {
            return SettingsFragment()
        }
    }
}