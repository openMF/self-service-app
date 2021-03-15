package org.mifos.mobile.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener

import androidx.fragment.app.DialogFragment

import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick

import com.google.android.material.textfield.TextInputLayout

import org.mifos.mobile.R
import org.mifos.mobile.models.accounts.loan.LoanAccount
import org.mifos.mobile.models.accounts.loan.LoanWithAssociations
import org.mifos.mobile.models.payload.LoansPayload
import org.mifos.mobile.models.templates.loans.LoanTemplate
import org.mifos.mobile.presenters.LoanApplicationPresenter
import org.mifos.mobile.ui.activities.base.BaseActivity
import org.mifos.mobile.ui.enums.LoanState
import org.mifos.mobile.ui.fragments.ReviewLoanApplicationFragment.Companion.newInstance
import org.mifos.mobile.ui.fragments.base.BaseFragment
import org.mifos.mobile.ui.views.LoanApplicationMvpView
import org.mifos.mobile.utils.*
import org.mifos.mobile.utils.MFDatePicker.OnDatePickListener

import java.util.*
import javax.inject.Inject


/**
 * Created by Rajan Maurya on 06/03/17.
 */
class LoanApplicationFragment : BaseFragment(), LoanApplicationMvpView, OnDatePickListener, OnItemSelectedListener {

    @JvmField
    @BindView(R.id.tv_new_loan_application)
    var tvNewLoanApplication: TextView? = null

    @JvmField
    @BindView(R.id.tv_account_number)
    var tvAccountNumber: TextView? = null

    @JvmField
    @BindView(R.id.tv_submission_date)
    var tvSubmissionDate: TextView? = null

    @JvmField
    @BindView(R.id.sp_loan_products)
    var spLoanProducts: Spinner? = null

    @JvmField
    @BindView(R.id.sp_loan_purpose)
    var spLoanPurpose: Spinner? = null

    @JvmField
    @BindView(R.id.til_principal_amount)
    var tilPrincipalAmount: TextInputLayout? = null

    @JvmField
    @BindView(R.id.tv_currency)
    var tvCurrency: TextView? = null

    @JvmField
    @BindView(R.id.tv_expected_disbursement_date)
    var tvExpectedDisbursementDate: TextView? = null

    @JvmField
    @BindView(R.id.ll_add_loan)
    var llAddLoan: LinearLayout? = null

    @JvmField
    @BindView(R.id.ll_error)
    var llError: RelativeLayout? = null

    @JvmField
    @BindView(R.id.tv_status)
    var tvErrorStatus: TextView? = null

    @JvmField
    @BindView(R.id.iv_status)
    var ivReload: ImageView? = null

    @JvmField
    @Inject
    var loanApplicationPresenter: LoanApplicationPresenter? = null
    var rootView: View? = null
    private val listLoanProducts: MutableList<String?> = ArrayList()
    private val listLoanPurpose: MutableList<String?> = ArrayList()
    private var loanProductAdapter: ArrayAdapter<String?>? = null
    private var loanPurposeAdapter: ArrayAdapter<String?>? = null
    private var loanTemplate: LoanTemplate? = null
    private var mfDatePicker: DialogFragment? = null
    private var loanState: LoanState? = null
    private var loanWithAssociations: LoanWithAssociations? = null
    private var productId: Int? = 0
    private var purposeId: Int? = -1
    private var disbursementDate: String? = null
    private var submittedDate: String? = null
    private var isDisbursementDate = false
    private var isLoanUpdatePurposesInitialization = true
    var active: Boolean = false


    /**
     * Used when we want to apply for a Loan
     *
     * @param loanState [LoanState] is set to `LoanState.CREATE`
     * @return Instance of [LoanApplicationFragment]
     */
    fun newInstance(loanState: LoanState): LoanApplicationFragment? {
        val fragment = LoanApplicationFragment()
        val args = Bundle()
        args.putSerializable(Constants.LOAN_STATE, loanState)
        fragment.arguments = args
        return fragment
    }

    /**
     * Used when we want to update a Loan Application
     *
     * @param loanState            [LoanState] is set to `LoanState.UPDATE`
     * @param loanWithAssociations [LoanAccount] to modify
     * @return Instance of [LoanApplicationFragment]
     */
    fun newInstance(
            loanState: LoanState?,
            loanWithAssociations: LoanWithAssociations?
    ): LoanApplicationFragment? {
        val fragment = LoanApplicationFragment()
        val args = Bundle()
        args.putSerializable(Constants.LOAN_STATE, loanState)
        args.putParcelable(Constants.LOAN_ACCOUNT, loanWithAssociations)
        fragment.arguments = args
        return fragment
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (activity as BaseActivity?)?.activityComponent?.inject(this)
        if (arguments != null) {
            loanState = arguments?.getSerializable(Constants.LOAN_STATE) as LoanState
            if (loanState == LoanState.CREATE) {
                setToolbarTitle(getString(R.string.apply_for_loan))
            } else {
                setToolbarTitle(getString(R.string.update_loan))
                loanWithAssociations = arguments?.getParcelable(Constants.LOAN_ACCOUNT)
            }
        }
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.fragment_add_loan_application, container, false)
        ButterKnife.bind(this, rootView!!)
        loanApplicationPresenter?.attachView(this)
        showUserInterface()
        if (savedInstanceState == null) {
            loadLoanTemplate()
        }
        return rootView
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(Constants.TEMPLATE, loanTemplate)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (savedInstanceState != null) {
            val template: LoanTemplate? = savedInstanceState.getParcelable(Constants.TEMPLATE)
            if (loanState == LoanState.CREATE) {
                showLoanTemplate(template)
            } else {
                showUpdateLoanTemplate(template)
            }
        }
    }

    /**
     * Loads [LoanTemplate] according to the `loanState`
     */
    private fun loadLoanTemplate() {
        if (loanState == LoanState.CREATE) {
            loanApplicationPresenter?.loadLoanApplicationTemplate(LoanState.CREATE)
        } else {
            loanApplicationPresenter?.loadLoanApplicationTemplate(LoanState.UPDATE)
        }
    }

    /**
     * Calls function which applies for a new Loan Application or updates a Loan Application
     * according to `loanState`
     */
    @OnClick(R.id.btn_loan_review)
    fun onReviewLoanApplication() {
        if (tilPrincipalAmount?.editText?.text.toString() == "") {
            tilPrincipalAmount?.error = getString(R.string.enter_amount)
            return
        }
        if (tilPrincipalAmount?.editText?.text.toString() == ".") {
            tilPrincipalAmount?.error = getString(R.string.invalid_amount)
            return
        }
        if (tilPrincipalAmount?.editText?.text.toString().matches("^0*".toRegex())) {
            tilPrincipalAmount?.error = getString(R.string.amount_greater_than_zero)
            return
        }
        tilPrincipalAmount?.error = null
        if (loanState == LoanState.CREATE) {
            reviewNewLoanApplication()
        } else {
            submitUpdateLoanApplication()
        }
    }

    /**
     * Submits a New Loan Application to the server
     */
    private fun reviewNewLoanApplication() {
        val loansPayload = LoansPayload()
        loansPayload.clientId = loanTemplate?.clientId
        loansPayload.loanPurpose = spLoanPurpose?.selectedItem.toString()
        loansPayload.productName = spLoanProducts?.selectedItem.toString()
        loansPayload.currency = tvCurrency?.text.toString()
        if (purposeId != null && purposeId!! > 0) loansPayload.loanPurposeId = purposeId
        loansPayload.productId = productId
        loansPayload.principal = tilPrincipalAmount?.editText?.text.toString().toDouble()
        loansPayload.loanTermFrequency = loanTemplate?.termFrequency
        loansPayload.loanTermFrequencyType = loanTemplate?.interestRateFrequencyType?.id
        loansPayload.loanType = "individual"
        loansPayload.numberOfRepayments = loanTemplate?.numberOfRepayments
        loansPayload.repaymentEvery = loanTemplate?.repaymentEvery
        loansPayload.repaymentFrequencyType = loanTemplate?.interestRateFrequencyType?.id
        loansPayload.interestRatePerPeriod = loanTemplate?.interestRatePerPeriod
        loansPayload.expectedDisbursementDate = disbursementDate
        loansPayload.submittedOnDate = submittedDate
        loansPayload.transactionProcessingStrategyId = loanTemplate?.transactionProcessingStrategyId
        loansPayload.amortizationType = loanTemplate?.amortizationType?.id
        loansPayload.interestCalculationPeriodType = loanTemplate?.interestCalculationPeriodType?.id
        loansPayload.interestType = loanTemplate?.interestType?.id
        (activity as BaseActivity?)?.replaceFragment(newInstance(loanState!!, loansPayload,
                tvNewLoanApplication?.text.toString(),
                tvAccountNumber?.text.toString()),
                true, R.id.container)
    }

    /**
     * Requests server to update the Loan Application with new values
     */
    private fun submitUpdateLoanApplication() {
        val loansPayload = LoansPayload()
        loansPayload.principal = tilPrincipalAmount?.editText?.text.toString().toDouble()
        loansPayload.productId = productId
        loansPayload.loanPurpose = spLoanPurpose?.selectedItem.toString()
        loansPayload.productName = spLoanProducts?.selectedItem.toString()
        loansPayload.currency = tvCurrency?.text.toString()
        if (purposeId != null && purposeId!! > 0) loansPayload.loanPurposeId = purposeId
        loansPayload.loanTermFrequency = loanTemplate?.termFrequency
        loansPayload.loanTermFrequencyType = loanTemplate?.interestRateFrequencyType?.id
        loansPayload.numberOfRepayments = loanTemplate?.numberOfRepayments
        loansPayload.repaymentEvery = loanTemplate?.repaymentEvery
        loansPayload.repaymentFrequencyType = loanTemplate?.interestRateFrequencyType?.id
        loansPayload.interestRatePerPeriod = loanTemplate?.interestRatePerPeriod
        loansPayload.interestType = loanTemplate?.interestType?.id
        loansPayload.interestCalculationPeriodType = loanTemplate?.interestCalculationPeriodType?.id
        loansPayload.amortizationType = loanTemplate?.amortizationType?.id
        loansPayload.transactionProcessingStrategyId = loanTemplate?.transactionProcessingStrategyId
        loansPayload.expectedDisbursementDate = disbursementDate
        (activity as BaseActivity?)?.replaceFragment(newInstance(loanState,
                loansPayload,
                loanWithAssociations?.id?.toLong(),
                tvNewLoanApplication?.text.toString(),
                tvAccountNumber?.text.toString()),
                false, R.id.container)
    }

    /**
     * Retries to fetch [LoanTemplate] by calling `loadLoanTemplate()`
     */
    @OnClick(R.id.iv_status)
    fun onRetry() {
        llError?.visibility = View.GONE
        llAddLoan?.visibility = View.VISIBLE
        loadLoanTemplate()
    }

    /**
     * Initializes `tvSubmissionDate` with current Date
     */
    private fun inflateSubmissionDate() {
        tvSubmissionDate?.text = MFDatePicker.datePickedAsString
    }

    /**
     * Initializes `tvExpectedDisbursementDate` with current Date
     */
    private fun inflateDisbursementDate() {
        mfDatePicker = MFDatePicker.newInstance(this, MFDatePicker.FUTURE_DAYS, active)
        tvExpectedDisbursementDate?.text = MFDatePicker.datePickedAsString
        active = true
    }

    /**
     * Sets `submittedDate` and `disbursementDate` in a specific format
     */
    fun setSubmissionDisburseDate() {
        disbursementDate = tvExpectedDisbursementDate?.text.toString()
        submittedDate = tvSubmissionDate?.text.toString()
        submittedDate = DateHelper.getSpecificFormat(DateHelper.FORMAT_dd_MMMM_yyyy, submittedDate)
        disbursementDate = DateHelper.getSpecificFormat(
                DateHelper.FORMAT_dd_MMMM_yyyy, disbursementDate)
    }

    /**
     * Shows a [DialogFragment] for selecting a Date for Disbursement
     */
    @OnClick(R.id.ll_expected_disbursement_date_edit)
    fun setTvDisbursementOnDate() {
        isDisbursementDate = true
        mfDatePicker?.show(activity?.supportFragmentManager, Constants.DFRAG_DATE_PICKER)
    }

    /**
     * A CallBack for [MFDatePicker] which provides us with the date selected from the
     * [android.app.DatePickerDialog]
     *
     * @param date Date selected by user in [String]
     */
    override fun onDatePicked(date: String?) {
        if (isDisbursementDate) {
            tvExpectedDisbursementDate?.text = date
            disbursementDate = date
            isDisbursementDate = false
        }
        setSubmissionDisburseDate()
    }

    /**
     * Initializes the layout
     */
    override fun showUserInterface() {
        loanProductAdapter = ArrayAdapter(activity, android.R.layout.simple_spinner_item,
                listLoanProducts)
        loanProductAdapter?.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spLoanProducts?.adapter = loanProductAdapter
        spLoanProducts?.onItemSelectedListener = this
        loanPurposeAdapter = ArrayAdapter(activity, android.R.layout.simple_spinner_item,
                listLoanPurpose)
        loanPurposeAdapter?.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spLoanPurpose?.adapter = loanPurposeAdapter
        spLoanPurpose?.onItemSelectedListener = this
        inflateSubmissionDate()
        inflateDisbursementDate()
        setSubmissionDisburseDate()
    }

    /**
     * Fetches the [LoanTemplate] from server for `loanState` as CREATE
     *
     * @param loanTemplate Template for Loan Application
     */
    override fun showLoanTemplate(loanTemplate: LoanTemplate?) {
        this.loanTemplate = loanTemplate
        if (loanTemplate?.productOptions != null)
            for ((_, name) in loanTemplate.productOptions) {
                listLoanProducts.add(name)
            }
        loanProductAdapter?.notifyDataSetChanged()
    }

    /**
     * Fetches the [LoanTemplate] from server for `loanState` as UPDATE
     *
     * @param loanTemplate Template for Loan Application
     */
    override fun showUpdateLoanTemplate(loanTemplate: LoanTemplate?) {
        this.loanTemplate = loanTemplate
        if (loanTemplate?.productOptions != null)
            for ((_, name) in loanTemplate.productOptions) {
                listLoanProducts.add(name)
            }
        loanProductAdapter?.notifyDataSetChanged()
        spLoanProducts?.setSelection(loanProductAdapter!!
                .getPosition(loanWithAssociations?.loanProductName))
        tvAccountNumber?.text = getString(R.string.string_and_string,
                getString(R.string.account_number) + " ", loanWithAssociations?.accountNo)
        tvNewLoanApplication?.text = getString(R.string.string_and_string,
                getString(R.string.update_loan_application) + " ",
                loanWithAssociations?.clientName)
        tilPrincipalAmount?.editText?.setText(String.format(Locale.getDefault(),
                "%.2f", loanWithAssociations?.principal))
        tvCurrency?.text = loanWithAssociations?.currency?.displayLabel
        tvSubmissionDate?.text = DateHelper.getDateAsString(loanWithAssociations?.timeline?.submittedOnDate, "dd-MM-yyyy")
        tvExpectedDisbursementDate?.text = DateHelper.getDateAsString(loanWithAssociations?.timeline?.expectedDisbursementDate, "dd-MM-yyyy")
        setSubmissionDisburseDate()
    }

    /**
     * Fetches the [LoanTemplate] according to product from server for `loanState` as
     * CREATE
     *
     * @param loanTemplate Template for Loan Application
     */
    override fun showLoanTemplateByProduct(loanTemplate: LoanTemplate?) {
        this.loanTemplate = loanTemplate
        tvAccountNumber?.text = getString(R.string.string_and_string,
                getString(R.string.account_number) + " ", loanTemplate?.clientAccountNo)
        tvNewLoanApplication?.text = getString(R.string.string_and_string,
                getString(R.string.new_loan_application) + " ", loanTemplate?.clientName)
        tilPrincipalAmount?.editText?.setText(loanTemplate?.principal.toString())
        tvCurrency?.text = loanTemplate?.currency?.displayLabel
        listLoanPurpose.clear()
        var index = -1;
        listLoanPurpose.add(activity?.getString(R.string.loan_purpose_not_provided))
        if (loanTemplate?.loanPurposeOptions != null)
            for (loanPurposeOptions in loanTemplate.loanPurposeOptions) {
                if (purposeId!! > 0 && loanPurposeOptions.id?.equals(purposeId)!!)
                    index = loanTemplate.loanPurposeOptions.indexOf(loanPurposeOptions)
                listLoanPurpose.add(loanPurposeOptions.name)
            }
        loanPurposeAdapter?.notifyDataSetChanged()
        spLoanPurpose?.setSelection(index + 1)
    }

    /**
     * Fetches the [LoanTemplate] according to product from server for `loanState` as
     * UPDATE
     *
     * @param loanTemplate Template for Loan Application
     */
    override fun showUpdateLoanTemplateByProduct(loanTemplate: LoanTemplate?) {
        this.loanTemplate = loanTemplate
        listLoanPurpose.clear()
        var index = -1;
        listLoanPurpose.add(activity?.getString(R.string.loan_purpose_not_provided))
        if (loanTemplate?.loanPurposeOptions != null)
            for (loanPurposeOptions in loanTemplate.loanPurposeOptions) {
                if (purposeId!! > 0 && loanPurposeOptions.id?.equals(purposeId)!!)
                    index = loanTemplate.loanPurposeOptions.indexOf(loanPurposeOptions)
                listLoanPurpose.add(loanPurposeOptions.name)
            }
        loanPurposeAdapter?.notifyDataSetChanged()
        spLoanPurpose?.setSelection(index + 1)
        if (isLoanUpdatePurposesInitialization &&
                loanWithAssociations?.loanPurposeName != null) {
            spLoanPurpose?.setSelection(loanPurposeAdapter!!
                    .getPosition(loanWithAssociations?.loanPurposeName))
            isLoanUpdatePurposesInitialization = false
        } else {
            tvAccountNumber?.text = getString(R.string.string_and_string,
                    getString(R.string.account_number) + " ", loanTemplate?.clientAccountNo)
            tvNewLoanApplication?.text = getString(R.string.string_and_string,
                    getString(R.string.new_loan_application) + " ", loanTemplate?.clientName)
            tilPrincipalAmount?.editText?.setText(loanTemplate?.principal.toString())
            tvCurrency?.text = loanTemplate?.currency?.displayLabel
        }
    }

    /**
     * It is called whenever any error occurs while executing a request
     *
     * @param message Error message that tells the user about the problem.
     */
    override fun showError(message: String?) {
        if (!Network.isConnected(activity)) {
            ivReload?.setImageResource(R.drawable.ic_error_black_24dp)
            tvErrorStatus?.text = getString(R.string.internet_not_connected)
            llAddLoan?.visibility = View.GONE
            llError?.visibility = View.VISIBLE
        } else {
            Toaster.show(rootView, message)
        }
    }

    override fun showProgress() {
        llAddLoan?.visibility = View.GONE
        showProgressBar()
    }

    override fun hideProgress() {
        llAddLoan?.visibility = View.VISIBLE
        hideProgressBar()
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        when (parent?.id) {
            R.id.sp_loan_products -> {
                if (loanTemplate?.productOptions?.size!! > position) {
                    if (loanTemplate?.productOptions?.get(position)?.id != productId)
                        purposeId = -1
                    productId = loanTemplate?.productOptions?.get(position)?.id
                    if (loanState == LoanState.CREATE) {
                        loanApplicationPresenter?.loadLoanApplicationTemplateByProduct(productId,
                                LoanState.CREATE)
                    } else {
                        loanApplicationPresenter?.loadLoanApplicationTemplateByProduct(productId,
                                LoanState.UPDATE)
                    }
                }
            }
            R.id.sp_loan_purpose -> {
                if (position == 0) purposeId = -1
                else if (loanTemplate?.loanPurposeOptions?.size!! > position - 1)
                    purposeId = loanTemplate?.loanPurposeOptions?.get(position - 1)?.id
            }
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {}
    override fun onDestroyView() {
        super.onDestroyView()
        hideProgressBar()
        loanApplicationPresenter?.detachView()
    }

    companion object {
        /**
         * Used when we want to apply for a Loan
         *
         * @param loanState [LoanState] is set to `LoanState.CREATE`
         * @return Instance of [LoanApplicationFragment]
         */
        fun newInstance(loanState: LoanState?): LoanApplicationFragment {
            val fragment = LoanApplicationFragment()
            val args = Bundle()
            args.putSerializable(Constants.LOAN_STATE, loanState)
            fragment.arguments = args
            return fragment
        }

        /**
         * Used when we want to update a Loan Application
         *
         * @param loanState            [LoanState] is set to `LoanState.UPDATE`
         * @param loanWithAssociations [LoanAccount] to modify
         * @return Instance of [LoanApplicationFragment]
         */
        fun newInstance(
                loanState: LoanState?,
                loanWithAssociations: LoanWithAssociations?
        ): LoanApplicationFragment {
            val fragment = LoanApplicationFragment()
            val args = Bundle()
            args.putSerializable(Constants.LOAN_STATE, loanState)
            args.putParcelable(Constants.LOAN_ACCOUNT, loanWithAssociations)
            fragment.arguments = args
            return fragment
        }
    }
}