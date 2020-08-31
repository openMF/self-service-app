package org.mifos.mobile.ui.activities;

/*
 * Created by saksham on 01/June/2018
 */

import android.content.Intent;
import android.os.Bundle;

import com.mifos.mobile.passcode.utils.PasscodePreferencesHelper;

import org.mifos.mobile.R;
import org.mifos.mobile.ui.activities.base.BaseActivity;
import org.mifos.mobile.utils.Constants;



public class SplashActivity extends BaseActivity {

    PasscodePreferencesHelper passcodePreferencesHelper;
    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivityComponent().inject(this);

        passcodePreferencesHelper = new PasscodePreferencesHelper(this);
        if (!passcodePreferencesHelper.getPassCode().isEmpty()) {
            intent = new Intent(this, PassCodeActivity.class);
            intent.putExtra(Constants.INTIAL_LOGIN, true);
            if (getIntent().getAction() != null &&
                    !getIntent().getAction().trim().equals(getString(R.string.defaultAction))) {
                intent.setAction(getIntent().getAction());
            }
        } else {
            intent = new Intent(this, LoginActivity.class);
        }
        startActivity(intent);
        finish();
    }
}
