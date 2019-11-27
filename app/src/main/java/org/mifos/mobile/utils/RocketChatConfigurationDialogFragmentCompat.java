package org.mifos.mobile.utils;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.preference.DialogPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceDialogFragmentCompat;

import org.mifos.mobile.R;
import org.mifos.mobile.api.local.PreferencesHelper;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by dilpreet on 11/03/18.
 */

public class RocketChatConfigurationDialogFragmentCompat extends
        PreferenceDialogFragmentCompat implements DialogPreference.TargetFragment {

    @BindView(R.id.spinner_server_protocol)
    Spinner spinnerServerProtocol;

    @BindView(R.id.et_server_url)
    EditText etServerUrl;

    private PreferencesHelper preferencesHelper;

    @Override
    protected View onCreateDialogView(Context context) {
        return LayoutInflater.from(context).inflate(R.layout.preference_configuration_rocket_chat,
                null);
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        ButterKnife.bind(this, view);
        preferencesHelper = new PreferencesHelper(getContext());

        RocketChatConfigurationPreference preference = (RocketChatConfigurationPreference)
                getPreference();

        setupSpinner(getContext());

        if (preference.getProtocol().equals("https://")) {
            spinnerServerProtocol.setSelection(0);
        } else {
            spinnerServerProtocol.setSelection(1);
        }

        etServerUrl.setText(preference.getDomain());
    }

    private void setupSpinner(Context context) {
        List<String> list = new ArrayList<>();
        list.add("https://");
        list.add("http://");
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(context,
                android.R.layout.simple_spinner_item, list);

        spinnerServerProtocol.setAdapter(arrayAdapter);
    }


    @Override
    public void onDialogClosed(boolean positiveResult) {
        if (positiveResult && !isFieldEmpty()) {
            RocketChatConfigurationPreference preference =
                    (RocketChatConfigurationPreference) getPreference();
            preference.updateConfigurations (spinnerServerProtocol.getSelectedItem().toString(),
                    etServerUrl.getText().toString());

            Intent intent = new Intent(getActivity(), getActivity().getClass());
            intent.putExtra(Constants.HAS_SETTINGS_CHANGED, true);
            startActivity(intent);
            getActivity().finish();
        }
    }



    @Override
    public Preference findPreference(CharSequence key) {
        return getPreference();
    }

    public boolean isFieldEmpty() {
        if (etServerUrl.getText().toString().trim().length() == 0) {
            return true;
        }
        if (etServerUrl.getText().toString().trim().length() == 0) {
            return  true;
        }
        return  false;
    }
}