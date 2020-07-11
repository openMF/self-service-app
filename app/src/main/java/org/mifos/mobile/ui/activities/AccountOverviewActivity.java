package org.mifos.mobile.ui.activities;

import android.os.Bundle;

import org.mifos.mobile.R;
import org.mifos.mobile.ui.activities.base.BaseActivity;
import org.mifos.mobile.ui.fragments.AccountOverviewFragment;

/**
 * @author Rajan Maurya
 *         On 16/10/17.
 */
public class AccountOverviewActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_container);
        updateThemeVariables();
        replaceFragment(AccountOverviewFragment.newInstance(), false, R.id.container);
        showBackButton();
        hideToolbarElevation();
    }

    private void updateThemeVariables() {
        toolbar.setBackgroundColor(getPrimaryColor());
    }
}
