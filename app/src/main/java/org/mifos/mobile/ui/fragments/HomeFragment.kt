package org.mifos.mobile.ui.fragments

import android.app.Activity.RESULT_OK
import android.content.*
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Parcelable
import android.view.*
import android.widget.ImageView
import android.widget.TextView

import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener

import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick

import org.mifos.mobile.R
import org.mifos.mobile.api.local.PreferencesHelper
import org.mifos.mobile.presenters.HomePresenter
import org.mifos.mobile.ui.activities.AccountOverviewActivity
import org.mifos.mobile.ui.activities.HomeActivity
import org.mifos.mobile.ui.activities.LoanApplicationActivity
import org.mifos.mobile.ui.activities.UserProfileActivity
import org.mifos.mobile.ui.activities.base.BaseActivity
import org.mifos.mobile.ui.enums.AccountType
import org.mifos.mobile.ui.enums.ChargeType
import org.mifos.mobile.ui.enums.LoanState
import org.mifos.mobile.ui.fragments.base.BaseFragment
import org.mifos.mobile.ui.views.HomeView
import org.mifos.mobile.utils.*
import org.mifos.mobile.utils.Constants.APPLY_LOAN

import javax.inject.Inject

/**
 * Created by michaelsosnick on 1/1/17.
 */
class HomeFragment : BaseFragment(), HomeView, OnRefreshListener {

    @JvmField
    @BindView(R.id.iv_user_image)
    var ivUserImage: ImageView? = null

    @JvmField
    @BindView(R.id.iv_circular_user_image)
    var ivCircularUserImage: CircularImageView? = null


    @JvmField
    @BindView(R.id.tv_user_name)
    var tvUserName: TextView? = null


    @JvmField
    @BindView(R.id.swipe_home_container)
    var slHomeContainer: SwipeRefreshLayout? = null

    @JvmField
    @Inject
    var presenter: HomePresenter? = null

    @JvmField
    @Inject
    var preferencesHelper: PreferencesHelper? = null

    lateinit var rootView: View
    private var userProfileBitmap: Bitmap? = null
    private var clientId: Long? = 0
    private val notifCount = 0
    private var isReceiverRegistered = false
    private var tvNotificationCount: TextView? = null
    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.fragment_home_ui, container, false)
        (activity as HomeActivity?)?.activityComponent?.inject(this)
        ButterKnife.bind(this, rootView)
        clientId = preferencesHelper?.clientId
        setHasOptionsMenu(true)
        presenter?.attachView(this)
        slHomeContainer?.setColorSchemeResources(R.color.blue_light, R.color.green_light, R.color.orange_light, R.color.red_light)
        slHomeContainer?.setOnRefreshListener(this)
        if (savedInstanceState == null) {
            loadClientData()
        }
        setToolbarTitle(getString(R.string.home))
        showUserImageTextDrawable()
        return rootView
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(Constants.USER_PROFILE, userProfileBitmap)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (savedInstanceState != null) {
            showUserImage(savedInstanceState.getParcelable<Parcelable>(Constants.USER_PROFILE) as Bitmap)
            showUserDetails(preferencesHelper?.clientName)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_main, menu)
        val menuItem = menu.findItem(R.id.menu_notifications)
        val count = menuItem.actionView
        tvNotificationCount = count.findViewById(R.id.tv_notification_indicator)
        count.setOnClickListener {
            (activity as BaseActivity?)?.replaceFragment(NotificationFragment.newInstance(),
                    true, R.id.container)
        }
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onResume() {
        super.onResume()
        registerReceiver()
        activity?.invalidateOptionsMenu()
    }

    override fun onPause() {
        LocalBroadcastManager.getInstance(activity!!).unregisterReceiver(notificationReceiver)
        isReceiverRegistered = false
        super.onPause()
    }

    private fun registerReceiver() {
        if (!isReceiverRegistered) {
            LocalBroadcastManager.getInstance(activity!!).registerReceiver(notificationReceiver,
                    IntentFilter(Constants.NOTIFY_HOME_FRAGMENT))
            isReceiverRegistered = true
        }
    }

    override fun onRefresh() {
        loadClientData()
    }

    private fun loadClientData() {
        if (preferencesHelper?.clientName?.isBlank() == true) {
            tvUserName?.text = getString(R.string.hello_client, preferencesHelper?.clientName)
            hideProgress()
        } else {
            presenter?.userDetails
        }
        presenter?.userImage
        presenter?.unreadNotificationsCount
    }

    /**
     * Opens [ClientAccountsFragment] according to the `accountType` provided
     *
     * @param accountType Enum of [AccountType]
     */
    private fun openAccount(accountType: AccountType?) {
        (activity as BaseActivity?)?.replaceFragment(
                ClientAccountsFragment.newInstance(accountType), true, R.id.container)
    }

    /**
     * Fetches Client details and display clientName
     *
     * @param userName of the client
     */
    override fun showUserDetails(userName: String?) {
        tvUserName?.text = getString(R.string.hello_client, userName)
    }

    override fun showUserImageTextDrawable() {
        activity?.runOnUiThread {
            val userName: String? = if (preferencesHelper?.clientName?.isNotEmpty() == true) {
                preferencesHelper?.clientName
            } else {
                getString(R.string.app_name)
            }
            val drawable = TextDrawable.builder()
                    .beginConfig()
                    .toUpperCase()
                    .endConfig()
                    .buildRound(userName?.substring(0, 1),
                            ContextCompat.getColor(activity!!, R.color.primary_dark))
            ivUserImage?.setImageDrawable(drawable)
            ivUserImage?.visibility = View.VISIBLE
            ivCircularUserImage?.visibility = View.GONE
        }
    }

    /**
     * Provides with Client image fetched from server
     *
     * @param bitmap Client Image
     */
    override fun showUserImage(bitmap: Bitmap?) {
        if (bitmap != null) {
            activity?.runOnUiThread {
                userProfileBitmap = bitmap
                ivCircularUserImage?.setImageBitmap(bitmap)
                ivCircularUserImage?.visibility = View.VISIBLE
                ivUserImage?.visibility = View.GONE
            }
        }
    }

    override fun showNotificationCount(count: Int) {
        if (count > 0) {
            tvNotificationCount?.text = count.toString()
        } else {
            tvNotificationCount?.visibility = View.GONE
        }
    }

    @OnClick(R.id.iv_user_image)
    fun userImageClicked() {
        startActivity(Intent(activity, UserProfileActivity::class.java))
    }

    @OnClick(R.id.iv_circular_user_image)
    fun userCircularImageClick() {
        startActivity(Intent(activity, UserProfileActivity::class.java))
    }

    /**
     * Calls for opening Account Overview
     */
    @OnClick(R.id.ll_account_overview)
    fun onAccountOverview() {
        startActivity(Intent(activity, AccountOverviewActivity::class.java))
    }

    /**
     * Calls `openAccount()` for opening [ClientAccountsFragment]
     */
    @OnClick(R.id.ll_accounts)
    fun accountsClicked() {
        openAccount(AccountType.SAVINGS)
        (activity as HomeActivity?)?.setNavigationViewSelectedItem(R.id.item_accounts)
    }

    /**
     * Shows a dialog with options: Normal Transfer and Third Party Transfer
     */
    @OnClick(R.id.ll_transfer)
    fun transferClicked() {
        val transferTypes = arrayOf(getString(R.string.transfer), getString(R.string.third_party_transfer))
        MaterialDialog.Builder().init(activity)
                .setTitle(R.string.choose_transfer_type)
                .setItems(transferTypes,
                        DialogInterface.OnClickListener { _, which ->
                            if (which == 0) {
                                (activity as HomeActivity?)?.replaceFragment(
                                        SavingsMakeTransferFragment.newInstance(1, ""), true,
                                        R.id.container)
                            } else {
                                (activity as HomeActivity?)?.replaceFragment(
                                        ThirdPartyTransferFragment.newInstance(), true, R.id.container)
                            }
                        })
                .createMaterialDialog()
                .show()
    }

    /**
     * Opens [ClientChargeFragment] to display all Charges associated with client's account
     */
    @OnClick(R.id.ll_charges)
    fun chargesClicked() {
        (activity as HomeActivity?)?.replaceFragment(ClientChargeFragment.newInstance(clientId,
                ChargeType.CLIENT), true, R.id.container)
    }

    /**
     * Opens [LoanApplicationFragment] to apply for a loan
     */
    @OnClick(R.id.ll_apply_for_loan)
    fun applyForLoan() {
        startActivityForResult(Intent(activity, LoanApplicationActivity::class.java), APPLY_LOAN)
    }

    /**
     * Opens [BeneficiaryListFragment] which contains list of Beneficiaries associated with
     * Client's account
     */
    @OnClick(R.id.ll_beneficiaries)
    fun beneficiaries() {
        (activity as HomeActivity?)?.replaceFragment(BeneficiaryListFragment.newInstance(), true, R.id.container)
    }

    @OnClick(R.id.ll_surveys)
    fun surveys() {
    }

    @OnClick(R.id.ll_recent_transactions)
    fun showRecentTransactions() {
        (activity as HomeActivity?)?.replaceFragment(RecentTransactionsFragment.newInstance(),
                true, R.id.container)
    }

    /**
     * It is called whenever any error occurs while executing a request
     *
     * @param errorMessage Error message that tells the user about the problem.
     */
    override fun showError(errorMessage: String?) {
        Toaster.show(rootView, errorMessage)
    }

    override fun showUserImageNotFound() {
        showUserImageTextDrawable()
    }

    /**
     * Shows [SwipeRefreshLayout]
     */
    override fun showProgress() {
        slHomeContainer?.isRefreshing = true
    }

    /**
     * Hides [SwipeRefreshLayout]
     */
    override fun hideProgress() {
        slHomeContainer?.isRefreshing = false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        presenter?.detachView()
        if (slHomeContainer?.isRefreshing == true)
            slHomeContainer?.isRefreshing = false
        slHomeContainer?.removeAllViews()
    }

    private val notificationReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            activity?.invalidateOptionsMenu()
        }
    }

    companion object {
        val LOG_TAG: String? = HomeFragment::class.java.simpleName
        fun newInstance(): HomeFragment {
            val fragment = HomeFragment()
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == APPLY_LOAN && resultCode == RESULT_OK && data != null) {
            val state = data.getStringExtra(getString(R.string.loan_type))
            if (state == LoanState.CREATE.name)
                showLoanAccountCreatedSuccessfully()
            else if (state == LoanState.UPDATE.name)
                showLoanAccountUpdatedSuccessfully()
        }
    }

    private fun showLoanAccountCreatedSuccessfully() {
        Toaster.show(rootView, R.string.loan_application_submitted_successfully)
    }

    private fun showLoanAccountUpdatedSuccessfully() {
        Toaster.show(rootView, R.string.loan_application_updated_successfully)
    }
}