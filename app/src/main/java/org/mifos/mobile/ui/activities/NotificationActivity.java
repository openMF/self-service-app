package org.mifos.mobile.ui.activities;

import android.os.Bundle;

import org.mifos.mobile.R;
import org.mifos.mobile.ui.activities.base.BaseActivity;
import org.mifos.mobile.ui.fragments.NotificationFragment;

public class NotificationActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        updateThemeVariables();
        replaceFragment(NotificationFragment.newInstance(), false, R.id.container);
    }

    private void updateThemeVariables() {
        toolbar.setBackgroundColor(getPrimaryColor());
    }
}
