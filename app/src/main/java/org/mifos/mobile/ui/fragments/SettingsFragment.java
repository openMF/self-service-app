package org.mifos.mobile.ui.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import org.mifos.mobile.R;
import org.mifos.mobile.api.local.PreferencesHelper;
import org.mifos.mobile.ui.activities.base.BaseActivity;
import org.mifos.mobile.utils.ConfigurationDialogFragmentCompat;
import org.mifos.mobile.utils.ConfigurationPreference;
import org.mifos.mobile.utils.Constants;
import org.mifos.mobile.utils.LanguageHelper;

import androidx.fragment.app.DialogFragment;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import javax.inject.Inject;

/**
 * Created by dilpreet on 02/10/17.
 */

public class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.
        OnSharedPreferenceChangeListener {

    @Inject
    PreferencesHelper preferencesHelper;

    public static SettingsFragment newInstance() {
        SettingsFragment fragment = new SettingsFragment();
        return fragment;
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        ((BaseActivity) getActivity()).getActivityComponent().inject(this);
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

        if (dialogFragment != null) {
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
        if (preference instanceof ListPreference) {
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
                ((BaseActivity) getActivity()).replaceFragment(UpdatePasswordFragment
                        .newInstance(), false, R.id.container);
                break;
            case Constants.TOTP:
                if (preferencesHelper.isTwoAuthEnabled()) {
                    Toast.makeText(getContext(), getString(R.string.toast_enabled),
                            Toast.LENGTH_SHORT).show();
                } else {
                    ((BaseActivity) getActivity()).replaceFragment(EnableTwoFactorAuthFragment
                            .Companion.newInstance(), true, R.id.container);
                }
                break;
        }
        return super.onPreferenceTreeClick(preference);
    }
}
