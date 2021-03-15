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
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.mifos.mobile.R
import org.mifos.mobile.models.beneficiary.Beneficiary
import org.mifos.mobile.presenters.BeneficiaryListPresenter
import org.mifos.mobile.ui.activities.AddBeneficiaryActivity
import org.mifos.mobile.ui.activities.base.BaseActivity
import org.mifos.mobile.ui.adapters.BeneficiaryListAdapter
import org.mifos.mobile.ui.fragments.base.BaseFragment
import org.mifos.mobile.ui.views.BeneficiariesView
import org.mifos.mobile.utils.Constants
import org.mifos.mobile.utils.DividerItemDecoration
import org.mifos.mobile.utils.Network
import org.mifos.mobile.utils.RecyclerItemClickListener
import java.util.*
import javax.inject.Inject

/**
 * Created by dilpreet on 14/6/17.
 */
class BeneficiaryListFragment : BaseFragment(), RecyclerItemClickListener.OnItemClickListener, OnRefreshListener, BeneficiariesView {

    @kotlin.jvm.JvmField
    @BindView(R.id.rv_beneficiaries)
    var rvBeneficiaries: RecyclerView? = null

    @kotlin.jvm.JvmField
    @BindView(R.id.swipe_container)
    var swipeRefreshLayout: SwipeRefreshLayout? = null

    @kotlin.jvm.JvmField
    @BindView(R.id.fab_add_beneficiary)
    var fabAddBeneficiary: FloatingActionButton? = null

    @kotlin.jvm.JvmField
    @BindView(R.id.layout_error)
    var layoutError: View? = null

    @kotlin.jvm.JvmField
    @Inject
    var beneficiaryListPresenter: BeneficiaryListPresenter? = null

    @kotlin.jvm.JvmField
    @Inject
    var beneficiaryListAdapter: BeneficiaryListAdapter? = null
    private var rootView: View? = null
    private var beneficiaryList: List<Beneficiary?>? = null
    private var sweetUIErrorHandler: SweetUIErrorHandler? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.fragment_beneficiary_list, container, false)
        (activity as BaseActivity?)?.activityComponent?.inject(this)
        ButterKnife.bind(this, rootView!!)
        setToolbarTitle(getString(R.string.beneficiaries))
        sweetUIErrorHandler = SweetUIErrorHandler(activity, rootView)
        showUserInterface()
        beneficiaryListPresenter?.attachView(this)
        if (savedInstanceState == null) {
            beneficiaryListPresenter?.loadBeneficiaries()
        }
        return rootView
    }

    override fun onResume() {
        super.onResume()
        beneficiaryListPresenter?.loadBeneficiaries()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (beneficiaryList != null) {
            outState.putParcelableArrayList(Constants.BENEFICIARY, ArrayList<Parcelable?>(
                    beneficiaryList))
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (savedInstanceState != null) {
            val beneficiaries: List<Beneficiary?> = savedInstanceState.getParcelableArrayList(Constants.BENEFICIARY)
            showBeneficiaryList(beneficiaries)
        }
    }

    /**
     * Setup Initial User Interface
     */
    override fun showUserInterface() {
        val layoutManager = LinearLayoutManager(activity)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        rvBeneficiaries?.layoutManager = layoutManager
        rvBeneficiaries?.setHasFixedSize(true)
        rvBeneficiaries?.addItemDecoration(DividerItemDecoration((activity?.applicationContext)!!, layoutManager.orientation))
        rvBeneficiaries?.addOnItemTouchListener(RecyclerItemClickListener(activity, this))
        rvBeneficiaries?.adapter = beneficiaryListAdapter
        swipeRefreshLayout?.setColorSchemeColors(*activity!!
                .resources.getIntArray(R.array.swipeRefreshColors))
        swipeRefreshLayout?.setOnRefreshListener(this)
        fabAddBeneficiary?.setOnClickListener { startActivity(Intent(activity, AddBeneficiaryActivity::class.java)) }
    }

    @OnClick(R.id.btn_try_again)
    fun retryClicked() {
        if (Network.isConnected((context?.applicationContext)!!)) {
            sweetUIErrorHandler?.hideSweetErrorLayoutUI(rvBeneficiaries, layoutError)
            beneficiaryListPresenter?.loadBeneficiaries()
        } else {
            Toast.makeText(context, getString(R.string.internet_not_connected),
                    Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Refreshes `beneficiaryList` by calling `loadBeneficiaries()`
     */
    override fun onRefresh() {
        beneficiaryListPresenter?.loadBeneficiaries()
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

    /**
     * It is called whenever any error occurs while executing a request
     *
     * @param msg Error message that tells the user about the problem.
     */
    override fun showError(msg: String?) {
        if (!Network.isConnected((activity?.applicationContext)!!)) {
            sweetUIErrorHandler?.showSweetNoInternetUI(rvBeneficiaries, layoutError)
        } else {
            sweetUIErrorHandler?.showSweetErrorUI(msg,
                    rvBeneficiaries, layoutError)
            Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Set the `beneficiaryList` fetched from server to `beneficiaryListAdapter`
     */
    override fun showBeneficiaryList(beneficiaryList: List<Beneficiary?>?) {
        this.beneficiaryList = beneficiaryList
        if (beneficiaryList?.isNotEmpty() == true) {
            beneficiaryListAdapter?.setBeneficiaryList(beneficiaryList)
            sweetUIErrorHandler?.hideSweetErrorLayoutUI(rvBeneficiaries, layoutError)
        } else {
            showEmptyBeneficiary()
        }
    }

    override fun onItemClick(childView: View?, position: Int) {
        (activity as BaseActivity?)?.replaceFragment(BeneficiaryDetailFragment.newInstance(beneficiaryList!![position]), true, R.id.container)
    }

    override fun onItemLongPress(childView: View?, position: Int) {}
    private fun showSwipeRefreshLayout(show: Boolean?) {
        swipeRefreshLayout?.post { swipeRefreshLayout?.isRefreshing = (show == true) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        beneficiaryListPresenter?.detachView()
    }

    /**
     * Shows an error layout when this function is called.
     */
    private fun showEmptyBeneficiary() {
        sweetUIErrorHandler?.showSweetEmptyUI(getString(R.string.beneficiary),
                getString(R.string.beneficiary),
                R.drawable.ic_beneficiaries_48px, rvBeneficiaries, layoutError)
        rvBeneficiaries?.visibility = View.GONE
    }

    companion object {
        fun newInstance(): BeneficiaryListFragment {
            return BeneficiaryListFragment()
        }
    }
}