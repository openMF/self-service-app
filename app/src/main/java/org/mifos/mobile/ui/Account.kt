package org.mifos.mobile.ui

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.SearchView
import androidx.viewpager.widget.ViewPager
import kotlinx.android.synthetic.main.fragment_client_accounts.*
import org.mifos.mobile.R
import org.mifos.mobile.models.accounts.loan.LoanAccount
import org.mifos.mobile.models.accounts.savings.SavingAccount
import org.mifos.mobile.models.client.DepositType
import org.mifos.mobile.presenters.AccountsPresenter
import org.mifos.mobile.ui.activities.LoanApplicationActivity
import org.mifos.mobile.ui.activities.base.BaseActivity
import org.mifos.mobile.ui.adapters.ViewPagerAdapter
import org.mifos.mobile.ui.enums.AccountType
import org.mifos.mobile.ui.fragments.AccountsFragment
import org.mifos.mobile.ui.fragments.ClientAccountsFragment
import org.mifos.mobile.ui.fragments.base.BaseFragment
import org.mifos.mobile.utils.Constants
import org.mifos.mobile.utils.StatusUtils
import javax.inject.Inject

class Account : BaseFragment(), AccountsContract.View {
    private lateinit var accountType: AccountType


    @Inject
    internal lateinit var accountsPresenter: AccountsPresenter

    companion object {
        fun newInstance(accountType: AccountType): ClientAccountsFragment {
            val fragment = ClientAccountsFragment()
            val args = Bundle()
            args.putSerializable(Constants.ACCOUNT_TYPE, accountType)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        if (arguments != null) {
            accountType = arguments!!.getSerializable(Constants.ACCOUNT_TYPE) as AccountType
        }

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootview: View = inflater.inflate(R.layout.fragment_client_accounts,
                container, false)
        return rootview

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpViewPagerAndTabLayout()
        setToolbarTitle(getString(R.string.accounts))
    }

    /**
     * Returns tag of Fragment present at `position`
     * @param position position of Fragment
     * @return Tag of Fragment
     */
    private fun getFragmentTag(position: Int): String {
        return "android:switcher:" + R.id.viewpager + ":" + position
    }


    private fun setUpViewPagerAndTabLayout() {
        val viewPagerAdapter = ViewPagerAdapter(childFragmentManager)
        viewPagerAdapter.addFragment(AccountsFragment.newInstance(Constants.SAVINGS_ACCOUNTS),
                getString(R.string.deposit))
        viewPagerAdapter.addFragment(AccountsFragment.newInstance(Constants.LOAN_ACCOUNTS),
                getString(R.string.loan))
        viewpager.adapter = viewPagerAdapter
        viewpager.offscreenPageLimit = 2
        when (accountType) {
            AccountType.SAVINGS -> viewpager.currentItem = 0
            AccountType.LOAN -> viewpager.currentItem = 1
        }

        deposit_toggle_btn.setOnClickListener {
            viewpager.currentItem = 0
        }
        loan_toggle_btn.setOnClickListener {
            viewpager.currentItem = 1
        }

        tabs.setupWithViewPager(viewpager)
        viewpager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float,
                                        positionOffsetPixels: Int) {
                activity?.invalidateOptionsMenu()
            }

            override fun onPageSelected(position: Int) {}

            override fun onPageScrollStateChanged(state: Int) {}
        })


    }

    override fun showLoanAccounts(loanAccount: List<LoanAccount>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun showDepositAccounts(depositType: DepositType) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
    override fun showError(message: String) {
        (childFragmentManager.findFragmentByTag(getFragmentTag(1)) as AccountsContract.View)
                .showError(getString(R.string.loan))
        (childFragmentManager.findFragmentByTag(getFragmentTag(0)) as AccountsContract.View)
                .showError(getString(R.string.deposit))
    }

    override fun showEmptyAccounts(feature: String) {
        (childFragmentManager.findFragmentByTag(getFragmentTag(1)) as AccountsContract.View)
                .showEmptyAccounts(getString(R.string.loan))
        (childFragmentManager.findFragmentByTag(getFragmentTag(0)) as AccountsContract.View)
                .showEmptyAccounts(getString(R.string.deposit))
    }


    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.menu_account, menu)
        if (viewpager.currentItem == 1) {
            loan_toggle_focus_btn.visibility = View.VISIBLE
            deposit_toggle_focus_btn.visibility = View.GONE
            iv_apply_for_loan.visibility = View.VISIBLE
            applyForLoan()
            menu?.findItem(R.id.menu_filter_loan)?.isVisible = true
            menu?.findItem(R.id.menu_search_saving)?.isVisible = true
            menu?.findItem(R.id.menu_search_share)?.isVisible = false
            initSearch(menu!!, AccountType.LOAN)
        } else if (viewpager.currentItem == 0) {
            deposit_toggle_focus_btn.visibility = View.VISIBLE
            loan_toggle_focus_btn.visibility = View.GONE
            iv_apply_for_loan.visibility = View.GONE
            menu?.findItem(R.id.menu_filter_loan)?.isVisible = false
            menu?.findItem(R.id.menu_filter_savings)?.isVisible = true
            menu?.findItem(R.id.menu_search_share)?.isVisible = true
            menu?.findItem(R.id.menu_search_loan)?.isVisible = false
            initSearch(menu!!, AccountType.SAVINGS)
        }
        super.onCreateOptionsMenu(menu, inflater)

    }

    private fun applyForLoan() {
        iv_apply_for_loan.setOnClickListener(View.OnClickListener {
            val intent = Intent(activity, LoanApplicationActivity::class.java)
            intent.putExtra(Constants.CUSTOMER_IDENTIFIER, "customer_identifier")
            startActivity(intent)
        })
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.menu_filter_loan -> showFilterDialog(AccountType.LOAN)
            R.id.menu_filter_savings -> showFilterDialog(AccountType.SAVINGS)
        }
        return true
    }

    private fun showFilterDialog(accountType: AccountType) {
        val accountFilterBottomSheet = Account()
        when (accountType) {

        }

        accountFilterBottomSheet.accountType = accountType
    }

    /**
     * Initializes the search option in [Menu] depending upon `account`
     * @param menu Interface for managing the items in a menu.
     * @param account An enum of [AccountType]
     */
    private fun initSearch(menu: Menu, account: AccountType) {
        val manager = activity?.getSystemService(Context.SEARCH_SERVICE) as SearchManager
        var search: SearchView? = null

        if (account == AccountType.LOAN) {
            search = menu.findItem(R.id.menu_search_loan).actionView as SearchView
        } else if (account == AccountType.LOAN) {
            search = menu.findItem(R.id.menu_filter_loan).actionView as SearchView
        }

        search!!.setSearchableInfo(manager.getSearchableInfo(activity?.componentName))
        search.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {

                if (account == AccountType.LOAN) {
                    (childFragmentManager.findFragmentByTag(
                            getFragmentTag(1)) as AccountsFragment).searchLoanAccount(newText)
                } else if (account == AccountType.SAVINGS) {
                    (childFragmentManager.findFragmentByTag(
                            getFragmentTag(0)) as AccountsFragment).searchSavingsAccount(newText)
                }

                return false
            }
        })
    }

    override fun showProgress() {

    }

    override fun hideProgress() {

    }


    override fun onResume() {
        super.onResume()
        (activity as BaseActivity).hideToolbarElevation()
    }

    override fun onPause() {
        super.onPause()
        (activity as BaseActivity).setToolbarElevation()
    }

}