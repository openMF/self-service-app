package org.mifos.mobile.ui.activities;

import android.os.Bundle;

import org.mifos.mobile.R;
import org.mifos.mobile.ui.activities.base.BaseActivity;
import org.mifos.mobile.ui.fragments.BeneficiaryAddOptionsFragment;

/**
 * @author Rajan Maurya
 * On 04/06/18.
 */
public class AddBeneficiaryActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_container);
        showBackButton();
        updateThemeVariables();
        replaceFragment(BeneficiaryAddOptionsFragment.newInstance(), false, R.id.container);
    }

    private void updateThemeVariables() {
        toolbar.setBackgroundColor(getPrimaryColor());
    }
}
