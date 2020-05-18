package org.mifos.mobile.ui.activities;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;

import androidx.annotation.RequiresApi;

import org.mifos.mobile.R;
import org.mifos.mobile.ui.activities.base.BaseActivity;
import org.mifos.mobile.ui.fragments.RegistrationFragment;

import java.util.Objects;

import butterknife.BindView;
import butterknife.OnClick;

public class RegistrationActivity extends BaseActivity {

    @BindView(R.id.register_linear)
    LinearLayout llLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        replaceFragment(RegistrationFragment.newInstance(), false, R.id.container);


    }
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @OnClick(R.id.register_linear)
    public void hideKeyboardregister(View view) {
        InputMethodManager manager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        manager.hideSoftInputFromWindow(Objects.requireNonNull(getCurrentFocus())
                .getWindowToken(), 0);
    }
}

