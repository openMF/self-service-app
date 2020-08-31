package org.mifos.mobile.ui.activities

import android.Manifest
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.View

import com.mifos.mobile.passcode.MifosPassCodeActivity
import com.mifos.mobile.passcode.utils.EncryptionUtil

import org.mifos.mobile.R
import org.mifos.mobile.utils.CheckSelfPermissionAndRequest
import org.mifos.mobile.utils.Constants
import org.mifos.mobile.utils.MaterialDialog
import org.mifos.mobile.utils.Toaster

class PassCodeActivity : MifosPassCodeActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!CheckSelfPermissionAndRequest.checkSelfPermission(this,
                        Manifest.permission.READ_PHONE_STATE)) {
            requestPermission()
        }
    }

    /**
     * Uses [CheckSelfPermissionAndRequest] to check for runtime permissions
     */
    private fun requestPermission() {
        CheckSelfPermissionAndRequest.requestPermission(
                this,
                Manifest.permission.READ_PHONE_STATE,
                Constants.PERMISSIONS_REQUEST_READ_PHONE_STATE,
                resources.getString(
                        R.string.dialog_message_phone_state_permission_denied_prompt),
                resources.getString(R.string.dialog_message_phone_state_permission_never_ask_again),
                Constants.PERMISSIONS_READ_PHONE_STATE_STATUS)
    }

    override fun getLogo(): Int {
        return R.drawable.mifos_logo
    }

    override fun startNextActivity() {
        startActivity(Intent(this@PassCodeActivity, HomeActivity::class.java))
    }

    override fun startLoginActivity() {
        MaterialDialog.Builder().init(this@PassCodeActivity)
                .setCancelable(false)
                .setMessage(R.string.login_using_password_confirmation)
                .setPositiveButton(getString(R.string.logout),
                        DialogInterface.OnClickListener { _, _ ->
                            val i = Intent(this@PassCodeActivity,
                                    LoginActivity::class.java)
                            i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(i)
                            finish()
                        })
                .setNegativeButton(getString(R.string.cancel),
                        DialogInterface.OnClickListener { dialog, _ -> dialog.dismiss() })
                .createMaterialDialog()
                .show()
    }

    override fun showToaster(view: View, msg: Int) {
        Toaster.show(view, msg)
    }

    override fun getEncryptionType(): Int {
        return EncryptionUtil.MOBILE_BANKING
    }
}