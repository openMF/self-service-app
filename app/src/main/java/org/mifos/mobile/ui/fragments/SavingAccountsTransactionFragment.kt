package org.mifos.mobile.ui.fragments

import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.view.*
import android.widget.*

import androidx.appcompat.widget.AppCompatCheckBox
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick

import com.github.therajanmaurya.sweeterror.SweetUIErrorHandler
import org.florescu.android.rangeseekbar.RangeSeekBar

import org.mifos.mobile.R
import org.mifos.mobile.models.CheckboxStatus
import org.mifos.mobile.models.accounts.savings.SavingsWithAssociations
import org.mifos.mobile.models.accounts.savings.Transactions
import org.mifos.mobile.presenters.SavingAccountsTransactionPresenter
import org.mifos.mobile.ui.activities.base.BaseActivity
import org.mifos.mobile.ui.adapters.CheckBoxAdapter
import org.mifos.mobile.ui.adapters.SavingAccountsTransactionListAdapter
import org.mifos.mobile.ui.fragments.base.BaseFragment
import org.mifos.mobile.ui.views.SavingAccountsTransactionView
import org.mifos.mobile.utils.*
import org.mifos.mobile.utils.MFDatePicker.OnDatePickListener

import java.util.*
import javax.inject.Inject

/**
 * Created by dilpreet on 6/3/17.
 */
class SavingAccountsTransactionFragment : BaseFragment(), SavingAccountsTransactionView, OnDatePickListener, RangeSeekBar.OnRangeSeekBarChangeListener<Float> {

    @kotlin.jvm.JvmField
    @BindView(R.id.ll_account)
    var layoutAccount: LinearLayout? = null

    @kotlin.jvm.JvmField
    @BindView(R.id.layout_error)
    var layoutError: View? = null

    @kotlin.jvm.JvmField
    @BindView(R.id.rv_saving_accounts_transaction)
    var rvSavingAccountsTransaction: RecyclerView? = null

    @kotlin.jvm.JvmField
    @Inject
    var transactionListAdapter: SavingAccountsTransactionListAdapter? = null

    @kotlin.jvm.JvmField
    @Inject
    var savingAccountsTransactionPresenter: SavingAccountsTransactionPresenter? = null

    @kotlin.jvm.JvmField
    @Inject
    var checkBoxAdapter: CheckBoxAdapter? = null
    private var tvStartDate: TextView? = null
    private var tvEndDate: TextView? = null
    private var sweetUIErrorHandler: SweetUIErrorHandler? = null
    private var rootView: View? = null
    private var savingsId: Long = 0
    private var transactionsList: List<Transactions?>? = null
    private var savingsWithAssociations: SavingsWithAssociations? = null
    private var datePick: DatePick? = null
    private var mfDatePicker: MFDatePicker? = null
    private var startDate: Long? = 0
    private var endDate: Long? = 0
    private var isReady = false
    private var statusList: List<CheckboxStatus>? = null
    private var isCheckBoxPeriod = false
    private var selectedRadioButtonId = 0
    private var filterMinAmount: Float? = 0f
    private var filterMaxAmount: Float? = 0f
    var active = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        (activity as BaseActivity?)?.activityComponent?.inject(this)
        setToolbarTitle(getString(R.string.saving_account_transactions_details))
        if (arguments != null) savingsId = arguments?.getLong(Constants.SAVINGS_ID)!!
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.fragment_saving_account_transactions,
                container, false)
        ButterKnife.bind(this, rootView!!)
        savingAccountsTransactionPresenter?.attachView(this)
        sweetUIErrorHandler = SweetUIErrorHandler(context, rootView)
        showUserInterface()
        if (savedInstanceState == null) {
            savingAccountsTransactionPresenter?.loadSavingsWithAssociations(savingsId)
        }
        initializeFilterVariables()
        return rootView
    }

    private fun initializeFilterVariables() {
        statusList = StatusUtils.getSavingsAccountTransactionList(activity)
        startDate = -1
        endDate = -2
        isCheckBoxPeriod = false
        isReady = false
        selectedRadioButtonId = -1
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
     * Setting up basic components
     */
    override fun showUserInterface() {
        val layoutManager = LinearLayoutManager(activity)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        rvSavingAccountsTransaction?.setHasFixedSize(true)
        rvSavingAccountsTransaction?.layoutManager = layoutManager
        rvSavingAccountsTransaction?.adapter = transactionListAdapter

//        radioGroup.setOnCheckedChangeListener(this);
        mfDatePicker = MFDatePicker.newInstance(this, MFDatePicker.ALL_DAYS, active)
        active = true
    }

    /**
     * Provides with `savingsWithAssociations` fetched from server which is used to update the
     * `transactionListAdapter`
     *
     * @param savingsWithAssociations Contains [Transactions] for given Savings account.
     */
    override fun showSavingAccountsDetail(savingsWithAssociations: SavingsWithAssociations?) {
        layoutAccount?.visibility = View.VISIBLE
        this.savingsWithAssociations = savingsWithAssociations
        transactionsList = savingsWithAssociations?.transactions
        if (transactionsList != null && transactionsList?.isNotEmpty() == true) {
            transactionListAdapter?.setContext(context)
            transactionListAdapter?.setSavingAccountsTransactionList(transactionsList)
        } else {
            showEmptyTransactions()
        }
    }

    /**
     * It is called whenever any error occurs while executing a request
     *
     * @param message Error message that tells the user about the problem.
     */
    override fun showErrorFetchingSavingAccountsDetail(message: String?) {
        if (!Network.isConnected(activity)) {
            sweetUIErrorHandler?.showSweetNoInternetUI(rvSavingAccountsTransaction, layoutError)
        } else {
            sweetUIErrorHandler?.showSweetErrorUI(message, rvSavingAccountsTransaction, layoutError)
        }
    }

    @OnClick(R.id.btn_try_again)
    fun retryClicked() {
        if (Network.isConnected(context)) {
            sweetUIErrorHandler?.hideSweetErrorLayoutUI(rvSavingAccountsTransaction, layoutError)
            savingAccountsTransactionPresenter?.loadSavingsWithAssociations(savingsId)
        } else {
            Toast.makeText(context, getString(R.string.internet_not_connected),
                    Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Provides with a filtered list according to the constraints used in `filter()` function
     */
    override fun showFilteredList(list: List<Transactions?>?) {
        if (list != null && list.isNotEmpty()) {
            Toaster.show(rootView, getString(R.string.filtered))
            transactionListAdapter?.setSavingAccountsTransactionList(list)
        } else {
            showEmptyTransactions()
        }
    }

    override fun showEmptyTransactions() {
        sweetUIErrorHandler?.showSweetEmptyUI(getString(R.string.transactions),
                R.drawable.ic_compare_arrows_black_24dp, rvSavingAccountsTransaction, layoutError)
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

    private fun startDatePick() {
        datePick = DatePick.START
        mfDatePicker?.show(activity?.supportFragmentManager, Constants.DFRAG_DATE_PICKER)
    }

    private fun endDatePick() {
        datePick = DatePick.END
        mfDatePicker?.show(activity?.supportFragmentManager, Constants.DFRAG_DATE_PICKER)
    }

    /**
     * A CallBack for [MFDatePicker] which provides us with the date selected from the
     * [android.app.DatePickerDialog] by `mfDatePicker`
     *
     * @param date Date selected by user in [String]
     */
    override fun onDatePicked(date: String?) {
        val timeInMillis = DateHelper.getDateAsLongFromString(date, "dd-MM-yyyy")
        if (datePick == DatePick.START) {
            tvEndDate?.isEnabled = true
            tvStartDate?.text = DateHelper.getDateAsStringFromLong(timeInMillis)
            startDate = timeInMillis
        } else {
            endDate = timeInMillis
            tvEndDate?.text = DateHelper.getDateAsStringFromLong(timeInMillis)
            isReady = true
        }
    }

    /**
     * Checks if `startDate` is less than `endDate`
     *
     * @return Returns true if `startDate` is less than `endDate`
     */
    private fun isEndDateLargeThanStartDate(): Boolean? {
        return if (startDate != null && endDate != null)
            startDate!! <= endDate!!
        else null
    }

    override fun showProgress() {
        showProgressBar()
    }

    override fun hideProgress() {
        hideProgressBar()
    }

    /**
     * Shows a filter dialog
     */
    private fun showFilterDialog() {
        val inflater = activity?.layoutInflater
        val dialogView = inflater?.inflate(R.layout.layout_filter_dialog, null, false)
        val llcheckBoxPeriod = dialogView?.findViewById<LinearLayout>(R.id.ll_row_checkbox)
        val checkBoxPeriod: AppCompatCheckBox? = dialogView?.findViewById(R.id.cb_select)
        val radioGroupFilter = dialogView?.findViewById<RadioGroup>(R.id.rg_date_filter)
        val amountCheckBox = dialogView?.findViewById<AppCompatCheckBox>(R.id.amount_select)
        val llRowAmount = dialogView?.findViewById<LinearLayout>(R.id.ll_row_amount)
        val seekBar: RangeSeekBar<Float> = dialogView!!.findViewById(R.id.filter_seekBar)
        tvStartDate = dialogView?.findViewById(R.id.tv_start_date)
        tvEndDate = dialogView?.findViewById(R.id.tv_end_date)
        tvStartDate?.isEnabled = false
        tvEndDate?.isEnabled = false

        //setup listeners
        seekBar.setRangeValues(getRangeMinValue(),getRangeMaxValue())
        llRowAmount?.setOnClickListener { amountCheckBox?.isChecked = (amountCheckBox?.isChecked != true) }
        amountCheckBox?.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                seekBar.visibility = View.VISIBLE
            } else {
                seekBar.visibility = View.GONE
            }
        }
        seekBar.setOnRangeSeekBarChangeListener(this)
        tvStartDate?.setOnClickListener { startDatePick() }
        tvEndDate?.setOnClickListener { endDatePick() }
        llcheckBoxPeriod?.setOnClickListener { checkBoxPeriod?.isChecked = (checkBoxPeriod?.isChecked != true) }
        checkBoxPeriod?.setOnCheckedChangeListener { _, isChecked ->
            isCheckBoxPeriod = isChecked
            if (!isChecked) {
                isReady = false
                radioGroupFilter?.clearCheck()
                selectedRadioButtonId = -1
            } else {
                if (selectedRadioButtonId == -1) {
                    val btn = dialogView.findViewById<RadioButton>(R.id.rb_date)
                    btn.isChecked = true
                }
            }
        }
        radioGroupFilter?.setOnCheckedChangeListener { radioGroup, _ ->
            isCheckBoxPeriod = true
            selectedRadioButtonId = radioGroup.checkedRadioButtonId
            when (radioGroup.checkedRadioButtonId) {
                R.id.rb_four_weeks -> {
                    tvStartDate?.isEnabled = false
                    tvEndDate?.isEnabled = false
                    startDate = DateHelper.subtractWeeks(4)
                    endDate = System.currentTimeMillis()
                    isReady = true
                }
                R.id.rb_three_months -> {
                    tvStartDate?.isEnabled = false
                    tvEndDate?.isEnabled = false
                    startDate = DateHelper.subtractMonths(3)
                    endDate = System.currentTimeMillis()
                    isReady = true
                }
                R.id.rb_six_months -> {
                    tvStartDate?.isEnabled = false
                    tvEndDate?.isEnabled = false
                    startDate = DateHelper.subtractMonths(6)
                    endDate = System.currentTimeMillis()
                    isReady = true
                }
                R.id.rb_date -> {
                    tvStartDate?.isEnabled = true
                    tvEndDate?.isEnabled = false
                }
            }
        }

        //restore prev state
        checkBoxPeriod?.isChecked = isCheckBoxPeriod
        if (selectedRadioButtonId != -1) {
            val btn = dialogView?.findViewById<RadioButton>(selectedRadioButtonId)
            btn?.isChecked = true
        }
        val checkBoxRecyclerView: RecyclerView? = dialogView?.findViewById(R.id.recycler_view)
        val layoutManager = LinearLayoutManager(activity)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        checkBoxRecyclerView?.layoutManager = layoutManager
        checkBoxRecyclerView?.adapter = checkBoxAdapter
        checkBoxAdapter?.statusList = statusList
        MaterialDialog.Builder().init(activity)
                .setTitle(R.string.savings_account_transaction)
                .addView(dialogView)
                .setPositiveButton(getString(R.string.filter), DialogInterface.OnClickListener { _, _ ->
                    when {
                        amountCheckBox?.isChecked!! -> {
                            filterByAmount(filterMinAmount,filterMaxAmount)
                        }
                        checkBoxPeriod?.isChecked == true -> {
                            if (!isReady) {
                                Toaster.show(rootView, getString(R.string.select_date))
                                return@OnClickListener
                            } else if (isEndDateLargeThanStartDate() == false) {
                                Toaster.show(rootView,
                                        getString(R.string.end_date_must_be_greater))
                                return@OnClickListener
                            }
                            filter(startDate, endDate, checkBoxAdapter?.statusList)
                        }
                        else -> {
                            filter(checkBoxAdapter?.statusList)
                        }
                    }
                    filterSavingsAccountTransactionsbyType(checkBoxAdapter?.statusList)
                })
                .setNeutralButton(getString(R.string.clear_filters),
                        DialogInterface.OnClickListener { _, _ ->
                            transactionListAdapter
                                    ?.setSavingAccountsTransactionList(transactionsList)
                            initializeFilterVariables()
                        }
                )
                .setNegativeButton(R.string.cancel)
                .createMaterialDialog()
                .show()
    }

    private fun getRangeMinValue(): Float? {
        var min = transactionsList?.get(0)?.amount
        for(t in transactionsList!!) if(t?.amount!! < min!!) min = t.amount
        return min!!.toFloat()
    }

    private fun getRangeMaxValue(): Float? {
        var max = transactionsList?.get(0)?.amount
        for(t in transactionsList!!) if(t?.amount!! > max!!) max = t.amount
        return max!!.toFloat()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_saving_accounts_transaction, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    private fun filterByAmount(min: Float?, max: Float?): MutableList<Transactions?>? {
        val filteredSavingsTransactions: MutableList<Transactions?>? = ArrayList()
        savingAccountsTransactionPresenter?.filterTransactionListByAmount(transactionsList,
                min, max).let { filteredSavingsTransactions?.addAll(it!!) }
        showFilteredList(filteredSavingsTransactions)
        return filteredSavingsTransactions
    }

    /**
     * Will filter `transactionsList` according to `startDate` and `endDate`
     *
     * @param startDate Starting date
     * @param endDate   Ending date
     */
    private fun filter(startDate: Long?, endDate: Long?, statusModelList: List<CheckboxStatus?>?) {
        val dummyTransactionList = filterSavingsAccountTransactionsbyType(statusModelList)
        savingAccountsTransactionPresenter?.filterTransactionList(dummyTransactionList,
                startDate, endDate)
    }

    /**
     * Will filter `transactionsList` according to `startDate` and `endDate`
     *
     * @param statusModelList Status Model List
     */
    private fun filter(statusModelList: List<CheckboxStatus?>?) {
        showFilteredList(filterSavingsAccountTransactionsbyType(statusModelList))
    }

    /**
     * Will filter `transactionsList` according to `startDate` and `endDate`
     * @param statusModelList Status Model List
     */
    private fun filterSavingsAccountTransactionsbyType(statusModelList: List<CheckboxStatus?>?): List<Transactions?>? {
        val filteredSavingsTransactions: MutableList<Transactions?>? = ArrayList()
        if (savingAccountsTransactionPresenter != null)
            for (status in savingAccountsTransactionPresenter
                    ?.getCheckedStatus(statusModelList)!!) {
                savingAccountsTransactionPresenter
                        ?.filterTranactionListbyType(transactionsList, status)?.let { filteredSavingsTransactions?.addAll(it) }
            }
        return filteredSavingsTransactions
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_filter_savings_transactions -> showFilterDialog()
        }
        return true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        hideProgress()
        savingAccountsTransactionPresenter?.detachView()
    }

    companion object {
        fun newInstance(savingsId: Long?): SavingAccountsTransactionFragment {
            val fragment = SavingAccountsTransactionFragment()
            val args = Bundle()
            if (savingsId != null) args.putLong(Constants.SAVINGS_ID, savingsId)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onRangeSeekBarValuesChanged(bar: RangeSeekBar<*>?, minValue: Float?, maxValue: Float?) {
        filterMaxAmount = maxValue
        filterMinAmount = minValue
    }
}