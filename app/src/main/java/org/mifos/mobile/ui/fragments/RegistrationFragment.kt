package org.mifos.mobile.ui.fragments

import android.graphics.PorterDuff
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*

import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick

import org.mifos.mobile.R
import org.mifos.mobile.models.register.RegisterPayload
import org.mifos.mobile.presenters.RegistrationPresenter
import org.mifos.mobile.ui.activities.base.BaseActivity
import org.mifos.mobile.ui.fragments.base.BaseFragment
import org.mifos.mobile.ui.views.RegistrationView
import org.mifos.mobile.utils.Network
import org.mifos.mobile.utils.PasswordStrength
import org.mifos.mobile.utils.Toaster

import javax.inject.Inject

/**
 * Created by dilpreet on 31/7/17.
 */
class RegistrationFragment : BaseFragment(), RegistrationView {
    @JvmField
    @BindView(R.id.et_account_number)
    var etAccountNumber: EditText? = null

    @JvmField
    @BindView(R.id.et_username)
    var etUsername: EditText? = null

    @JvmField
    @BindView(R.id.et_first_name)
    var etFirstName: EditText? = null

    @JvmField
    @BindView(R.id.et_last_name)
    var etLastName: EditText? = null

    @JvmField
    @BindView(R.id.et_phone_number)
    var etPhoneNumber: EditText? = null

    @JvmField
    @BindView(R.id.et_email)
    var etEmail: EditText? = null

    @JvmField
    @BindView(R.id.et_password)
    var etPassword: EditText? = null

    @JvmField
    @BindView(R.id.et_confirm_password)
    var etConfirmPassword: EditText? = null

    @JvmField
    @BindView(R.id.rg_verification_mode)
    var rgVerificationMode: RadioGroup? = null

    @JvmField
    @Inject
    var presenter: RegistrationPresenter? = null

    @JvmField
    @BindView(R.id.progressBar)
    var progressBar: ProgressBar? = null

    @JvmField
    @BindView(R.id.password_strength)
    var strengthView: TextView? = null
    private var rootView: View? = null
    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.fragment_registration, container, false)
        (activity as BaseActivity?)?.activityComponent?.inject(this)
        val rootView = this.rootView
        if (rootView != null) {
            ButterKnife.bind(this, rootView)
        }
        presenter?.attachView(this)
        progressBar?.visibility = View.GONE
        strengthView?.visibility = View.GONE
        etPassword?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                if (charSequence.isEmpty()) {
                    progressBar?.visibility = View.GONE
                    strengthView?.visibility = View.GONE
                } else {
                    progressBar?.visibility = View.VISIBLE
                    strengthView?.visibility = View.VISIBLE
                    updatePasswordStrengthView(charSequence.toString())
                }
            }

            override fun afterTextChanged(editable: Editable) {}
        })
        return rootView
    }

    private fun updatePasswordStrengthView(password: String) {
        if (TextView.VISIBLE != strengthView?.visibility) return
        if (password.isEmpty()) {
            strengthView?.text = ""
            progressBar?.progress = 0
            return
        }
        val str = PasswordStrength.calculateStrength(password)
        strengthView?.text = str.getText(context)
        strengthView?.setTextColor(str.color)
        val mode = PorterDuff.Mode.SRC_IN
        progressBar?.progressDrawable?.setColorFilter(str.color, mode)
        if (str.getText(context) == getString(R.string.password_strength_weak)) {
            progressBar?.progress = 25
        } else if (str.getText(context) == getString(R.string.password_strength_medium)) {
            progressBar?.progress = 50
        } else if (str.getText(context) == getString(R.string.password_strength_strong)) {
            progressBar?.progress = 75
        } else {
            progressBar?.progress = 100
        }
    }

    @OnClick(R.id.btn_register)
    fun registerClicked() {
        if (areFieldsValidated()) {
            val radioButton = rgVerificationMode?.checkedRadioButtonId?.let { rootView?.findViewById<RadioButton>(it) }
            val payload = RegisterPayload()
            payload.accountNumber = etAccountNumber?.text.toString()
            payload.authenticationMode = radioButton?.text.toString()
            payload.email = etEmail?.text.toString()
            payload.firstName = etFirstName?.text.toString()
            payload.lastName = etLastName?.text.toString()
            payload.mobileNumber = etPhoneNumber?.text.toString()
            if (etPassword?.text.toString() != etConfirmPassword?.text.toString()) {
                Toaster.show(rootView, getString(R.string.error_password_not_match))
                return
            } else {
                payload.password = etPassword?.text.toString()
            }
            payload.password = etPassword?.text.toString()
            payload.username = etUsername?.text.toString().replace(" ", "")
            if (Network.isConnected(context)) {
                presenter?.registerUser(payload)
            } else {
                Toaster.show(rootView, getString(R.string.no_internet_connection))
            }
        }
    }

    private fun areFieldsValidated(): Boolean {
        if (etAccountNumber?.text.toString().trim { it <= ' ' }.isEmpty()) {
            Toaster.show(rootView, getString(R.string.error_validation_blank, getString(R.string.account_number)))
            return false
        } else if (etUsername?.text.toString().trim { it <= ' ' }.isEmpty()) {
            Toaster.show(rootView, getString(R.string.error_validation_blank, getString(R.string.username)))
            return false
        } else if (etUsername?.text.toString().trim { it <= ' ' }.length < 6) {
            Toaster.show(rootView, getString(R.string.error_username_greater_than_six))
            return false
        } else if (etUsername?.text.toString().trim { it <= ' ' }.contains(" ")) {
            Toaster.show(rootView, getString(R.string.error_validation_cannot_contain_spaces,
                    getString(R.string.username), getString(R.string.not_contain_username)))
            return false
        } else if (etFirstName?.text?.isEmpty() == true) {
            Toaster.show(rootView, getString(R.string.error_validation_blank, getString(R.string.first_name)))
            return false
        } else if (etLastName?.text.toString().trim { it <= ' ' }.isEmpty()) {
            Toaster.show(rootView, getString(R.string.error_validation_blank, getString(R.string.last_name)))
            return false
        } else if (etPhoneNumber?.text.toString().trim { it <= ' ' }.isEmpty()) {
            Toaster.show(rootView, getString(R.string.error_validation_blank, getString(R.string.phone_number)))
            return false 
        } else if (etEmail?.text.toString().trim { it <= ' ' }.isEmpty()) {
            Toaster.show(rootView, getString(R.string.error_validation_blank, getString(R.string.email)))
            return false
        } else if (etPassword?.text.toString().trim { it <= ' ' }.isEmpty()) {
            Toaster.show(rootView, getString(R.string.error_validation_blank, getString(R.string.password)))
            return false
        } else if (etPassword?.text.toString().trim { it <= ' ' }.length
                < etPassword?.text.toString().length) {
            Toaster.show(rootView,
                    getString(R.string.error_validation_cannot_contain_leading_or_trailing_spaces,
                            getString(R.string.password)))
            return false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(etEmail?.text.toString().trim { it <= ' ' })
                        .matches()) {
            Toaster.show(rootView, getString(R.string.error_invalid_email))
            return false
        } else if (etPassword?.text.toString().trim { it <= ' ' }.length < 6) {
            Toaster.show(rootView, getString(R.string.error_validation_minimum_chars,
                    getString(R.string.password), resources.getInteger(R.integer.password_minimum_length)))
            return false
        }
        return true
    }

    override fun showRegisteredSuccessfully() {
        (activity as BaseActivity?)?.replaceFragment(RegistrationVerificationFragment.newInstance(), true, R.id.container)
    }

    override fun showError(msg: String?) {
        Toaster.show(rootView, msg)
    }

    override fun showProgress() {
        showMifosProgressDialog(getString(R.string.sign_up))
    }

    override fun hideProgress() {
        hideMifosProgressDialog()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        presenter?.detachView()
    }

    companion object {
        fun newInstance(): RegistrationFragment {
            return RegistrationFragment()
        }
    }
}
