package org.mifos.mobile.ui.fragments

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
import org.mifos.mobile.models.Transaction
import org.mifos.mobile.presenters.RecentTransactionsPresenter
import org.mifos.mobile.ui.activities.base.BaseActivity
import org.mifos.mobile.ui.adapters.RecentTransactionListAdapter
import org.mifos.mobile.ui.fragments.base.BaseFragment
import org.mifos.mobile.ui.views.RecentTransactionsView
import org.mifos.mobile.utils.Constants
import org.mifos.mobile.utils.DividerItemDecoration
import org.mifos.mobile.utils.EndlessRecyclerViewScrollListener
import org.mifos.mobile.utils.Network.isConnected
import org.mifos.mobile.utils.Toaster

import javax.inject.Inject

/**
 * @author Vishwwajeet
 * @since 09/08/16
 */
class RecentTransactionsFragment : BaseFragment(), RecentTransactionsView, OnRefreshListener {

    @JvmField
    @BindView(R.id.rv_recent_transactions)
    var rvRecentTransactions: RecyclerView? = null

    @JvmField
    @BindView(R.id.swipe_transaction_container)
    var swipeTransactionContainer: SwipeRefreshLayout? = null

    @JvmField
    @BindView(R.id.layout_error)
    var layoutError: View? = null

    @JvmField
    @Inject
    var recentTransactionsPresenter: RecentTransactionsPresenter? = null

    @JvmField
    @Inject
    var recentTransactionsListAdapter: RecentTransactionListAdapter? = null
    private var sweetUIErrorHandler: SweetUIErrorHandler? = null
    private var rootView: View? = null
    private var recentTransactionList: MutableList<Transaction?>? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (activity as BaseActivity?)?.activityComponent?.inject(this)
        recentTransactionList = ArrayList()
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.fragment_recent_transactions, container, false)
        ButterKnife.bind(this, rootView!!)
        recentTransactionsPresenter?.attachView(this)
        sweetUIErrorHandler = SweetUIErrorHandler(activity, rootView)
        showUserInterface()
        setToolbarTitle(getString(R.string.recent_transactions))
        if (savedInstanceState == null) {
            recentTransactionsPresenter?.loadRecentTransactions(false, 0)
        }
        return rootView
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelableArrayList(Constants.RECENT_TRANSACTIONS, ArrayList<Parcelable?>(
                recentTransactionList))
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (savedInstanceState != null) {
            val transactions: List<Transaction?> = savedInstanceState.getParcelableArrayList(Constants.RECENT_TRANSACTIONS)
            showRecentTransactions(transactions)
        }
    }

    /**
     * Setting up `rvRecentTransactions`
     */
    override fun showUserInterface() {
        val layoutManager = LinearLayoutManager(activity)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        rvRecentTransactions?.layoutManager = layoutManager
        rvRecentTransactions?.setHasFixedSize(true)
        rvRecentTransactions?.addItemDecoration(DividerItemDecoration(activity!!,
                layoutManager.orientation))
        recentTransactionsListAdapter?.setTransactions(recentTransactionList)
        rvRecentTransactions?.adapter = recentTransactionsListAdapter
        rvRecentTransactions?.addOnScrollListener(
                object : EndlessRecyclerViewScrollListener(layoutManager) {
                    override fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView?) {
                        recentTransactionsPresenter?.loadRecentTransactions(true, totalItemsCount)
                    }

                    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                        super.onScrollStateChanged(recyclerView, newState)
                        if (!recyclerView.canScrollVertically(1) &&
                                newState == RecyclerView.SCROLL_STATE_IDLE) {
                            Toaster.show(rootView, R.string.no_more_transactions_available)
                        }
                    }
                })
        swipeTransactionContainer?.setColorSchemeColors(*activity!!
                .resources.getIntArray(R.array.swipeRefreshColors))
        swipeTransactionContainer?.setOnRefreshListener(this)
    }

    /**
     * Refreshes the List of [Transaction]
     */
    override fun onRefresh() {
        recentTransactionsPresenter?.loadRecentTransactions(false, 0)
    }

    /**
     * Shows a Toast
     */
    override fun showMessage(message: String?) {
        (activity as BaseActivity?)?.showToast(message!!)
    }

    /**
     * Updates `recentTransactionsListAdapter` with `recentTransactionList` fetched from
     * server
     *
     * @param recentTransactionList List of [Transaction]
     */
    override fun showRecentTransactions(recentTransactionList: List<Transaction?>?) {
        this.recentTransactionList = recentTransactionList as MutableList<Transaction?>?
        recentTransactionsListAdapter?.setTransactions(recentTransactionList)
        resetUI()
    }

    /**
     * Appends more Transactions in `recentTransactionList`
     *
     * @param transactions List of [Transaction]
     */
    override fun showLoadMoreRecentTransactions(transactions: List<Transaction?>?) {
        this.recentTransactionList?.addAll(recentTransactionList!!)
        recentTransactionsListAdapter?.notifyDataSetChanged()
        resetUI()
    }

    override fun resetUI() {
        sweetUIErrorHandler?.hideSweetErrorLayoutUI(rvRecentTransactions, layoutError)
    }

    /**
     * Hides `rvRecentTransactions` and shows a textview prompting no transactions
     */
    override fun showEmptyTransaction() {
        sweetUIErrorHandler?.showSweetEmptyUI(getString(R.string.recent_transactions),
                R.drawable.ic_error_black_24dp, rvRecentTransactions, layoutError)
    }

    /**
     * It is called whenever any error occurs while executing a request
     *
     * @param message Error message that tells the user about the problem.
     */
    override fun showErrorFetchingRecentTransactions(message: String?) {
        if (!isConnected(activity!!)) {
            sweetUIErrorHandler?.showSweetNoInternetUI(rvRecentTransactions, layoutError)
        } else {
            sweetUIErrorHandler?.showSweetErrorUI(message, rvRecentTransactions, layoutError)
        }
    }

    @OnClick(R.id.btn_try_again)
    fun retryClicked() {
        if (isConnected(context!!)) {
            sweetUIErrorHandler?.hideSweetErrorLayoutUI(rvRecentTransactions, layoutError)
            recentTransactionsPresenter?.loadRecentTransactions(false, 0)
        } else {
            Toast.makeText(context, getString(R.string.internet_not_connected),
                    Toast.LENGTH_SHORT).show()
        }
    }

    override fun showProgress() {
        showSwipeRefreshLayout(true)
    }

    override fun hideProgress() {
        showSwipeRefreshLayout(false)
    }

    override fun showSwipeRefreshLayout(show: Boolean) {
        swipeTransactionContainer?.post { swipeTransactionContainer?.isRefreshing = show }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        recentTransactionsPresenter?.detachView()
    }

    companion object {
        fun newInstance(): RecentTransactionsFragment {
            val fragment = RecentTransactionsFragment()
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }
    }
}