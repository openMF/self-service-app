package org.mifos.mobile.ui.activities;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import org.mifos.mobile.R;
import org.mifos.mobile.ui.activities.base.BaseActivity;
import org.mifos.mobile.ui.fragments.BeneficiaryAddOptionsFragment;

import java.util.List;

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
        replaceFragment(BeneficiaryAddOptionsFragment.newInstance(), false, R.id.container);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        if (fragments != null) {
            for (Fragment fragment : fragments) {
                fragment.onRequestPermissionsResult(requestCode, permissions, grantResults);
            }
        }
    }
    
}
