package org.mifos.mobile.ui.activities;

/*
 * Created by saksham on 01/June/2018
 */

import android.content.Intent;
import android.os.Bundle;

import com.mifos.mobile.passcode.utils.PasscodePreferencesHelper;

import org.mifos.mobile.R;
import org.mifos.mobile.api.local.PreferencesHelper;
import org.mifos.mobile.ui.activities.base.BaseActivity;
import org.mifos.mobile.utils.Constants;

import javax.inject.Inject;

public class SplashActivity extends BaseActivity {

    @Inject
    PreferencesHelper preferencesHelper;

    PasscodePreferencesHelper passcodePreferencesHelper;
    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivityComponent().inject(this);

        passcodePreferencesHelper = new PasscodePreferencesHelper(this);
        if (!passcodePreferencesHelper.getPassCode().isEmpty() &&
                preferencesHelper.getBoolean(getString(R.string.use_passcode), true )) {
            intent = new Intent(this, PassCodeActivity.class);
            intent.putExtra(Constants.INTIAL_LOGIN, true);
        } else {
            intent = new Intent(this, LoginActivity.class);
        }
        startActivity(intent);
        finish();
    }
}
