package org.mifos.mobile.ui.activities

import android.app.Activity
import android.os.Bundle
import androidx.lifecycle.ViewModelProviders
import kotlinx.android.synthetic.main.activity_two_factor_auth.*
import org.mifos.mobile.R
import org.mifos.mobile.ui.activities.base.BaseActivity
import org.mifos.mobile.utils.Constants
import org.mifos.mobile.viewModels.TwoFactorAuthViewModel
import org.mifos.mobile.viewModels.TwoFactorAuthViewModelFactory
import javax.inject.Inject

/**
 * @author Prashant Khandelwal
 * On 11/04/19.
 */
class TwoFactorAuthActivity : BaseActivity() {

    @Inject
    lateinit var twoFactorAuthViewModelFactory: TwoFactorAuthViewModelFactory

    private var userName: String? = null

    private lateinit var viewModel: TwoFactorAuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_two_factor_auth)
        showBackButton()

        setToolbarTitle(getString(R.string.title_verify_code))
        activityComponent.inject(this)

        viewModel = ViewModelProviders.of(this, twoFactorAuthViewModelFactory)
                .get(TwoFactorAuthViewModel::class.java)

        if (intent != null && intent.hasExtra(Constants.USER_NAME)) {
            userName = intent.getStringExtra(Constants.USER_NAME)
        }

        btn_submit.setOnClickListener {
            verify()
        }
    }

    private fun verify() {
        val stCode = et_code.text.toString()
        if (stCode.isNotEmpty()) {
            try {
                val code = Integer.parseInt(stCode)
                val isCodeValid = viewModel.verifyCode(code)
                if (isCodeValid) {
                    intent.putExtra(Constants.USER_NAME, userName)
                    setResult(Activity.RESULT_OK, intent)
                    finish()
                } else {
                    showToast(getString(R.string.incorrect_code))
                }
            } catch (e: Exception) {
                showToast(getString(R.string.invalid_code))
            }

        } else {
            showToast(getString(R.string.enter_verification_code))
        }
    }
}
