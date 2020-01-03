package org.mifos.mobile.ui.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.mifos.mobile.passcode.utils.PasscodePreferencesHelper;

import org.mifos.mobile.R;
import org.mifos.mobile.ui.activities.PassCodeActivity;
import org.mifos.mobile.ui.activities.base.BaseActivity;
import org.mifos.mobile.utils.ConfigurationDialogFragmentCompat;
import org.mifos.mobile.utils.ConfigurationPreference;
import org.mifos.mobile.utils.Constants;
import org.mifos.mobile.utils.LanguageHelper;

/**
 * Created by dilpreet on 02/10/17.
 */

public class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.
        OnSharedPreferenceChangeListener {
    private PasscodePreferencesHelper passcodePreferencesHelper;
    public static SettingsFragment newInstance() {
        return new SettingsFragment();
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.settings_preference);
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().
                unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onDisplayPreferenceDialog(Preference preference) {
        DialogFragment dialogFragment = null;
        if (preference instanceof ConfigurationPreference) {
            dialogFragment = new ConfigurationDialogFragmentCompat();
            Bundle bundle = new Bundle(1);
            bundle.putString("key", preference.getKey());
            dialogFragment.setArguments(bundle);
        }

        if (dialogFragment != null && this.getFragmentManager() != null) {
            dialogFragment.setTargetFragment(this, 0);
            dialogFragment.show(this.getFragmentManager(),
                    "android.support.v7.preference.PreferenceFragment.DIALOG");
        } else {
            super.onDisplayPreferenceDialog(preference);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        Preference preference = findPreference(s);
        if (preference instanceof ListPreference && getActivity() != null) {
            ListPreference listPreference = (ListPreference) preference;
            LanguageHelper.setLocale(getContext(), listPreference.getValue());
            Intent intent = new Intent(getActivity(), getActivity().getClass());
            intent.putExtra(Constants.HAS_SETTINGS_CHANGED, true);
            startActivity(intent);
            getActivity().finish();
        }
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        switch (preference.getKey()) {
            case Constants.PASSWORD:
                if (getActivity() != null)
                    ((BaseActivity) getActivity()).replaceFragment(UpdatePasswordFragment
                        .newInstance(), false, R.id.container);
                break;
            case Constants.PASSCODE:
                if (getActivity() != null) {
                    passcodePreferencesHelper = new PasscodePreferencesHelper(getActivity());
                    String currentPass = passcodePreferencesHelper.getPassCode();
                    passcodePreferencesHelper.savePassCode("");
                    Intent intent = new Intent(getActivity(), PassCodeActivity.class );
                    intent.putExtra(Constants.CURR_PASSWORD, currentPass);
                    intent.putExtra(Constants.UPDATE_PASSWORD_KEY, true);
                    startActivity(intent);
                }
        }
        return super.onPreferenceTreeClick(preference);
    }
}
