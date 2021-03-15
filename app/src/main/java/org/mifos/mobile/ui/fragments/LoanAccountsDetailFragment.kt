package org.mifos.mobile.ui.fragments

import android.os.Bundle
import android.os.Parcelable
import android.view.*
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.github.therajanmaurya.sweeterror.SweetUIErrorHandler
import org.mifos.mobile.R
import org.mifos.mobile.api.local.PreferencesHelper
import org.mifos.mobile.models.accounts.loan.LoanWithAssociations
import org.mifos.mobile.presenters.LoanAccountsDetailPresenter
import org.mifos.mobile.ui.activities.base.BaseActivity
import org.mifos.mobile.ui.enums.AccountType
import org.mifos.mobile.ui.enums.ChargeType
import org.mifos.mobile.ui.enums.LoanState
import org.mifos.mobile.ui.fragments.base.BaseFragment
import org.mifos.mobile.ui.views.LoanAccountsDetailView
import org.mifos.mobile.utils.*
import javax.inject.Inject

/*
~This project is licensed under the open source MPL V2.
~See https://github.com/openMF/self-service-app/blob/master/LICENSE.md
*/ /**
 * @author Vishwajeet
 * @since 19/08/16
 */
class LoanAccountsDetailFragment : BaseFragment(), LoanAccountsDetailView {
    @kotlin.jvm.JvmField
    @Inject
    var loanAccountDetailsPresenter: LoanAccountsDetailPresenter? = null

    @kotlin.jvm.JvmField
    @BindView(R.id.tv_outstanding_balance)
    var tvOutstandingBalanceName: TextView? = null

    @kotlin.jvm.JvmField
    @BindView(R.id.tv_next_installment)
    var tvNextInstallmentName: TextView? = null

    @kotlin.jvm.JvmField
    @BindView(R.id.tv_due_date)
    var tvDueDateName: TextView? = null

    @kotlin.jvm.JvmField
    @BindView(R.id.tv_account_number)
    var tvAccountNumberName: TextView? = null

    @kotlin.jvm.JvmField
    @BindView(R.id.tv_loan_type)
    var tvLoanTypeName: TextView? = null

    @kotlin.jvm.JvmField
    @BindView(R.id.tv_currency)
    var tvCurrencyName: TextView? = null

    @kotlin.jvm.JvmField
    @BindView(R.id.ll_account_detail)
    var llAccountDetail: LinearLayout? = null

    @kotlin.jvm.JvmField
    @BindView(R.id.layout_error)
    var layoutError: View? = null

    @kotlin.jvm.JvmField
    @BindView(R.id.btn_make_payment)
    var btMakePayment: Button? = null

    @kotlin.jvm.JvmField
    @Inject
    var preferencesHelper: PreferencesHelper? = null
    private var loanWithAssociations: LoanWithAssociations? = null
    private var showLoanUpdateOption = false
    private var loanId: Long? = 0
    private var sweetUIErrorHandler: SweetUIErrorHandler? = null
    var rootView: View? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            loanId = arguments?.getLong(Constants.LOAN_ID)
        }
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        (activity as BaseActivity?)?.activityComponent?.inject(this)
        rootView = inflater.inflate(R.layout.fragment_loan_account_details, container, false)
        setToolbarTitle(getString(R.string.loan_account_details))
        ButterKnife.bind(this, rootView!!)
        loanAccountDetailsPresenter?.attachView(this)
        sweetUIErrorHandler = SweetUIErrorHandler(activity, rootView)
        if (savedInstanceState == null) {
            loanAccountDetailsPresenter?.loadLoanAccountDetails(loanId)
        }
        return rootView
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(Constants.LOAN_ACCOUNT, loanWithAssociations)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (savedInstanceState != null) {
            showLoanAccountsDetail(savedInstanceState.getParcelable<Parcelable>(Constants.LOAN_ACCOUNT) as LoanWithAssociations)
        }
    }

    /**
     * Shows details about loan account fetched from server is status is Active else shows and
     * error layout i.e. `layoutError` with a msg related to the status.
     *
     * @param loanWithAssociations object containing details of each loan account,
     */
    override fun showLoanAccountsDetail(loanWithAssociations: LoanWithAssociations?) {
        llAccountDetail?.visibility = View.VISIBLE
        this.loanWithAssociations = loanWithAssociations
        if (loanWithAssociations?.status?.active!!) {
            val overdueSinceDate = loanWithAssociations.summary?.getOverdueSinceDate()
            if (overdueSinceDate == null) {
                tvDueDateName?.setText(R.string.not_available)
            } else {
                tvDueDateName?.text = DateHelper.getDateAsString(overdueSinceDate)
            }
            showDetails(loanWithAssociations)
        } else if (loanWithAssociations.status?.pendingApproval!!) {
            sweetUIErrorHandler?.showSweetCustomErrorUI(getString(R.string.approval_pending),
                    R.drawable.ic_assignment_turned_in_black_24dp, llAccountDetail, layoutError)
            showLoanUpdateOption = true
        } else if (loanWithAssociations.status?.waitingForDisbursal!!) {
            sweetUIErrorHandler?.showSweetCustomErrorUI(getString(R.string.waiting_for_disburse),
                    R.drawable.ic_assignment_turned_in_black_24dp, llAccountDetail, layoutError)
        } else {
            btMakePayment?.visibility = View.GONE
            tvDueDateName?.setText(R.string.not_available)
            showDetails(loanWithAssociations)
        }
        activity?.invalidateOptionsMenu()
    }

    /**
     * Sets basic information about a loan
     *
     * @param loanWithAssociations object containing details of each loan account,
     */
    fun showDetails(loanWithAssociations: LoanWithAssociations?) {
        var currencyRepresentation = loanWithAssociations?.summary?.currency?.displaySymbol
        if (currencyRepresentation == null) {
            currencyRepresentation = loanWithAssociations?.summary?.currency?.code
        }
        tvOutstandingBalanceName?.text = currencyRepresentation +
                CurrencyUtil.formatCurrency(activity, loanWithAssociations?.summary?.totalOutstanding)
        if (loanWithAssociations?.repaymentSchedule?.periods != null) for ((_, _, dueDate, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, totalDueForPeriod) in loanWithAssociations.repaymentSchedule?.periods!!) {
            if (dueDate == loanWithAssociations.summary?.getOverdueSinceDate()) {
                tvNextInstallmentName?.text = currencyRepresentation +
                        CurrencyUtil.formatCurrency(activity, totalDueForPeriod)
                break
            } else if (loanWithAssociations.summary?.getOverdueSinceDate() == null) {
                tvNextInstallmentName?.text = getString(R.string.not_available)
            }
        }
        tvAccountNumberName?.text = loanWithAssociations?.accountNo
        tvLoanTypeName?.text = loanWithAssociations?.loanType?.value
        tvCurrencyName?.text = loanWithAssociations?.summary?.currency?.code
    }

    /**
     * Opens [SavingsMakeTransferFragment] to Make a payment for loan account with given
     * `loanId`
     */
    @OnClick(R.id.btn_make_payment)
    fun onMakePaymentClicked() {
        (activity as BaseActivity?)?.replaceFragment(SavingsMakeTransferFragment.newInstance(loanId, loanWithAssociations?.summary?.totalOutstanding,
                Constants.TRANSFER_PAY_TO), true, R.id.container)
    }

    /**
     * Opens [LoanAccountSummaryFragment]
     */
    @OnClick(R.id.ll_summary)
    fun onLoanSummaryClicked() {
        (activity as BaseActivity?)?.replaceFragment(LoanAccountSummaryFragment.newInstance(loanWithAssociations), true, R.id.container)
    }

    /**
     * Opens [LoanRepaymentScheduleFragment]
     */
    @OnClick(R.id.ll_repayment)
    fun onRepaymentScheduleClicked() {
        (activity as BaseActivity?)?.replaceFragment(LoanRepaymentScheduleFragment.newInstance(loanId), true, R.id.container)
    }

    /**
     * Opens [LoanAccountTransactionFragment]
     */
    @OnClick(R.id.ll_loan_transactions)
    fun onTransactionsClicked() {
        (activity as BaseActivity?)?.replaceFragment(LoanAccountTransactionFragment.newInstance(loanId), true, R.id.container)
    }

    @OnClick(R.id.ll_loan_charges)
    fun chargesClicked() {
        (activity as BaseActivity?)?.replaceFragment(ClientChargeFragment.newInstance(loanWithAssociations?.id?.toLong(), ChargeType.LOAN), true, R.id.container)
    }

    @OnClick(R.id.ll_loan_qr_code)
    fun onQrCodeClicked() {
        val accountDetailsInJson = QrCodeGenerator.getAccountDetailsInString(loanWithAssociations?.accountNo,
                preferencesHelper?.officeName, AccountType.LOAN)
        (activity as BaseActivity?)?.replaceFragment(QrCodeDisplayFragment.newInstance(accountDetailsInJson), true, R.id.container)
    }

    /**
     * It is called whenever any error occurs while executing a request
     *
     * @param message Error message that tells the user about the problem.
     */
    override fun showErrorFetchingLoanAccountsDetail(message: String?) {
        if (!Network.isConnected(activity)) {
            sweetUIErrorHandler?.showSweetNoInternetUI(llAccountDetail, layoutError)
        } else {
            sweetUIErrorHandler?.showSweetErrorUI(message,
                    llAccountDetail, layoutError)
            Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
        }
    }

    @OnClick(R.id.btn_try_again)
    fun retryClicked() {
        if (Network.isConnected(context)) {
            sweetUIErrorHandler?.hideSweetErrorLayoutUI(llAccountDetail, layoutError)
            loanAccountDetailsPresenter?.loadLoanAccountDetails(loanId)
        } else {
            Toast.makeText(context, getString(R.string.internet_not_connected),
                    Toast.LENGTH_SHORT).show()
        }
    }

    override fun showProgress() {
        showProgressBar()
    }

    override fun hideProgress() {
        hideProgressBar()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        hideProgressBar()
        loanAccountDetailsPresenter?.detachView()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_loan_details, menu)
        if (showLoanUpdateOption) {
            menu.findItem(R.id.menu_update_loan).isVisible = true
            menu.findItem(R.id.menu_withdraw_loan).isVisible = true
            menu.findItem(R.id.menu_view_guarantor).isVisible = true
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.menu_update_loan) {
            (activity as BaseActivity?)?.replaceFragment(LoanApplicationFragment.newInstance(LoanState.UPDATE, loanWithAssociations), true, R.id.container)
            return true
        } else if (id == R.id.menu_withdraw_loan) {
            (activity as BaseActivity?)?.replaceFragment(LoanAccountWithdrawFragment.newInstance(loanWithAssociations), true, R.id.container)
        } else if (id == R.id.menu_view_guarantor) {
            (activity as BaseActivity?)?.replaceFragment(GuarantorListFragment.newInstance(loanId), true, R.id.container)
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        @kotlin.jvm.JvmStatic
        fun newInstance(loanId: Long): LoanAccountsDetailFragment {
            val fragment = LoanAccountsDetailFragment()
            val args = Bundle()
            args.putLong(Constants.LOAN_ID, loanId)
            fragment.arguments = args
            return fragment
        }
    }
}