package org.mifos.mobile.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.mifos.mobile.passcode.MifosPassCodeActivity
import com.mifos.mobile.passcode.utils.EncryptionUtil
import com.mifos.mobile.passcode.utils.PasscodePreferencesHelper
import org.mifos.mobile.R
import org.mifos.mobile.utils.Constants
import org.mifos.mobile.utils.Toaster

class PassCodeActivity : MifosPassCodeActivity() {
    private var currPassCode: String? = null
    private var isToUpdatePassCode: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        intent?.let {
            currPassCode = it.getStringExtra(Constants.CURR_PASSWORD)
            isToUpdatePassCode = it.getBooleanExtra(Constants.IS_TO_UPDATE_PASS_CODE, false)
        }
    }

    override fun showToaster(view: View?, msg: Int) {
        Toaster.show(view, msg, Toaster.SHORT)
    }
    override fun startLoginActivity() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
    override fun getLogo(): Int {
        return R.drawable.mifos_logo
    }
    override fun getEncryptionType(): Int {
        return EncryptionUtil.FINERACT_CN
    }
    override fun startNextActivity() {
        startActivity(Intent(this, HomeActivity::class.java))
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if (isToUpdatePassCode && !currPassCode.isNullOrEmpty()) {
            PasscodePreferencesHelper(this).apply {
                savePassCode(currPassCode)
            }
        }
        finish()
    }
}
