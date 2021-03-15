package org.mifos.mobile.ui.fragments

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import com.mifos.mobile.passcode.utils.PasscodePreferencesHelper
import org.mifos.mobile.R
import org.mifos.mobile.api.local.PreferencesHelper
import org.mifos.mobile.ui.activities.PassCodeActivity
import org.mifos.mobile.ui.activities.base.BaseActivity
import org.mifos.mobile.utils.ConfigurationDialogFragmentCompat
import org.mifos.mobile.utils.ConfigurationPreference
import org.mifos.mobile.utils.Constants
import org.mifos.mobile.utils.LanguageHelper
import org.mifos.mobile.utils.MaterialDialog

/**
 * Created by dilpreet on 02/10/17.
 */
class SettingsFragment : PreferenceFragmentCompat(), OnSharedPreferenceChangeListener {

    private lateinit var preferencesHelper: PreferencesHelper
    private lateinit var passcodePreferencesHelper: PasscodePreferencesHelper

    private lateinit var currentPasscode: String
    private lateinit var usePasscodeSwitchPreference: SwitchPreference

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        preferencesHelper = PreferencesHelper(context)
        passcodePreferencesHelper = PasscodePreferencesHelper(context)
        currentPasscode = passcodePreferencesHelper.passCode
        addPreferencesFromResource(R.xml.settings_preference)
        usePasscodeSwitchPreference = findPreference(Constants.USE_PASSCODE) as SwitchPreference
        usePasscodeSwitchPreference.isChecked = preferencesHelper.usePasscode!!
        setSwitchPreferenceTitle(usePasscodeSwitchPreference, getString(R.string.turn_off_passcode), getString(R.string.turn_on_passcode))
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
            Constants.PASSWORD -> (activity as BaseActivity?)?.replaceFragment(
                    UpdatePasswordFragment.newInstance(), true, R.id.container
            )
            Constants.USE_PASSCODE -> {
                val usePasscode = (preference as SwitchPreference).isChecked
                if (usePasscode) {
                    val intent = Intent(activity, PassCodeActivity::class.java)
                    intent.putExtra(Constants.INTIAL_LOGIN, true)
                    intent.putExtra(Constants.REQUEST_SET_PASSCODE, true)
                    startActivityForResult(intent, Constants.RC_SET_PASSCODE)
                } else {
                    MaterialDialog.Builder().init(context)
                            .setTitle(getString(R.string.turn_off_passcode) + "?")
                            .setMessage(getString(R.string.do_you_want_to_turn_off_passcode))
                            .setPositiveButton(getString(R.string.proceed), DialogInterface.OnClickListener { _, _ ->
                                passcodePreferencesHelper.savePassCode("")
                                preferencesHelper.usePasscode = false
                            })
                            .setNegativeButton(R.string.cancel, DialogInterface.OnClickListener { _, _ ->
                                preference.isChecked = true
                                preferencesHelper.usePasscode = true
                            })
                            .createMaterialDialog()
                            .show()
                }
                setSwitchPreferenceTitle(
                        preference,
                        getString(R.string.turn_off_passcode),
                        getString(R.string.turn_on_passcode)
                )
            }
            Constants.CHANGE_PASSCODE -> {
                passcodePreferencesHelper.savePassCode("")
                val intent = Intent(activity, PassCodeActivity::class.java)
                intent.putExtra(Constants.INTIAL_LOGIN, true)
                intent.putExtra(Constants.REQUEST_UPDATE_PASSCODE, true)
                startActivityForResult(intent, Constants.RC_UPDATE_PASSCODE)
            }
        }
        return super.onPreferenceTreeClick(preference)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_CANCELED) {
            passcodePreferencesHelper.savePassCode(currentPasscode)
        }
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                Constants.RC_UPDATE_PASSCODE -> {
                    if (data!!.getBooleanExtra(Constants.PASSWORD_UPDATED, false)) {
                        Toast.makeText(activity, getString(R.string.passcode_updated), Toast.LENGTH_LONG).show()
                    }
                }
                Constants.RC_SET_PASSCODE -> {
                    if (data!!.getBooleanExtra(Constants.PASSWORD_UPDATED, true)) {
                        Toast.makeText(activity, getString(R.string.passcode_set_successfully), Toast.LENGTH_LONG).show()
                    } else {
                        usePasscodeSwitchPreference.isChecked = false
                        preferencesHelper.usePasscode = false
                        Toast.makeText(activity, getString(R.string.passcode_not_updated), Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun setSwitchPreferenceTitle(preference: SwitchPreference, onActiveTitle: String, onInactiveTitle: String) {
        preference.title = if (preference.isChecked) {
            onActiveTitle
        } else {
            onInactiveTitle
        }
    }

    companion object {
        @kotlin.jvm.JvmStatic
        fun newInstance(): SettingsFragment {
            return SettingsFragment()
        }
    }
}