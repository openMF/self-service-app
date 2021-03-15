package org.mifos.mobile.ui.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.view.*
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.github.therajanmaurya.sweeterror.SweetUIErrorHandler
import org.mifos.mobile.R
import org.mifos.mobile.api.local.PreferencesHelper
import org.mifos.mobile.models.accounts.savings.SavingsWithAssociations
import org.mifos.mobile.models.accounts.savings.Status
import org.mifos.mobile.presenters.SavingAccountsDetailPresenter
import org.mifos.mobile.ui.activities.base.BaseActivity
import org.mifos.mobile.ui.enums.AccountType
import org.mifos.mobile.ui.enums.ChargeType
import org.mifos.mobile.ui.enums.SavingsAccountState
import org.mifos.mobile.ui.fragments.base.BaseFragment
import org.mifos.mobile.ui.views.SavingAccountsDetailView
import org.mifos.mobile.utils.*
import javax.inject.Inject

/**
 * @author Vishwajeet
 * @since 18/8/16.
 */
class SavingAccountsDetailFragment : BaseFragment(), SavingAccountsDetailView {
    @kotlin.jvm.JvmField
    @BindView(R.id.tv_account_status)
    var tvAccountStatus: TextView? = null

    @kotlin.jvm.JvmField
    @BindView(R.id.iv_circle_status)
    var ivCircularStatus: CircularImageView? = null

    @kotlin.jvm.JvmField
    @BindView(R.id.tv_total_withdrawals)
    var tvTotalWithdrawals: TextView? = null

    @kotlin.jvm.JvmField
    @BindView(R.id.tv_min_req_bal)
    var tvMiniRequiredBalance: TextView? = null

    @kotlin.jvm.JvmField
    @BindView(R.id.tv_saving_account_number)
    var tvSavingAccountNumber: TextView? = null

    @kotlin.jvm.JvmField
    @BindView(R.id.tv_nominal_interest_rate)
    var tvNominalInterestRate: TextView? = null

    @kotlin.jvm.JvmField
    @BindView(R.id.tv_total_deposits)
    var tvTotalDeposits: TextView? = null

    @kotlin.jvm.JvmField
    @BindView(R.id.tv_acc_balance)
    var tvAccountBalanceMain: TextView? = null

    @kotlin.jvm.JvmField
    @BindView(R.id.tv_last_transaction)
    var tvLastTransaction: TextView? = null

    @kotlin.jvm.JvmField
    @BindView(R.id.made_on)
    var tvMadeOnTextView: TextView? = null

    @kotlin.jvm.JvmField
    @BindView(R.id.tv_made_on)
    var tvMadeOnTransaction: TextView? = null

    @kotlin.jvm.JvmField
    @BindView(R.id.ll_account)
    var layoutAccount: LinearLayout? = null

    @kotlin.jvm.JvmField
    @BindView(R.id.layout_error)
    var layoutError: View? = null

    @kotlin.jvm.JvmField
    @BindView(R.id.tv_minRequiredBalance)
    var tvMinRequiredBalanceLabel: TextView? = null

    @kotlin.jvm.JvmField
    @Inject
    var preferencesHelper: PreferencesHelper? = null

    @kotlin.jvm.JvmField
    @Inject
    var savingAccountsDetailPresenter: SavingAccountsDetailPresenter? = null
    private var rootView: View? = null
    private var savingsId: Long? = 0
    private var status: Status? = null
    private var savingsWithAssociations: SavingsWithAssociations? = null
    private var sweetUIErrorHandler: SweetUIErrorHandler? = null
    private var isMenuVisible:Boolean? = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            savingsId = arguments?.getLong(Constants.SAVINGS_ID)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.fragment_saving_account_details, container, false)
        (activity as BaseActivity?)?.activityComponent?.inject(this)
        setToolbarTitle(getString(R.string.saving_account_details))
        ButterKnife.bind(this, rootView!!)
        savingAccountsDetailPresenter?.attachView(this)
        sweetUIErrorHandler = SweetUIErrorHandler(context, rootView)
        if (savedInstanceState == null) {
            savingAccountsDetailPresenter?.loadSavingsWithAssociations(savingsId)
        }
        setHasOptionsMenu(true)
        return rootView
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(Constants.SAVINGS_ACCOUNTS, savingsWithAssociations)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (savedInstanceState != null) {
            showSavingAccountsDetail(savedInstanceState.getParcelable<Parcelable>(Constants.SAVINGS_ACCOUNTS) as SavingsWithAssociations)
        }
    }

    /**
     * Opens up Phone Dialer
     */
    @OnClick(R.id.tv_help_line_number)
    fun dialHelpLineNumber() {
        val intent = Intent(Intent.ACTION_DIAL)
        intent.data = Uri.parse("tel:" + getString(R.string.help_line_number))
        startActivity(intent)
    }

    /**
     * Opens [SavingsMakeTransferFragment] if status is ACTIVE else shows a
     * {@link Snackbar} that Account should be Active
     */
    @OnClick(R.id.tv_deposit)
    fun deposit() {
        if (status?.active == true) {
            (activity as BaseActivity?)?.replaceFragment(SavingsMakeTransferFragment.newInstance(savingsId, Constants.TRANSFER_PAY_TO), true, R.id.container)
        } else {
            Toaster.show(rootView, getString(R.string.account_not_active_to_perform_deposit))
        }
    }

    /**
     * Opens [SavingsMakeTransferFragment] if status is ACTIVE else shows a
     * {@link Snackbar} that Account should be Active
     */
    @OnClick(R.id.tv_make_a_transfer)
    fun transfer() {
        if (status?.active == true) {
            (activity as BaseActivity?)?.replaceFragment(SavingsMakeTransferFragment.newInstance(savingsId, Constants.TRANSFER_PAY_FROM), true, R.id.container)
        } else {
            Toaster.show(rootView, getString(R.string.account_not_active_to_perform_transfer))
        }
    }

    /**
     * Sets Saving account basic info fetched from the server
     *
     * @param savingsWithAssociations object containing details of a saving account
     */
    override fun showSavingAccountsDetail(savingsWithAssociations: SavingsWithAssociations?) {
        if (savingsWithAssociations?.status?.submittedAndPendingApproval == true) {
            sweetUIErrorHandler?.showSweetCustomErrorUI(getString(R.string.approval_pending),
                    R.drawable.ic_assignment_turned_in_black_24dp, layoutAccount, layoutError)
            isMenuVisible = (savingsWithAssociations.status?.submittedAndPendingApproval == true)
        } else {
            layoutAccount?.visibility = View.VISIBLE
            val currencySymbol = savingsWithAssociations?.currency?.displaySymbol
            val accountBalance = savingsWithAssociations?.summary?.accountBalance
            tvAccountStatus?.text = savingsWithAssociations?.clientName
            if (savingsWithAssociations?.minRequiredOpeningBalance != null) {
                tvMiniRequiredBalance?.text = getString(R.string.string_and_string, currencySymbol,
                        CurrencyUtil.formatCurrency(activity, savingsWithAssociations.minRequiredOpeningBalance!!))
            } else {
                tvMinRequiredBalanceLabel?.visibility = View.GONE
                tvMiniRequiredBalance?.visibility = View.GONE
            }
            if (savingsWithAssociations?.summary?.totalWithdrawals != null) {
                tvTotalWithdrawals?.text = getString(R.string.string_and_string, currencySymbol,
                        CurrencyUtil.formatCurrency(activity, savingsWithAssociations.summary?.totalWithdrawals!!))
            } else {
                tvTotalWithdrawals?.setText(R.string.no_withdrawals)
            }
            tvAccountBalanceMain?.text = getString(R.string.string_and_string,
                    currencySymbol, CurrencyUtil.formatCurrency(activity, accountBalance!!))
            tvNominalInterestRate?.text = getString(R.string.double_and_string,
                    savingsWithAssociations.getNominalAnnualInterestRate(), SymbolsUtils.PERCENT)
            tvSavingAccountNumber?.text = savingsWithAssociations.accountNo.toString()
            if (savingsWithAssociations.summary?.totalDeposits != null) {
                tvTotalDeposits?.text = getString(R.string.string_and_string, currencySymbol,
                        CurrencyUtil.formatCurrency(activity, savingsWithAssociations.summary?.totalDeposits!!))
            } else {
                tvTotalDeposits?.text = getString(R.string.not_available)
            }
            if (savingsWithAssociations.transactions.isNotEmpty()) {
                tvLastTransaction?.text = getString(R.string.string_and_double,
                        currencySymbol,
                        savingsWithAssociations.getLastTransaction()?.amount)
                tvMadeOnTransaction?.text = DateHelper.getDateAsString(
                        savingsWithAssociations.getLastTransaction()?.date)
            } else {
                tvLastTransaction?.setText(R.string.no_transaction)
                tvMadeOnTransaction?.visibility = View.GONE
                tvMadeOnTextView?.visibility = View.GONE
            }
            showAccountStatus(savingsWithAssociations)
        }
        this.savingsWithAssociations = savingsWithAssociations
        activity?.invalidateOptionsMenu()
        showAccountStatus(savingsWithAssociations)
    }

    /**
     * It is called whenever any error occurs while executing a request
     *
     * @param message Error message that tells the user about the problem.
     */
    override fun showErrorFetchingSavingAccountsDetail(message: String?) {
        if (!Network.isConnected(context)) {
            sweetUIErrorHandler?.showSweetNoInternetUI(layoutAccount, layoutError)
            Toast.makeText(context, getString(R.string.internet_not_connected),
                    Toast.LENGTH_SHORT).show()
        } else {
            sweetUIErrorHandler?.showSweetErrorUI(message, layoutAccount, layoutError)
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    @OnClick(R.id.btn_try_again)
    fun onRetry() {
        if (!Network.isConnected(context)) {
            Toast.makeText(context, getString(R.string.internet_not_connected),
                    Toast.LENGTH_SHORT).show()
        } else {
            sweetUIErrorHandler?.hideSweetErrorLayoutUI(layoutAccount, layoutError)
            savingAccountsDetailPresenter?.loadSavingsWithAssociations(savingsId)
        }
    }

    /**
     * Sets the status of account i.e. `tvAccountStatus` and `ivCircularStatus` color
     * according to `savingsWithAssociations`
     *
     * @param savingsWithAssociations object containing details of a saving account
     */
    override fun showAccountStatus(savingsWithAssociations: SavingsWithAssociations?) {
        status = savingsWithAssociations?.status
        when {
            status?.active == true -> {
                ivCircularStatus?.setImageDrawable(
                        Utils.setCircularBackground(R.color.deposit_green, activity))
                tvAccountStatus?.setText(R.string.active)
            }
            status?.approved == true -> {
                ivCircularStatus?.setImageDrawable(
                        Utils.setCircularBackground(R.color.blue, activity))
                tvAccountStatus?.setText(R.string.need_approval)
            }
            status?.submittedAndPendingApproval == true -> {
                ivCircularStatus?.setImageDrawable(
                        Utils.setCircularBackground(R.color.light_yellow, activity))
                tvAccountStatus?.setText(R.string.pending)
            }
            status?.matured == true -> {
                ivCircularStatus?.setImageDrawable(
                        Utils.setCircularBackground(R.color.red_light, activity))
                tvAccountStatus?.setText(R.string.matured)
            }
            else -> {
                ivCircularStatus?.setImageDrawable(
                        Utils.setCircularBackground(R.color.black, activity))
                tvAccountStatus?.setText(R.string.closed)
            }
        }
        activity?.invalidateOptionsMenu()
    }

    override fun showProgress() {
        layoutAccount?.visibility = View.GONE
        showProgressBar()
    }

    override fun hideProgress() {
        layoutAccount?.visibility = View.VISIBLE
        hideProgressBar()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        hideProgressBar()
        savingAccountsDetailPresenter?.detachView()
    }

    @OnClick(R.id.ll_savings_transactions)
    fun transactionsClicked() {
        (activity as BaseActivity?)?.replaceFragment(SavingAccountsTransactionFragment.newInstance(savingsId), true, R.id.container)
    }

    @OnClick(R.id.ll_savings_charges)
    fun chargeClicked() {
        (activity as BaseActivity?)?.replaceFragment(ClientChargeFragment.newInstance(savingsId, ChargeType.SAVINGS), true, R.id.container)
    }

    @OnClick(R.id.ll_savings_qr_code)
    fun qrCodeClicked() {
        val accountDetailsInJson = QrCodeGenerator.getAccountDetailsInString(
                savingsWithAssociations?.accountNo, preferencesHelper?.officeName,
                AccountType.SAVINGS)
        (activity as BaseActivity?)?.replaceFragment(QrCodeDisplayFragment.newInstance(accountDetailsInJson), true, R.id.container)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_savings_account_detail, menu)
        if (isMenuVisible == true) {
            menu.findItem(R.id.menu_withdraw_savings_account).isVisible = true
            menu.findItem(R.id.menu_update_savings_account).isVisible = true
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        when (id) {
            R.id.menu_withdraw_savings_account -> (activity as BaseActivity?)?.replaceFragment(
                    SavingsAccountWithdrawFragment.newInstance(savingsWithAssociations),
                    true, R.id.container)
            R.id.menu_update_savings_account -> (activity as BaseActivity?)?.replaceFragment(SavingsAccountApplicationFragment.newInstance(SavingsAccountState.UPDATE, savingsWithAssociations),
                    true, R.id.container)
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        @kotlin.jvm.JvmStatic
        fun newInstance(savingsId: Long): SavingAccountsDetailFragment {
            val fragment = SavingAccountsDetailFragment()
            val args = Bundle()
            args.putLong(Constants.SAVINGS_ID, savingsId)
            fragment.arguments = args
            return fragment
        }
    }
}