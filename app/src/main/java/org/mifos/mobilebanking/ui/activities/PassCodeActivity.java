package org.mifos.mobilebanking.ui.activities;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.mifos.mobile.passcode.MifosPassCodeActivity;
import com.mifos.mobile.passcode.utils.EncryptionUtil;

import org.mifos.mobilebanking.R;
import org.mifos.mobilebanking.utils.CheckSelfPermissionAndRequest;
import org.mifos.mobilebanking.utils.Constants;
import org.mifos.mobilebanking.utils.Toaster;

public class PassCodeActivity extends MifosPassCodeActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!CheckSelfPermissionAndRequest.checkSelfPermission(this,
                Manifest.permission.READ_PHONE_STATE)) {
            requestPermission();
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
        Toast.makeText(this, "Logged in succesfully", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(PassCodeActivity.this, HomeActivity.class));
    }

    @Override
    public void startLoginActivity() {
        Intent i = new Intent(PassCodeActivity.this, LoginActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }

    @Override
    public void showToaster(View view, int msg) {
        Toaster.show(view, msg);
    }

    @Override
    public int getEncryptionType() {
        return EncryptionUtil.MOBILE_BANKING;
    }

}
