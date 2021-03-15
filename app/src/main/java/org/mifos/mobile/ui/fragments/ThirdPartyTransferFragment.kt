package org.mifos.mobile.ui.fragments

import android.os.Bundle
import android.os.Parcelable
import android.view.*
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import androidx.appcompat.widget.AppCompatButton

import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick

import com.github.therajanmaurya.sweeterror.SweetUIErrorHandler

import org.mifos.mobile.R
import org.mifos.mobile.models.beneficiary.Beneficiary
import org.mifos.mobile.models.beneficiary.BeneficiaryDetail
import org.mifos.mobile.models.payload.AccountDetail
import org.mifos.mobile.models.payload.TransferPayload
import org.mifos.mobile.models.templates.account.AccountOption
import org.mifos.mobile.models.templates.account.AccountOptionsTemplate
import org.mifos.mobile.presenters.ThirdPartyTransferPresenter
import org.mifos.mobile.ui.activities.base.BaseActivity
import org.mifos.mobile.ui.adapters.AccountsSpinnerAdapter
import org.mifos.mobile.ui.adapters.BeneficiarySpinnerAdapter
import org.mifos.mobile.ui.enums.TransferType
import org.mifos.mobile.ui.fragments.base.BaseFragment
import org.mifos.mobile.ui.views.ThirdPartyTransferView
import org.mifos.mobile.utils.*

import java.util.*
import javax.inject.Inject

/**
 * Created by dilpreet on 21/6/17.
 */
class ThirdPartyTransferFragment : BaseFragment(), ThirdPartyTransferView, OnItemSelectedListener {

    @kotlin.jvm.JvmField
    @BindView(R.id.sp_beneficiary)
    var spBeneficiary: Spinner? = null

    @kotlin.jvm.JvmField
    @BindView(R.id.sp_pay_from)
    var spPayFrom: Spinner? = null

    @kotlin.jvm.JvmField
    @BindView(R.id.et_amount)
    var etAmount: EditText? = null

    @kotlin.jvm.JvmField
    @BindView(R.id.et_remark)
    var etRemark: EditText? = null

    @kotlin.jvm.JvmField
    @BindView(R.id.ll_make_transfer)
    var layoutMakeTransfer: LinearLayout? = null

    @kotlin.jvm.JvmField
    @BindView(R.id.process_one)
    var pvOne: ProcessView? = null

    @kotlin.jvm.JvmField
    @BindView(R.id.process_two)
    var pvTwo: ProcessView? = null

    @kotlin.jvm.JvmField
    @BindView(R.id.process_three)
    var pvThree: ProcessView? = null

    @kotlin.jvm.JvmField
    @BindView(R.id.process_four)
    var pvFour: ProcessView? = null

    @kotlin.jvm.JvmField
    @BindView(R.id.btn_pay_to)
    var btnPayTo: AppCompatButton? = null

    @kotlin.jvm.JvmField
    @BindView(R.id.btn_pay_from)
    var btnPayFrom: AppCompatButton? = null

    @kotlin.jvm.JvmField
    @BindView(R.id.btn_amount)
    var btnAmount: AppCompatButton? = null

    @kotlin.jvm.JvmField
    @BindView(R.id.btn_add_beneficiary)
    var btnAddBeneficiary: AppCompatButton? = null

    @kotlin.jvm.JvmField
    @BindView(R.id.ll_review)
    var llReview: LinearLayout? = null

    @kotlin.jvm.JvmField
    @BindView(R.id.tv_select_beneficary)
    var tvSelectBeneficiary: TextView? = null

    @kotlin.jvm.JvmField
    @BindView(R.id.tv_select_amount)
    var tvEnterAmount: TextView? = null

    @kotlin.jvm.JvmField
    @BindView(R.id.tv_enter_remark)
    var tvEnterRemark: TextView? = null

    @kotlin.jvm.JvmField
    @BindView(R.id.tv_add_beneficiary_msg)
    var tvAddBeneficiaryMsg: TextView? = null

    @kotlin.jvm.JvmField
    @BindView(R.id.layout_error)
    var layoutError: View? = null

    @kotlin.jvm.JvmField
    @Inject
    var presenter: ThirdPartyTransferPresenter? = null
    private val listBeneficiary: MutableList<BeneficiaryDetail?> = ArrayList()
    private val listPayFrom: MutableList<AccountDetail?> = ArrayList()
    private var beneficiaries: List<Beneficiary?>? = null
    private var beneficiaryAdapter: BeneficiarySpinnerAdapter? = null
    private var payFromAdapter: AccountsSpinnerAdapter? = null
    private var fromAccountOption: AccountOption? = null
    private var beneficiaryAccountOption: AccountOption? = null
    private var accountOptionsTemplate: AccountOptionsTemplate? = null
    private var transferDate: String? = null
    private var rootView: View? = null
    private var sweetUIErrorHandler: SweetUIErrorHandler? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        (activity as BaseActivity?)?.activityComponent?.inject(this)
        rootView = inflater.inflate(R.layout.fragment_third_party_transfer, container, false)
        setToolbarTitle(getString(R.string.third_party_transfer))
        if (rootView != null ) ButterKnife.bind(this, rootView!!)
        sweetUIErrorHandler = SweetUIErrorHandler(activity, rootView)
        showUserInterface()
        presenter?.attachView(this)
        if (savedInstanceState == null) {
            presenter?.loadTransferTemplate()
        }
        return rootView
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(Constants.TEMPLATE, accountOptionsTemplate)
        outState.putParcelableArrayList(Constants.BENEFICIARY, ArrayList<Parcelable?>(
                beneficiaries))
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (savedInstanceState != null) {
            showThirdPartyTransferTemplate(savedInstanceState.getParcelable<Parcelable>(Constants.TEMPLATE) as AccountOptionsTemplate)
            val tempBeneficiaries: List<Beneficiary?> = savedInstanceState.getParcelableArrayList(
                    Constants.BENEFICIARY)
            showBeneficiaryList(tempBeneficiaries)
        }
    }

    /**
     * Setting up basic components
     */
    override fun showUserInterface() {
        payFromAdapter = activity?.applicationContext?.let {
            AccountsSpinnerAdapter(it, R.layout.account_spinner_layout,
                    listPayFrom)
        }
        payFromAdapter?.setDropDownViewResource(android.R.layout.select_dialog_singlechoice)
        spPayFrom?.adapter = payFromAdapter
        spPayFrom?.onItemSelectedListener = this
        beneficiaryAdapter = activity?.applicationContext?.let {
            BeneficiarySpinnerAdapter(it,
                    R.layout.beneficiary_spinner_layout, listBeneficiary)
        }
        beneficiaryAdapter?.setDropDownViewResource(android.R.layout.select_dialog_singlechoice)
        spBeneficiary?.adapter = beneficiaryAdapter
        spBeneficiary?.onItemSelectedListener = this
        transferDate = DateHelper.getSpecificFormat(DateHelper.FORMAT_dd_MMMM_yyyy,
                MFDatePicker.datePickedAsString)
        pvOne?.setCurrentActive()
    }

    /**
     * Checks validation of `etRemark` and then opens [TransferProcessFragment] for
     * initiating the transfer
     */
    @OnClick(R.id.btn_review_transfer)
    fun reviewTransfer() {
        if (etAmount?.text.toString() == "") {
            Toaster.show(rootView, getString(R.string.enter_amount))
            return
        }
        if (etAmount?.text.toString() == ".") {
            Toaster.show(rootView, getString(R.string.invalid_amount))
            return
        }
        if (etRemark?.text.toString().trim { it <= ' ' } == "") {
            Toaster.show(rootView, getString(R.string.remark_is_mandatory))
            return
        }
        if (spBeneficiary?.selectedItem.toString() == spPayFrom?.selectedItem.toString()) {
            Toaster.show(rootView, getString(R.string.error_same_account_transfer))
            return
        }
        val transferPayload = TransferPayload()
        transferPayload.fromAccountId = fromAccountOption?.accountId
        transferPayload.fromClientId = fromAccountOption?.clientId
        transferPayload.fromAccountType = fromAccountOption?.accountType?.id
        transferPayload.fromOfficeId = fromAccountOption?.officeId
        transferPayload.toOfficeId = beneficiaryAccountOption?.officeId
        transferPayload.toAccountId = beneficiaryAccountOption?.accountId
        transferPayload.toClientId = beneficiaryAccountOption?.clientId
        transferPayload.toAccountType = beneficiaryAccountOption?.accountType?.id
        transferPayload.transferDate = transferDate
        transferPayload.transferAmount = etAmount?.text.toString().toDouble()
        transferPayload.transferDescription = etRemark?.text.toString()
        transferPayload.fromAccountNumber = fromAccountOption?.accountNo
        transferPayload.toAccountNumber = beneficiaryAccountOption?.accountNo
        (activity as BaseActivity?)?.replaceFragment(TransferProcessFragment.newInstance(transferPayload, TransferType.TPT), true, R.id.container)
    }

    /**
     * Shows a {@link Snackbar} with `message`
     *
     * @param msg String to be shown
     */
    override fun showToaster(msg: String?) {
        Toaster.show(rootView, msg)
    }

    /**
     * Provides with `accountOptionsTemplate` fetched from server which is used to update
     * `listPayFrom`
     *
     * @param accountOptionsTemplate Template for account transfer
     */
    override fun showThirdPartyTransferTemplate(accountOptionsTemplate: AccountOptionsTemplate?) {
        this.accountOptionsTemplate = accountOptionsTemplate
        listPayFrom.clear()
        presenter?.getAccountNumbersFromAccountOptions(accountOptionsTemplate?.fromAccountOptions)?.let { listPayFrom.addAll(it) }
        payFromAdapter?.notifyDataSetChanged()
    }

    /**
     * Provides with `beneficiaries` fetched from server which is used to update
     * `listBeneficiary`
     *
     * @param beneficiaries List of [Beneficiary] linked with user's account
     */
    override fun showBeneficiaryList(beneficiaries: List<Beneficiary?>?) {
        this.beneficiaries = beneficiaries
        listBeneficiary.clear()
        presenter?.getAccountNumbersFromBeneficiaries(beneficiaries)?.let { listBeneficiary.addAll(it) }
        beneficiaryAdapter?.notifyDataSetChanged()
    }

    /**
     * Disables `spPayFrom` [Spinner] and sets `pvOne` to completed and make
     * `pvThree` pvTwo
     */
    @OnClick(R.id.btn_pay_from)
    fun payFromSelected() {
        pvOne?.setCurrentCompleted()
        pvTwo?.setCurrentActive()
        btnPayFrom?.visibility = View.GONE
        tvSelectBeneficiary?.visibility = View.GONE
        if (listBeneficiary.isNotEmpty()) {
            btnPayTo?.visibility = View.VISIBLE
            spBeneficiary?.visibility = View.VISIBLE
            spPayFrom?.isEnabled = false
        } else {
            tvAddBeneficiaryMsg?.visibility = View.VISIBLE
            btnAddBeneficiary?.visibility = View.VISIBLE
        }
    }

    /**
     * Checks validation of `spBeneficiary` [Spinner].<br></br>
     * Disables `spBeneficiary` [Spinner] and sets `pvTwo` to completed and make
     * `pvThree` active
     */
    @OnClick(R.id.btn_pay_to)
    fun payToSelected() {
        if (spBeneficiary?.selectedItem.toString() == spPayFrom?.selectedItem.toString()) {
            showToaster(getString(R.string.error_same_account_transfer))
            return
        }
        pvTwo?.setCurrentCompleted()
        pvThree?.setCurrentActive()
        btnPayTo?.visibility = View.GONE
        tvEnterAmount?.visibility = View.GONE
        etAmount?.visibility = View.VISIBLE
        btnAmount?.visibility = View.VISIBLE
        spBeneficiary?.isEnabled = false
    }

    /**
     * Checks validation of `etAmount` [EditText].<br></br>
     * Disables `etAmount` and sets `pvThree` to completed and make
     * `pvFour` active
     */
    @OnClick(R.id.btn_amount)
    fun amountSet() {
        if (etAmount?.text.toString() == "") {
            showToaster(getString(R.string.enter_amount))
            return
        }
        if (etAmount?.text.toString() == ".") {
            showToaster(getString(R.string.invalid_amount))
            return
        }
        if (etAmount?.text.toString().toDouble() == 0.0) {
            showToaster(getString(R.string.amount_greater_than_zero))
            return
        }
        pvThree?.setCurrentCompleted()
        pvFour?.setCurrentActive()
        btnAmount?.visibility = View.GONE
        tvEnterRemark?.visibility = View.GONE
        etRemark?.visibility = View.VISIBLE
        llReview?.visibility = View.VISIBLE
        etAmount?.isEnabled = false
    }

    @OnClick(R.id.btn_cancel_transfer)
    fun cancelTransfer() {
        activity?.supportFragmentManager?.popBackStack()
    }

    @OnClick(R.id.btn_add_beneficiary)
    fun addBeneficiary() {
        (activity as BaseActivity?)?.replaceFragment(BeneficiaryAddOptionsFragment.newInstance(),
                true, R.id.container)
    }

    @OnClick(R.id.btn_try_again)
    fun onRetry() {
        if (Network.isConnected(context)) {
            sweetUIErrorHandler?.hideSweetErrorLayoutUI(layoutMakeTransfer, layoutError)
            presenter?.loadTransferTemplate()
        } else {
            Toaster.show(rootView, getString(R.string.internet_not_connected))
        }
    }

    /**
     * It is called whenever any error occurs while executing a request
     *
     * @param msg Error message that tells the user about the problem.
     */
    override fun showError(msg: String?) {
        if (!Network.isConnected(context)) {
            sweetUIErrorHandler?.showSweetNoInternetUI(layoutMakeTransfer, layoutError)
        } else {
            sweetUIErrorHandler?.showSweetErrorUI(msg, layoutMakeTransfer, layoutError)
            Toaster.show(rootView, msg)
        }
    }

    override fun showProgress() {
        layoutMakeTransfer?.visibility = View.GONE
        showProgressBar()
    }

    override fun hideProgress() {
        hideProgressBar()
        layoutMakeTransfer?.visibility = View.VISIBLE
    }

    /**
     * Callback for `spPayFrom` and `spBeneficiary`
     */
    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        when (parent?.id) {
            R.id.sp_beneficiary -> beneficiaryAccountOption =
                    presenter?.searchAccount(accountOptionsTemplate?.fromAccountOptions, beneficiaryAdapter?.getItem(position))
            R.id.sp_pay_from -> fromAccountOption =
                    accountOptionsTemplate?.fromAccountOptions?.get(position)
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {}
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_transfer, menu)
        Utils.setToolbarIconColor(activity, menu, R.color.white)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menu_refresh_transfer) {
            val transaction = fragmentManager?.beginTransaction()
            val currFragment = activity?.supportFragmentManager
                    ?.findFragmentById(R.id.container)
            if (currFragment!=null){
                transaction?.detach(currFragment)
                transaction?.attach(currFragment)
            }
            transaction?.commit()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        hideProgress()
        presenter?.detachView()
    }

    companion object {
        fun newInstance(): ThirdPartyTransferFragment {
            return ThirdPartyTransferFragment()
        }
    }
}