package org.mifos.mobile.ui.fragments

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick

import com.google.android.material.textfield.TextInputLayout

import org.mifos.mobile.R
import org.mifos.mobile.models.accounts.savings.SavingsAccountWithdrawPayload
import org.mifos.mobile.models.accounts.savings.SavingsWithAssociations
import org.mifos.mobile.presenters.SavingsAccountWithdrawPresenter
import org.mifos.mobile.ui.activities.base.BaseActivity
import org.mifos.mobile.ui.fragments.base.BaseFragment
import org.mifos.mobile.ui.views.SavingsAccountWithdrawView
import org.mifos.mobile.utils.Constants
import org.mifos.mobile.utils.MFDatePicker
import org.mifos.mobile.utils.Toaster

import javax.inject.Inject

/*
* Created by saksham on 02/July/2018
*/
class SavingsAccountWithdrawFragment : BaseFragment(), SavingsAccountWithdrawView {
    @kotlin.jvm.JvmField
    @BindView(R.id.tv_client_name)
    var tvClientName: TextView? = null

    @kotlin.jvm.JvmField
    @BindView(R.id.tv_account_number)
    var tvAccountNumber: TextView? = null

    @kotlin.jvm.JvmField
    @BindView(R.id.tv_withdrawal_date)
    var tvWithdrawalDate: TextView? = null

    @kotlin.jvm.JvmField
    @BindView(R.id.til_remark)
    var tilRemark: TextInputLayout? = null

    @kotlin.jvm.JvmField
    @Inject
    var presenter: SavingsAccountWithdrawPresenter? = null
    private var rootView: View? = null
    private var savingsWithAssociations: SavingsWithAssociations? = null
    private var payload: SavingsAccountWithdrawPayload? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            savingsWithAssociations = arguments?.getParcelable(Constants.SAVINGS_ACCOUNTS)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.fragment_savings_account_withdraw_fragment,
                container, false)
        ButterKnife.bind(this, rootView!!)
        (activity as BaseActivity?)?.activityComponent?.inject(this)
        presenter?.attachView(this)
        showUserInterface()
        return rootView
    }

    @OnClick(R.id.btn_withdraw_savings_account)
    fun onWithdrawSavingsAccount() {
        submitWithdrawSavingsAccount()
    }

    override fun showUserInterface() {
        activity?.title = getString(R.string.withdraw_savings_account)
        tvAccountNumber?.text = savingsWithAssociations?.accountNo
        tvClientName?.text = savingsWithAssociations?.clientName
        tvWithdrawalDate?.text = MFDatePicker.datePickedAsString
    }

    private val isFormIncomplete: Boolean?
        get() {
            var rv = false
            if (tilRemark?.editText?.text.toString().trim { it <= ' ' }.isEmpty()) {
                rv = true
                tilRemark?.error = getString(R.string.error_validation_blank,
                        getString(R.string.remark))
            }
            return rv
        }

    override fun submitWithdrawSavingsAccount() {
        tilRemark?.isErrorEnabled = false
        if (isFormIncomplete == false) {
            payload = SavingsAccountWithdrawPayload()
            payload?.note = tilRemark?.editText?.text.toString()
            payload?.withdrawnOnDate = MFDatePicker.datePickedAsString
            presenter?.submitWithdrawSavingsAccount(savingsWithAssociations?.accountNo, payload)
        }
    }

    override fun showSavingsAccountWithdrawSuccessfully() {
        val data = Intent()
        data.putExtra("message", getString(R.string.savings_account_withdraw_successful))
        activity?.setResult(RESULT_OK, data)
        activity?.finish()
    }

    override fun showMessage(message: String?) {
        Toaster.show(rootView, message)
    }

    override fun showError(error: String?) {
        Toaster.show(rootView, error)
    }

    override fun showProgress() {
        showMifosProgressDialog(getString(R.string.progress_message_loading))
    }

    override fun hideProgress() {
        hideMifosProgressDialog()
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter?.detachView()
    }

    companion object {
        fun newInstance(
                savingsWithAssociations: SavingsWithAssociations?): SavingsAccountWithdrawFragment {
            val fragment = SavingsAccountWithdrawFragment()
            val bundle = Bundle()
            bundle.putParcelable(Constants.SAVINGS_ACCOUNTS, savingsWithAssociations)
            fragment.arguments = bundle
            return fragment
        }
    }
}