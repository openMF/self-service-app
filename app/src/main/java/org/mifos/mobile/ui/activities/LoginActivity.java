package org.mifos.mobile.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;

import com.google.android.material.textfield.TextInputLayout;

import org.mifos.mobile.R;
import org.mifos.mobile.presenters.LoginPresenter;
import org.mifos.mobile.ui.activities.base.BaseActivity;
import org.mifos.mobile.ui.views.LoginView;
import org.mifos.mobile.utils.Constants;
import org.mifos.mobile.utils.Network;
import org.mifos.mobile.utils.Toaster;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * @author Vishwajeet
 * @since 05/06/16
 */

public class LoginActivity extends BaseActivity implements LoginView {

    @Inject
    LoginPresenter loginPresenter;

    @BindView(R.id.btn_login)
    AppCompatButton btnLogin;

    @BindView(R.id.til_username)
    TextInputLayout tilUsername;

    @BindView(R.id.til_password)
    TextInputLayout tilPassword;

    @BindView(R.id.ll_login)
    LinearLayout llLogin;

    private String userName;
    private int totpRequestCode = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivityComponent().inject(this);

        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        loginPresenter.attachView(this);
    }

    /**
     * Called when Login is user has successfully logged in
     *
     * @param userName Username of the user that successfully logged in!
     */
    @Override
    public void onLoginSuccess(String userName) {
        this.userName = userName;
        loginPresenter.loadClient();
    }

    /**
     * Shows ProgressDialog when called
     */
    @Override
    public void showProgress() {
        showProgressDialog(getString(R.string.progress_message_login));
    }

    /**
     * Hides the progressDialog which is being shown
     */
    @Override
    public void hideProgress() {
        hideProgressDialog();
    }

    /**
     * Starts {@link PassCodeActivity}
     */
    @Override
    public void showPassCodeActivity() {
        showToast(getString(R.string.toast_welcome, userName));
        startPassCodeActivity();
    }

    @Override
    public void showTwoFactorAuthActivity(String userName) {
        Intent intent = new Intent(LoginActivity.this, TwoFactorAuthActivity.class);
        intent.putExtra(Constants.USER_NAME, userName);
        startActivityForResult(intent, totpRequestCode);
    }

    /**
     * It is called whenever any error occurs while executing a request
     *
     * @param errorMessage Error message that tells the user about the problem.
     */
    @Override
    public void showMessage(String errorMessage) {
        showToast(errorMessage, Toast.LENGTH_LONG);
        llLogin.setVisibility(View.VISIBLE);
    }

    @Override
    public void showUsernameError(String error) {
        tilUsername.setError(error);
    }

    @Override
    public void showPasswordError(String error) {
        tilPassword.setError(error);
    }

    @Override
    public void clearUsernameError() {
        tilUsername.setErrorEnabled(false);
    }

    @Override
    public void clearPasswordError() {
        tilPassword.setErrorEnabled(false);
    }

    /**
     * Called when Login Button is clicked, used for logging in the user
     */
    @OnClick(R.id.btn_login)
    public void onLoginClicked() {

        final String username = tilUsername.getEditText().getEditableText().toString();
        final String password = tilPassword.getEditText().getEditableText().toString();

        if (Network.isConnected(this)) {
            loginPresenter.login(username, password);
        } else {
            Toaster.show(llLogin, getString(R.string.no_internet_connection));
        }
    }

    @OnClick(R.id.btn_register)
    public void onRegisterClicked() {
        startActivity(new Intent(LoginActivity.this, RegistrationActivity.class));

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        loginPresenter.detachView();
    }

    /**
     * Starts {@link PassCodeActivity} with {@code Constans.INTIAL_LOGIN} as true
     */
    private void startPassCodeActivity() {
        Intent intent = new Intent(LoginActivity.this, PassCodeActivity.class);
        intent.putExtra(Constants.INTIAL_LOGIN, true);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == totpRequestCode) {
            if (resultCode == RESULT_OK) {
                assert data != null;
                onLoginSuccess(data.getStringExtra(Constants.USER_NAME));
            } else {
                showMessage(getString(R.string.totp_verification_failed));
            }
        }
    }
}
