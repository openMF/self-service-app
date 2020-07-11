package org.mifos.mobile.ui.activities;

import android.os.Bundle;

import org.mifos.mobile.R;
import org.mifos.mobile.ui.activities.base.BaseActivity;
import org.mifos.mobile.ui.enums.LoanState;
import org.mifos.mobile.ui.fragments.LoanApplicationFragment;

public class LoanApplicationActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loan_application);
        if (savedInstanceState == null) {
            replaceFragment(LoanApplicationFragment.newInstance(LoanState.CREATE), false,
                    R.id.container);
        }
        updateThemeVariables();
        showBackButton();
    }

    private void updateThemeVariables() {
        toolbar.setBackgroundColor(getPrimaryColor());
    }
}
