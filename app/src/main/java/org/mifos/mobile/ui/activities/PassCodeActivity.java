package org.mifos.mobile.ui.activities;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.mifos.mobile.passcode.MifosPassCodeActivity;
import com.mifos.mobile.passcode.utils.EncryptionUtil;
import com.mifos.mobile.passcode.utils.PasscodePreferencesHelper;

import org.mifos.mobile.R;
import org.mifos.mobile.utils.CheckSelfPermissionAndRequest;
import org.mifos.mobile.utils.Constants;
import org.mifos.mobile.utils.MaterialDialog;
import org.mifos.mobile.utils.Toaster;

public class PassCodeActivity extends MifosPassCodeActivity {
    private String currPass = "";
    private Boolean updatePassword = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!CheckSelfPermissionAndRequest.checkSelfPermission(this,
                Manifest.permission.READ_PHONE_STATE)) {
            requestPermission();
        }
        if (getIntent() != null) {
            currPass = getIntent().getStringExtra(Constants.CURR_PASSWORD);
            updatePassword = getIntent().getBooleanExtra(Constants.UPDATE_PASSWORD_KEY, false);
        }
    }

    /**
     * Uses {@link CheckSelfPermissionAndRequest} to check for runtime permissions
     */
    private void requestPermission() {
        CheckSelfPermissionAndRequest.requestPermission(
                this,
                Manifest.permission.READ_PHONE_STATE,
                Constants.PERMISSIONS_REQUEST_READ_PHONE_STATE,
                getResources().getString(
                        R.string.dialog_message_phone_state_permission_denied_prompt),
                getResources().getString(R.string.
                        dialog_message_phone_state_permission_never_ask_again),
                Constants.PERMISSIONS_READ_PHONE_STATE_STATUS);
    }

    @Override
    public int getLogo() {
        return R.drawable.mifos_logo;
    }

    @Override
    public void startNextActivity() {
        startActivity(new Intent(PassCodeActivity.this, HomeActivity.class));
    }

    @Override
    public void startLoginActivity() {
        new MaterialDialog.Builder().init(PassCodeActivity.this)
                .setCancelable(false)
                .setMessage(R.string.login_using_password_confirmation)
                .setPositiveButton(getString(R.string.logout),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent i = new Intent(PassCodeActivity.this,
                                        LoginActivity.class);
                                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.
                                        FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(i);
                                finish();
                            }
                        })
                .setNegativeButton(getString(R.string.cancel),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                .createMaterialDialog()
                .show();
    }

    @Override
    public void showToaster(View view, int msg) {
        Toaster.show(view, msg);
    }

    @Override
    public int getEncryptionType() {
        return EncryptionUtil.MOBILE_BANKING;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (updatePassword && !currPass.isEmpty()) {
            PasscodePreferencesHelper helper = new PasscodePreferencesHelper(this);
            helper.savePassCode(currPass);
        }
        finish();
    }
}
