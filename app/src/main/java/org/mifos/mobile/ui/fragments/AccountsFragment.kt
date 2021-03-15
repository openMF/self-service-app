package org.mifos.mobile.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener

import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick

import com.github.therajanmaurya.sweeterror.SweetUIErrorHandler

import org.mifos.mobile.R
import org.mifos.mobile.models.CheckboxStatus
import org.mifos.mobile.models.accounts.loan.LoanAccount
import org.mifos.mobile.models.accounts.savings.SavingAccount
import org.mifos.mobile.models.accounts.share.ShareAccount
import org.mifos.mobile.presenters.AccountsPresenter
import org.mifos.mobile.ui.activities.LoanAccountContainerActivity
import org.mifos.mobile.ui.activities.SavingsAccountContainerActivity
import org.mifos.mobile.ui.activities.base.BaseActivity
import org.mifos.mobile.ui.adapters.LoanAccountsListAdapter
import org.mifos.mobile.ui.adapters.SavingAccountsListAdapter
import org.mifos.mobile.ui.adapters.ShareAccountsListAdapter
import org.mifos.mobile.ui.fragments.base.BaseFragment
import org.mifos.mobile.ui.views.AccountsView
import org.mifos.mobile.utils.*

import java.util.*
import javax.inject.Inject

import kotlin.collections.ArrayList

/**
 * Created by Rajan Maurya on 23/10/16.
 */
class AccountsFragment : BaseFragment(), OnRefreshListener, AccountsView, RecyclerItemClickListener.OnItemClickListener {
    @kotlin.jvm.JvmField
    @BindView(R.id.rv_accounts)
    var rvAccounts: RecyclerView? = null

    @kotlin.jvm.JvmField
    @BindView(R.id.swipe_container)
    var swipeRefreshLayout: SwipeRefreshLayout? = null

    @kotlin.jvm.JvmField
    @BindView(R.id.layout_error)
    var layoutError: View? = null

    @kotlin.jvm.JvmField
    @Inject
    var accountsPresenter: AccountsPresenter? = null

    @kotlin.jvm.JvmField
    @Inject
    var loanAccountsListAdapter: LoanAccountsListAdapter? = null

    @kotlin.jvm.JvmField
    @Inject
    var savingAccountsListAdapter: SavingAccountsListAdapter? = null

    @kotlin.jvm.JvmField
    @Inject
    var shareAccountsListAdapter: ShareAccountsListAdapter? = null
    private var accountType: String? = null
    private var loanAccounts: List<LoanAccount?>? = null
    private var savingAccounts: List<SavingAccount?>? = null
    private var shareAccounts: List<ShareAccount?>? = null
    private var currentFilterList: List<CheckboxStatus?>? = null

    /**
     * Method to get the current filter list for the fragment
     *
     * @return currentFilterList
     */
    fun getCurrentFilterList(): List<CheckboxStatus?>? {
        return currentFilterList
    }

    /**
     * Method to set current filter list value
     */
    fun setCurrentFilterList(currentFilterList: List<CheckboxStatus?>?) {
        this.currentFilterList = currentFilterList
    }

    private var sweetUIErrorHandler: SweetUIErrorHandler? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (activity as BaseActivity?)?.activityComponent?.inject(this)
        loanAccounts = ArrayList()
        savingAccounts = ArrayList()
        shareAccounts = ArrayList()
        if (arguments != null) {
            accountType = arguments?.getString(Constants.ACCOUNT_TYPE)
        }
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_accounts, container, false)
        ButterKnife.bind(this, rootView)
        accountsPresenter?.attachView(this)
        sweetUIErrorHandler = SweetUIErrorHandler(activity, rootView)
        val layoutManager = LinearLayoutManager(activity)
        layoutManager.orientation = RecyclerView.VERTICAL
        rvAccounts?.layoutManager = layoutManager
        rvAccounts?.setHasFixedSize(true)
        rvAccounts?.addItemDecoration(DividerItemDecoration(activity,
                layoutManager.orientation))
        rvAccounts?.addOnItemTouchListener(RecyclerItemClickListener(activity, this))
        swipeRefreshLayout?.setColorSchemeColors(*activity!!
                .resources.getIntArray(R.array.swipeRefreshColors))
        swipeRefreshLayout?.setOnRefreshListener(this)
        if (savedInstanceState == null) {
            showProgress()
        }
        return rootView
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(Constants.ACCOUNT_TYPE, accountType)
        when (accountType) {
            Constants.SAVINGS_ACCOUNTS -> outState.putParcelableArrayList(Constants.SAVINGS_ACCOUNTS,
                    ArrayList<Parcelable?>(savingAccounts))
            Constants.LOAN_ACCOUNTS -> outState.putParcelableArrayList(Constants.LOAN_ACCOUNTS,
                    ArrayList<Parcelable?>(loanAccounts))
            Constants.SHARE_ACCOUNTS -> outState.putParcelableArrayList(Constants.SHARE_ACCOUNTS,
                    ArrayList<Parcelable?>(shareAccounts))
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (savedInstanceState != null) {
            accountType = savedInstanceState.getString(Constants.ACCOUNT_TYPE)
            when (accountType) {
                Constants.SAVINGS_ACCOUNTS -> {
                    val savingAccountList: List<SavingAccount?> =
                            savedInstanceState.getParcelableArrayList(Constants.SAVINGS_ACCOUNTS)
                    showSavingsAccounts(savingAccountList)
                }
                Constants.LOAN_ACCOUNTS -> {
                    val loanAccountList: List<LoanAccount?> =
                            savedInstanceState.getParcelableArrayList(
                                    Constants.LOAN_ACCOUNTS)
                    showLoanAccounts(loanAccountList)
                }
                Constants.SHARE_ACCOUNTS -> {
                    val shareAccountList: List<ShareAccount?> =
                            savedInstanceState.getParcelableArrayList(
                                    Constants.SHARE_ACCOUNTS)
                    showShareAccounts(shareAccountList)
                }
            }
        }
    }

    /**
     * Used for reloading account of a particular `accountType` in case of a network error.
     */
    @OnClick(R.id.btn_try_again)
    fun onRetry() {
        if (Network.isConnected(context)) {
            sweetUIErrorHandler?.hideSweetErrorLayoutUI(rvAccounts, layoutError)
            accountsPresenter?.loadAccounts(accountType)
        } else {
            Toast.makeText(context, getString(R.string.internet_not_connected),
                    Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * This method is called when we swipe to refresh the view and is used for reloading account of
     * a particular `accountType`
     */
    override fun onRefresh() {
        if (Network.isConnected(context)) {
            clearFilter()
            accountsPresenter?.loadAccounts(accountType)
        } else {
            hideProgress()
            sweetUIErrorHandler?.showSweetNoInternetUI(rvAccounts, layoutError)
            Toast.makeText(context, getString(R.string.internet_not_connected),
                    Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Method to clear the current filters and set
     * currentFilterList = null
     */
    fun clearFilter() {
        sweetUIErrorHandler?.hideSweetErrorLayoutUI(rvAccounts, layoutError)
        currentFilterList = null
    }

    override fun onResume() {
        super.onResume()
        onRefresh()
    }

    /**
     * Shows [List] of [LoanAccount] fetched from server using
     * [LoanAccountsListAdapter]
     *
     * @param loanAccounts [List] of [LoanAccount]
     */
    override fun showLoanAccounts(loanAccounts: List<LoanAccount?>?) {
        Collections.sort(loanAccounts, ComparatorBasedOnId())
        this.loanAccounts = loanAccounts
        if (loanAccounts?.isNotEmpty() == true) {
            loanAccountsListAdapter?.setLoanAccountsList(loanAccounts)
            rvAccounts?.adapter = loanAccountsListAdapter
        } else {
            showEmptyAccounts(getString(R.string.loan_account))
        }
    }

    /**
     * Shows [List] of [SavingAccount] fetched from server using
     * [SavingAccountsListAdapter]
     *
     * @param savingAccounts [List] of [SavingAccount]
     */
    override fun showSavingsAccounts(savingAccounts: List<SavingAccount?>?) {
        Collections.sort(savingAccounts, ComparatorBasedOnId())
        this.savingAccounts = savingAccounts
        if (savingAccounts?.isNotEmpty() == true) {
            savingAccountsListAdapter?.setSavingAccountsList(savingAccounts)
            rvAccounts?.adapter = savingAccountsListAdapter
        } else {
            showEmptyAccounts(getString(R.string.savings_account))
        }
    }

    /**
     * Shows [List] of [ShareAccount] fetched from server using
     * [ShareAccountsListAdapter]
     *
     * @param shareAccounts [List] of [ShareAccount]
     */
    override fun showShareAccounts(shareAccounts: List<ShareAccount?>?) {
        Collections.sort(shareAccounts, ComparatorBasedOnId())
        this.shareAccounts = shareAccounts
        if (shareAccounts?.isNotEmpty() == true) {
            shareAccountsListAdapter?.setShareAccountsList(shareAccounts)
            rvAccounts?.adapter = shareAccountsListAdapter
        } else {
            showEmptyAccounts(getString(R.string.share_account))
        }
    }

    /**
     * Shows an error layout when this function is called.
     *
     * @param emptyAccounts Text to show in `noAccountText`
     */
    private fun showEmptyAccounts(emptyAccounts: String?) {
        sweetUIErrorHandler?.showSweetEmptyUI(emptyAccounts,
                R.drawable.ic_account_balance_wallet_black_background_24dp,
                rvAccounts, layoutError)
    }

    /**
     * Used for searching an `input` String in `savingAccounts` and displaying it in the
     * recyclerview.
     *
     * @param input String which is needs to be searched in list
     */
    fun searchSavingsAccount(input: String?) {
        val searchResult = accountsPresenter?.searchInSavingsList(savingAccounts, input)
        if (searchResult?.size == 0) {
            showEmptyAccounts(getString(R.string.no_saving_account))
        } else {
            sweetUIErrorHandler?.hideSweetErrorLayoutUI(rvAccounts, layoutError)
            savingAccountsListAdapter?.setSavingAccountsList(searchResult)
        }
    }

    /**
     * Used for searching an `input` String in `loanAccounts` and displaying it in the
     * recyclerview.
     *
     * @param input String which is needs to be searched in list
     */
    fun searchLoanAccount(input: String?) {
        val searchResult: List<LoanAccount?>? = accountsPresenter?.searchInLoanList(loanAccounts, input)
        if (searchResult?.size == 0) {
            showEmptyAccounts(getString(R.string.no_loan_account))
        } else {
            sweetUIErrorHandler?.hideSweetErrorLayoutUI(rvAccounts, layoutError)
            loanAccountsListAdapter?.setLoanAccountsList(searchResult)
        }
    }

    /**
     * Used for searching an `input` String in `savingAccounts` and displaying it in the
     * recyclerview.
     *
     * @param input String which is needs to be searched in list
     */
    fun searchSharesAccount(input: String?) {
        val searchResult: List<ShareAccount?>? = accountsPresenter?.searchInSharesList(shareAccounts, input)
        if (searchResult?.size == 0) {
            showEmptyAccounts(getString(R.string.no_sharing_account))
        } else {
            sweetUIErrorHandler?.hideSweetErrorLayoutUI(rvAccounts, layoutError)
            shareAccountsListAdapter?.setShareAccountsList(searchResult)
        }
    }

    /**
     * Used for filtering `savingAccounts` depending upon [List] of
     * [CheckboxStatus]
     *
     * @param statusModelList [List] of [CheckboxStatus]
     */
    fun filterSavingsAccount(statusModelList: List<CheckboxStatus?>?) {
        val filteredSavings: MutableList<SavingAccount?>? = ArrayList()
        if (accountsPresenter?.getCheckedStatus(statusModelList) != null && accountsPresenter != null) {
            for (status in accountsPresenter?.getCheckedStatus(statusModelList)!!) {
                accountsPresenter?.getFilteredSavingsAccount(savingAccounts,
                        status)?.let { filteredSavings?.addAll(it) }
            }
        }
        if (filteredSavings?.size == 0) {
            showEmptyAccounts(getString(R.string.no_saving_account))
        } else {
            savingAccountsListAdapter?.setSavingAccountsList(filteredSavings)
        }
    }

    /**
     * Used for filtering `loanAccounts` depending upon [List] of
     * [CheckboxStatus]
     *
     * @param statusModelList [List] of [CheckboxStatus]
     */
    fun filterLoanAccount(statusModelList: List<CheckboxStatus?>?) {
        val filteredSavings: MutableList<LoanAccount?>? = ArrayList()
        when {
            accountsPresenter != null && accountsPresenter?.getCheckedStatus(statusModelList) != null -> {
                for (status in accountsPresenter?.getCheckedStatus(statusModelList)!!) {
                    accountsPresenter?.getFilteredLoanAccount(loanAccounts,
                            status)?.let { filteredSavings?.addAll(it) }
                }
            }
            filteredSavings?.size == 0 -> {
                showEmptyAccounts(getString(R.string.no_loan_account))
            }
            else -> {
                loanAccountsListAdapter?.setLoanAccountsList(filteredSavings)
            }
        }
    }

    /**
     * Used for filtering `shareAccounts` depending upon [List] of
     * [CheckboxStatus]
     *
     * @param statusModelList [List] of [CheckboxStatus]
     */
    fun filterShareAccount(statusModelList: List<CheckboxStatus?>?) {
        sweetUIErrorHandler?.hideSweetErrorLayoutUI(rvAccounts, layoutError)
        var nonEmpty = false
        statusModelList!!.forEach { s ->
            if (s!!.isChecked) nonEmpty = true
        }
        val filteredSavings: MutableList<ShareAccount?>? = ArrayList()
        if (accountsPresenter?.getCheckedStatus(statusModelList) != null &&
                accountsPresenter != null && nonEmpty) {
            if (accountsPresenter != null && accountsPresenter?.getCheckedStatus(statusModelList) != null) {
                for (status in accountsPresenter?.getCheckedStatus(statusModelList)!!) {
                    accountsPresenter?.getFilteredShareAccount(shareAccounts,
                            status)?.let { filteredSavings?.addAll(it) }
                }
            }
        } else filteredSavings?.addAll(shareAccounts!!)

        if (filteredSavings?.size == 0) {
            showEmptyAccounts(getString(R.string.no_sharing_account))
        } else {
            shareAccountsListAdapter?.setShareAccountsList(filteredSavings)
        }
    }

    /**
     * It is called whenever any error occurs while executing a request
     *
     * @param errorMessage Error message that tells the user about the problem.
     */
    override fun showError(errorMessage: String?) {
        if (!Network.isConnected(context)) {
            sweetUIErrorHandler?.showSweetNoInternetUI(rvAccounts, layoutError)
        } else {
            sweetUIErrorHandler?.showSweetErrorUI(errorMessage, rvAccounts, layoutError)
            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
        }
        hideProgress()
    }

    /**
     * Shows [SwipeRefreshLayout]
     */
    override fun showProgress() {
        showSwipeRefreshLayout(true)
    }

    /**
     * Hides [SwipeRefreshLayout]
     */
    override fun hideProgress() {
        showSwipeRefreshLayout(false)
    }

    private fun showSwipeRefreshLayout(show: Boolean) {
        swipeRefreshLayout?.isRefreshing = show
    }

    override fun onDestroyView() {
        super.onDestroyView()
        accountsPresenter?.detachView()
    }

    override fun onItemClick(childView: View?, position: Int) {
        var intent: Intent? = null
        when (accountType) {
            Constants.SAVINGS_ACCOUNTS -> {
                intent = Intent(activity, SavingsAccountContainerActivity::class.java)
                intent.putExtra(Constants.SAVINGS_ID, savingAccountsListAdapter
                        ?.getSavingAccountsList()?.get(position)?.id)
            }
            Constants.LOAN_ACCOUNTS -> {
                intent = Intent(activity, LoanAccountContainerActivity::class.java)
                intent.putExtra(Constants.LOAN_ID, loanAccountsListAdapter?.getLoanAccountsList()
                        ?.get(position)?.id)
            }
        }
        openActivity(intent)
    }

    override fun onItemLongPress(childView: View?, position: Int) {}

    /**
     * This function opens up an activity only if the intent
     * is not null.
     *
     * This will prevent the application from crashing if the
     * intent is null.
     */
    private fun openActivity(intent: Intent?) {
        intent?.let { startActivity(it) }
    }

    companion object {
        val LOG_TAG: String? = AccountsFragment::class.java.simpleName
        fun newInstance(accountType: String?): AccountsFragment {
            val fragment = AccountsFragment()
            val args = Bundle()
            args.putString(Constants.ACCOUNT_TYPE, accountType)
            fragment.arguments = args
            return fragment
        }
    }
}