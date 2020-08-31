package org.mifos.mobile.ui.fragments

import android.app.SearchManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.*
import android.widget.Toast

import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.OnPageChangeListener

import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick

import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout

import org.mifos.mobile.R
import org.mifos.mobile.models.accounts.loan.LoanAccount
import org.mifos.mobile.models.accounts.savings.SavingAccount
import org.mifos.mobile.models.accounts.share.ShareAccount
import org.mifos.mobile.presenters.AccountsPresenter
import org.mifos.mobile.ui.activities.HomeActivity
import org.mifos.mobile.ui.activities.LoanApplicationActivity
import org.mifos.mobile.ui.activities.SavingsAccountApplicationActivity
import org.mifos.mobile.ui.activities.base.BaseActivity
import org.mifos.mobile.ui.adapters.CheckBoxAdapter
import org.mifos.mobile.ui.adapters.ViewPagerAdapter
import org.mifos.mobile.ui.enums.AccountType
import org.mifos.mobile.ui.fragments.base.BaseFragment
import org.mifos.mobile.ui.views.AccountsView
import org.mifos.mobile.utils.Constants
import org.mifos.mobile.utils.MaterialDialog
import org.mifos.mobile.utils.StatusUtils

import javax.inject.Inject

/*
~This project is licensed under the open source MPL V2.
~See https://github.com/openMF/self-service-app/blob/master/LICENSE.md
*/   class ClientAccountsFragment : BaseFragment(), AccountsView {
    @JvmField
    @BindView(R.id.viewpager)
    var viewPager: ViewPager? = null

    @JvmField
    @BindView(R.id.tabs)
    var tabLayout: TabLayout? = null

    @JvmField
    @BindView(R.id.fab_create_loan)
    var fabCreateLoan: FloatingActionButton? = null

    @JvmField
    @Inject
    var accountsPresenter: AccountsPresenter? = null

    @JvmField
    @Inject
    var checkBoxAdapter: CheckBoxAdapter? = null
    private var checkBoxRecyclerView: RecyclerView? = null
    private var accountType: AccountType? = null
    private var isDialogBoxSelected = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        if (arguments != null) {
            accountType = arguments?.getSerializable(Constants.ACCOUNT_TYPE) as AccountType
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_client_accounts, container, false)
        (activity as BaseActivity?)?.activityComponent?.inject(this)
        ButterKnife.bind(this, view)
        accountsPresenter?.attachView(this)
        setToolbarTitle(getString(R.string.accounts))
        setUpViewPagerAndTabLayout()
        if (savedInstanceState == null) {
            accountsPresenter?.loadClientAccounts()
        }
        return view
    }

    /**
     * Setting up [ViewPagerAdapter] and [TabLayout] for Savings, Loans and Share
     * accounts. `accountType` is used for setting the current Fragment
     */
    private fun setUpViewPagerAndTabLayout() {
        val viewPagerAdapter = ViewPagerAdapter(childFragmentManager)
        viewPagerAdapter.addFragment(AccountsFragment.newInstance(Constants.SAVINGS_ACCOUNTS),
                getString(R.string.savings))
        viewPagerAdapter.addFragment(AccountsFragment.newInstance(Constants.LOAN_ACCOUNTS),
                getString(R.string.loan))
        viewPagerAdapter.addFragment(AccountsFragment.newInstance(Constants.SHARE_ACCOUNTS),
                getString(R.string.share))
        viewPager?.offscreenPageLimit = 2
        viewPager?.adapter = viewPagerAdapter
        when (accountType) {
            AccountType.SAVINGS -> viewPager?.currentItem = 0
            AccountType.LOAN -> viewPager?.currentItem = 1
            AccountType.SHARE -> viewPager?.currentItem = 2
        }
        tabLayout?.setupWithViewPager(viewPager)
        viewPager?.addOnPageChangeListener(object : OnPageChangeListener {
            override fun onPageScrolled(
                    position: Int, positionOffset: Float,
                    positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
                activity?.invalidateOptionsMenu()
                view?.let { (activity as HomeActivity?)?.hideKeyboard(it) }
                if (position == 0 || position == 1) {
                    fabCreateLoan?.show()
                } else {
                    fabCreateLoan?.hide()
                }
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })
    }

    /**
     * Returns tag of Fragment present at `position`
     *
     * @param position position of Fragment
     * @return Tag of Fragment
     */
    private fun getFragmentTag(position: Int): String {
        return "android:switcher:" + R.id.viewpager + ":" + position
    }

    /**
     * It provides with `shareAccounts` fetched from server which is then passed to fragment
     * implementing [AccountsView] i.e. [AccountsFragment] which further displays them
     * in a recyclerView
     *
     * @param shareAccounts [List] of [ShareAccount]
     */
    override fun showShareAccounts(shareAccounts: List<ShareAccount?>?) {
        (childFragmentManager.findFragmentByTag(getFragmentTag(2)) as AccountsView?)?.showShareAccounts(shareAccounts)
        (childFragmentManager.findFragmentByTag(getFragmentTag(2)) as AccountsView?)?.hideProgress()
    }

    /**
     * It provides with `loanAccounts` fetched from server which is then passed to fragment
     * implementing [AccountsView] i.e. [AccountsFragment] which further displays them
     * in a recyclerView
     *
     * @param loanAccounts [List] of [LoanAccount]
     */
    override fun showLoanAccounts(loanAccounts: List<LoanAccount?>?) {
        (childFragmentManager.findFragmentByTag(getFragmentTag(1)) as AccountsView?)
                ?.showLoanAccounts(loanAccounts)
        (childFragmentManager.findFragmentByTag(getFragmentTag(1)) as AccountsView?)
                ?.hideProgress()
    }

    /**
     * It provides with `savingAccounts` fetched from server which is then passed to fragment
     * implementing [AccountsView] i.e. [AccountsFragment] which further displays them
     * in a recyclerView
     *
     * @param savingAccounts [List] of [SavingAccount]
     */
    override fun showSavingsAccounts(savingAccounts: List<SavingAccount?>?) {
        (childFragmentManager.findFragmentByTag(getFragmentTag(0)) as AccountsView?)
                ?.showSavingsAccounts(savingAccounts)
        (childFragmentManager.findFragmentByTag(getFragmentTag(0)) as AccountsView?)
                ?.hideProgress()
    }

    @OnClick(R.id.fab_create_loan)
    fun createLoan() {
        when (viewPager?.currentItem) {
            0 -> startActivity(Intent(activity, SavingsAccountApplicationActivity::class.java))
            1 -> startActivity(Intent(activity, LoanApplicationActivity::class.java))
        }
    }

    /**
     * It is called whenever any error occurs while executing a request which passes errorMessage to
     * fragment implementing [AccountsView] i.e. [AccountsFragment] which further
     * displays the errorMessage
     *
     * @param errorMessage Error message that tells the user about the problem.
     */
    override fun showError(errorMessage: String?) {
        (childFragmentManager.findFragmentByTag(getFragmentTag(0)) as AccountsView?)
                ?.showError(getString(R.string.error_fetching_accounts))
        (childFragmentManager.findFragmentByTag(getFragmentTag(1)) as AccountsView?)
                ?.showError(getString(R.string.error_fetching_accounts))
        (childFragmentManager.findFragmentByTag(getFragmentTag(2)) as AccountsView?)
                ?.showError(getString(R.string.error_fetching_accounts))
        Toast.makeText(activity, errorMessage, Toast.LENGTH_SHORT).show()
    }

    override fun showProgress() {}
    override fun hideProgress() {}
    override fun onDestroyView() {
        super.onDestroyView()
        accountsPresenter?.detachView()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_account, menu)
        if (viewPager?.currentItem == 0) {
            menu.findItem(R.id.menu_filter_savings).isVisible = true
            menu.findItem(R.id.menu_filter_loan).isVisible = false
            menu.findItem(R.id.menu_filter_shares).isVisible = false
            menu.findItem(R.id.menu_search_saving).isVisible = true
            menu.findItem(R.id.menu_search_loan).isVisible = false
            menu.findItem(R.id.menu_search_share).isVisible = false
            initSearch(menu, AccountType.SAVINGS)
        } else if (viewPager?.currentItem == 1) {
            menu.findItem(R.id.menu_filter_savings).isVisible = false
            menu.findItem(R.id.menu_filter_loan).isVisible = true
            menu.findItem(R.id.menu_filter_shares).isVisible = false
            menu.findItem(R.id.menu_search_saving).isVisible = false
            menu.findItem(R.id.menu_search_loan).isVisible = true
            menu.findItem(R.id.menu_search_share).isVisible = false
            initSearch(menu, AccountType.LOAN)
        } else if (viewPager?.currentItem == 2) {
            menu.findItem(R.id.menu_filter_savings).isVisible = false
            menu.findItem(R.id.menu_filter_loan).isVisible = false
            menu.findItem(R.id.menu_filter_shares).isVisible = true
            menu.findItem(R.id.menu_search_saving).isVisible = false
            menu.findItem(R.id.menu_search_loan).isVisible = false
            menu.findItem(R.id.menu_search_share).isVisible = true
            initSearch(menu, AccountType.SHARE)
        }
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_filter_savings -> showFilterDialog(AccountType.SAVINGS)
            R.id.menu_filter_loan -> showFilterDialog(AccountType.LOAN)
            R.id.menu_filter_shares -> showFilterDialog(AccountType.SHARE)
        }
        return true
    }

    /**
     * Initializes the search option in [Menu] depending upon `account`
     *
     * @param menu    Interface for managing the items in a menu.
     * @param account An enum of [AccountType]
     */
    private fun initSearch(menu: Menu, account: AccountType) {
        val manager = activity?.getSystemService(Context.SEARCH_SERVICE) as SearchManager
        var search: SearchView? = null

        when (account) {
            AccountType.SAVINGS -> {
                search = menu.findItem(R.id.menu_search_saving).actionView as SearchView
            }
            AccountType.LOAN -> {
                search = menu.findItem(R.id.menu_search_loan).actionView as SearchView
            }
            AccountType.SHARE -> {
                search = menu.findItem(R.id.menu_search_share).actionView as SearchView
            }
        }
        val displayMetrics = DisplayMetrics()
        activity?.windowManager?.defaultDisplay?.getMetrics(displayMetrics)
        val width = displayMetrics.widthPixels
        search.maxWidth = (0.75 * width).toInt()
        search.setSearchableInfo(manager.getSearchableInfo(activity?.componentName))
        search.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                when (account) {
                    AccountType.SAVINGS -> {
                        (childFragmentManager.findFragmentByTag(
                                getFragmentTag(0)) as AccountsFragment?)?.searchSavingsAccount(newText)
                    }
                    AccountType.LOAN -> {
                        (childFragmentManager.findFragmentByTag(
                                getFragmentTag(1)) as AccountsFragment?)?.searchLoanAccount(newText)
                    }
                    AccountType.SHARE -> {
                        (childFragmentManager.findFragmentByTag(
                                getFragmentTag(2)) as AccountsFragment?)?.searchSharesAccount(newText)
                    }
                }
                return false
            }
        })
    }

    /**
     * Displays a filter dialog according to the `account` provided in the parameter
     *
     * @param account An enum of [AccountType]
     */
    private fun showFilterDialog(account: AccountType) {
        if (isDialogBoxSelected) {
            return
        }
        isDialogBoxSelected = true
        var title = ""
        val layoutManager = LinearLayoutManager(activity)
        layoutManager.orientation = RecyclerView.VERTICAL
        checkBoxRecyclerView = RecyclerView(activity!!)
        checkBoxRecyclerView?.layoutManager = layoutManager
        checkBoxRecyclerView?.adapter = checkBoxAdapter
        when (account) {
            AccountType.SAVINGS -> {
                if ((childFragmentManager.findFragmentByTag(
                                getFragmentTag(0)) as AccountsFragment?)?.getCurrentFilterList() == null) {
                    checkBoxAdapter?.statusList = StatusUtils.getSavingsAccountStatusList(activity)
                } else {
                    checkBoxAdapter?.statusList = (childFragmentManager
                            .findFragmentByTag(getFragmentTag(0)) as AccountsFragment?)?.getCurrentFilterList()
                }
                title = getString(R.string.filter_savings)
            }
            AccountType.LOAN -> {
                if ((childFragmentManager.findFragmentByTag(
                                getFragmentTag(1)) as AccountsFragment?)?.getCurrentFilterList() == null) {
                    checkBoxAdapter?.statusList = StatusUtils.getLoanAccountStatusList(activity)
                } else {
                    checkBoxAdapter?.statusList = (childFragmentManager
                            .findFragmentByTag(getFragmentTag(1)) as AccountsFragment?)?.getCurrentFilterList()
                }
                title = getString(R.string.filter_loan)
            }
            AccountType.SHARE -> {
                if ((childFragmentManager.findFragmentByTag(
                                getFragmentTag(2)) as AccountsFragment?)?.getCurrentFilterList() == null) {
                    checkBoxAdapter?.statusList = StatusUtils.getShareAccountStatusList(activity)
                } else {
                    checkBoxAdapter?.statusList = (childFragmentManager
                            .findFragmentByTag(getFragmentTag(2)) as AccountsFragment?)?.getCurrentFilterList()
                }
                title = getString(R.string.filter_share)
            }
        }
        MaterialDialog.Builder().init(activity)
                .setTitle(title)
                .setCancelable(false)
                .setMessage(getString(R.string.select_you_want))
                .addView(checkBoxRecyclerView)
                .setPositiveButton(getString(R.string.filter),
                        DialogInterface.OnClickListener { _, _ ->
                            isDialogBoxSelected = false
                            when (account) {
                                AccountType.SAVINGS -> {
                                    (childFragmentManager.findFragmentByTag(
                                            getFragmentTag(0)) as AccountsFragment?)
                                            ?.setCurrentFilterList(checkBoxAdapter?.statusList)
                                    (childFragmentManager.findFragmentByTag(
                                            getFragmentTag(0)) as AccountsFragment?)
                                            ?.filterSavingsAccount(checkBoxAdapter?.statusList)
                                }
                                AccountType.LOAN -> {
                                    (childFragmentManager.findFragmentByTag(
                                            getFragmentTag(1)) as AccountsFragment?)
                                            ?.setCurrentFilterList(checkBoxAdapter?.statusList)
                                    (childFragmentManager.findFragmentByTag(
                                            getFragmentTag(1)) as AccountsFragment?)
                                            ?.filterLoanAccount(checkBoxAdapter?.statusList)
                                }
                                AccountType.SHARE -> {
                                    (childFragmentManager.findFragmentByTag(
                                            getFragmentTag(2)) as AccountsFragment?)
                                            ?.setCurrentFilterList(checkBoxAdapter?.statusList)
                                    (childFragmentManager.findFragmentByTag(
                                            getFragmentTag(2)) as AccountsFragment?)
                                            ?.filterShareAccount(checkBoxAdapter?.statusList)
                                }
                            }
                        })
                .setNeutralButton(R.string.clear_filters,
                        DialogInterface.OnClickListener { _, _ ->
                            isDialogBoxSelected = false
                            when (account) {
                                AccountType.SAVINGS -> {
                                    (childFragmentManager.findFragmentByTag(
                                            getFragmentTag(0)) as AccountsFragment?)?.clearFilter()
                                    checkBoxAdapter?.statusList = StatusUtils.getSavingsAccountStatusList(activity)
                                    accountsPresenter?.loadAccounts(Constants.SAVINGS_ACCOUNTS)
                                }
                                AccountType.LOAN -> {
                                    (childFragmentManager.findFragmentByTag(
                                            getFragmentTag(1)) as AccountsFragment?)?.clearFilter()
                                    checkBoxAdapter?.statusList = StatusUtils.getLoanAccountStatusList(activity)
                                    accountsPresenter?.loadAccounts(Constants.LOAN_ACCOUNTS)
                                }
                                AccountType.SHARE -> {
                                    (childFragmentManager.findFragmentByTag(
                                            getFragmentTag(2)) as AccountsFragment?)?.clearFilter()
                                    checkBoxAdapter?.statusList = StatusUtils.getShareAccountStatusList(activity)
                                    accountsPresenter?.loadAccounts(Constants.SHARE_ACCOUNTS)
                                }
                            }

                        })
                .setNegativeButton(getString(R.string.cancel),
                        DialogInterface.OnClickListener { _, _ ->
                            isDialogBoxSelected = false
                        }
                )
                .createMaterialDialog()
                .show()
    }

    override fun onResume() {
        super.onResume()
        (activity as BaseActivity?)?.hideToolbarElevation()
    }

    override fun onPause() {
        super.onPause()
        (activity as BaseActivity?)?.setToolbarElevation()
    }

    companion object {
        fun newInstance(accountType: AccountType?): ClientAccountsFragment {
            val clientAccountsFragment = ClientAccountsFragment()
            val args = Bundle()
            args.putSerializable(Constants.ACCOUNT_TYPE, accountType)
            clientAccountsFragment.arguments = args
            return clientAccountsFragment
        }
    }
}