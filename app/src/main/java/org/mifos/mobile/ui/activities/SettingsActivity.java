package org.mifos.mobile.ui.activities;

import android.content.Intent;
import android.os.Bundle;

import org.mifos.mobile.R;
import org.mifos.mobile.ui.activities.base.BaseActivity;
import org.mifos.mobile.ui.fragments.SettingsFragment;
import org.mifos.mobile.utils.Constants;

import androidx.core.app.ActivityCompat;

public class SettingsActivity extends BaseActivity {

    private boolean hasSettingsChanged;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setToolbarTitle(getString(R.string.settings));
        showBackButton();
        updateThemeVariables();
        replaceFragment(SettingsFragment.newInstance(), false, R.id.container);
        if (getIntent().hasExtra(Constants.HAS_SETTINGS_CHANGED)) {
            hasSettingsChanged = getIntent().getBooleanExtra(Constants.HAS_SETTINGS_CHANGED,
                    false);
        }
    }


    private void updateThemeVariables() {
        toolbar.setBackgroundColor(getPrimaryColor());
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (hasSettingsChanged) {
            ActivityCompat.finishAffinity(this);
            Intent i = new Intent(this, HomeActivity.class);
            startActivity(i);
        }
    }
}
