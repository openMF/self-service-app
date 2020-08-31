package org.mifos.mobile.ui.activities;

/*
 * Created by saksham on 01/June/2018
 */

import android.content.Intent;
import android.os.Bundle;

import com.mifos.mobile.passcode.utils.PasscodePreferencesHelper;

import org.mifos.mobile.api.local.PreferencesHelper;
import org.mifos.mobile.ui.activities.base.BaseActivity;
import org.mifos.mobile.utils.Constants;

public class SplashActivity extends BaseActivity {

    PasscodePreferencesHelper passcodePreferencesHelper;
    PreferencesHelper prefs;
    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivityComponent().inject(this);

        passcodePreferencesHelper = new PasscodePreferencesHelper(this);
        prefs = new PreferencesHelper(this);
        if (!passcodePreferencesHelper.getPassCode().isEmpty()) {
            intent = new Intent(this, PassCodeActivity.class);
            intent.putExtra(Constants.INTIAL_LOGIN, true);
        } else if (passcodePreferencesHelper.getPassCode().isEmpty() &&
                prefs.getBoolean("check_first_time", true)) {
            intent = new Intent(this, IntroductionActivity.class);
        } else {
            intent = new Intent(this, LoginActivity.class);
        }
        startActivity(intent);
        finish();
    }
}
