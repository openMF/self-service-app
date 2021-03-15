package org.mifos.mobile.ui.fragments

import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.Spinner
import androidx.core.widget.NestedScrollView
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.github.therajanmaurya.sweeterror.SweetUIErrorHandler
import com.google.android.material.textfield.TextInputLayout
import org.mifos.mobile.R
import org.mifos.mobile.models.beneficiary.Beneficiary
import org.mifos.mobile.models.beneficiary.BeneficiaryPayload
import org.mifos.mobile.models.beneficiary.BeneficiaryUpdatePayload
import org.mifos.mobile.models.templates.beneficiary.BeneficiaryTemplate
import org.mifos.mobile.presenters.BeneficiaryApplicationPresenter
import org.mifos.mobile.ui.activities.base.BaseActivity
import org.mifos.mobile.ui.enums.BeneficiaryState
import org.mifos.mobile.ui.fragments.base.BaseFragment
import org.mifos.mobile.ui.views.BeneficiaryApplicationView
import org.mifos.mobile.utils.Constants
import org.mifos.mobile.utils.Network
import org.mifos.mobile.utils.Toaster
import java.util.*
import javax.inject.Inject

/**
 * Created by dilpreet on 16/6/17.
 */
class BeneficiaryApplicationFragment : BaseFragment(), BeneficiaryApplicationView, OnItemSelectedListener {
    @kotlin.jvm.JvmField
    @BindView(R.id.sp_account_type)
    var spAccountType: Spinner? = null

    @kotlin.jvm.JvmField
    @BindView(R.id.ll_application_beneficiary)
    var llApplicationBeneficiary: LinearLayout? = null

    @kotlin.jvm.JvmField
    @BindView(R.id.til_account_number)
    var tilAccountNumber: TextInputLayout? = null

    @kotlin.jvm.JvmField
    @BindView(R.id.til_office_name)
    var tilOfficeName: TextInputLayout? = null

    @kotlin.jvm.JvmField
    @BindView(R.id.til_transfer_limit)
    var tilTransferLimit: TextInputLayout? = null

    @kotlin.jvm.JvmField
    @BindView(R.id.til_beneficiary_name)
    var tilBeneficiaryName: TextInputLayout? = null

    @kotlin.jvm.JvmField
    @BindView(R.id.layout_error)
    var layoutError: View? = null

    @kotlin.jvm.JvmField
    @BindView(R.id.view_flipper)
    var nsvBeneficiary: NestedScrollView? = null

    @kotlin.jvm.JvmField
    @Inject
    var presenter: BeneficiaryApplicationPresenter? = null
    private val listAccountType: MutableList<String?> = ArrayList()
    private var accountTypeAdapter: ArrayAdapter<String?>? = null
    private var beneficiaryState: BeneficiaryState? = null
    private var beneficiary: Beneficiary? = null
    private var beneficiaryTemplate: BeneficiaryTemplate? = null
    private var accountTypeId: Int? = -1
    private var rootView: View? = null
    private var sweetUIErrorHandler: SweetUIErrorHandler? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setToolbarTitle(getString(R.string.add_beneficiary))
        if (arguments != null) {
            beneficiaryState = arguments!!
                    .getSerializable(Constants.BENEFICIARY_STATE) as BeneficiaryState
            when (beneficiaryState) {
                BeneficiaryState.UPDATE -> {
                    beneficiary = arguments?.getParcelable(Constants.BENEFICIARY)
                    setToolbarTitle(getString(R.string.update_beneficiary))
                }
                BeneficiaryState.CREATE_QR -> {
                    beneficiary = arguments?.getParcelable(Constants.BENEFICIARY)
                    setToolbarTitle(getString(R.string.add_beneficiary))
                }
                else -> {
                    setToolbarTitle(getString(R.string.add_beneficiary))
                }
            }
        }
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.fragment_beneficiary_application, container, false)
        (activity as BaseActivity?)?.activityComponent?.inject(this)
        ButterKnife.bind(this, rootView!!)
        sweetUIErrorHandler = SweetUIErrorHandler(activity, rootView)
        showUserInterface()
        presenter?.attachView(this)
        if (savedInstanceState == null) {
            presenter?.loadBeneficiaryTemplate()
        }
        return rootView
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(Constants.TEMPLATE, beneficiaryTemplate)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (savedInstanceState != null) {
            showBeneficiaryTemplate(savedInstanceState.getParcelable<Parcelable>(Constants.TEMPLATE) as BeneficiaryTemplate)
        }
    }

    /**
     * Setting up `accountTypeAdapter` and `` spAccountType
     */
    override fun showUserInterface() {
        accountTypeAdapter = ArrayAdapter(activity,
                android.R.layout.simple_spinner_item, listAccountType)
        accountTypeAdapter?.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spAccountType?.onItemSelectedListener = this
        if (!Network.isConnected(context)) {
            spAccountType?.isEnabled = false
        }
        spAccountType?.adapter = accountTypeAdapter
    }

    /**
     * Fetches [BeneficiaryTemplate] from server and further updates the UI according to
     * [BeneficiaryState] which is initialized in `newInstance()` as
     * `beneficiaryState`
     *
     * @param beneficiaryTemplate [BeneficiaryTemplate] fetched from server
     */
    override fun showBeneficiaryTemplate(beneficiaryTemplate: BeneficiaryTemplate?) {
        this.beneficiaryTemplate = beneficiaryTemplate
        for ((_, _, value) in beneficiaryTemplate?.accountTypeOptions!!) {
            listAccountType.add(value)
        }
        accountTypeAdapter?.notifyDataSetChanged()
        if (beneficiaryState == BeneficiaryState.UPDATE) {
            accountTypeAdapter?.getPosition(beneficiary
                    ?.accountType?.value)?.let { spAccountType?.setSelection(it) }
            spAccountType?.isEnabled = false
            tilAccountNumber?.editText?.setText(beneficiary?.accountNumber)
            tilAccountNumber?.isEnabled = false
            tilOfficeName?.editText?.setText(beneficiary?.officeName)
            tilOfficeName?.isEnabled = false
            tilBeneficiaryName?.editText?.setText(beneficiary?.name)
            tilTransferLimit?.editText?.setText(beneficiary?.transferLimit?.toInt().toString())
        } else if (beneficiaryState == BeneficiaryState.CREATE_QR) {
            spAccountType?.setSelection(beneficiary?.accountType?.id!!)
            tilAccountNumber?.editText?.setText(beneficiary?.accountNumber)
            tilOfficeName?.editText?.setText(beneficiary?.officeName)
        }
    }

    /**
     * Used for submitting a new or updating beneficiary application
     */
    @OnClick(R.id.btn_beneficiary_submit)
    fun submitBeneficiary() {
        tilAccountNumber?.isErrorEnabled = false
        tilOfficeName?.isErrorEnabled = false
        tilTransferLimit?.isErrorEnabled = false
        tilBeneficiaryName?.isErrorEnabled = false
        if (accountTypeId == -1) {
            Toaster.show(rootView, getString(R.string.choose_account_type))
            return
        } else if (tilAccountNumber?.editText?.text.toString().trim { it <= ' ' } == "") {
            tilAccountNumber?.error = getString(R.string.enter_account_number)
            return
        } else if (tilOfficeName?.editText?.text.toString().trim { it <= ' ' } == "") {
            tilOfficeName?.error = getString(R.string.enter_office_name)
            return
        } else if (tilTransferLimit?.editText?.text.toString() == "") {
            tilTransferLimit?.error = getString(R.string.enter_transfer_limit)
            return
        } else if (tilTransferLimit?.editText?.text.toString() == ".") {
            tilTransferLimit?.error = getString(R.string.invalid_amount)
            return
        } else if (tilTransferLimit?.editText?.text.toString().matches("^0*".toRegex())) {
            tilTransferLimit?.error = getString(R.string.amount_greater_than_zero)
            return
        } else if (tilBeneficiaryName?.editText?.text.toString().trim { it <= ' ' } == "") {
            tilBeneficiaryName?.error = getString(R.string.enter_beneficiary_name)
            return
        }
        if (beneficiaryState == BeneficiaryState.CREATE_MANUAL ||
                beneficiaryState == BeneficiaryState.CREATE_QR) {
            submitNewBeneficiaryApplication()
        } else if (beneficiaryState == BeneficiaryState.UPDATE) {
            submitUpdateBeneficiaryApplication()
        }
    }

    @OnClick(R.id.btn_try_again)
    fun onRetry() {
        if (Network.isConnected(context)) {
            presenter?.loadBeneficiaryTemplate()
            sweetUIErrorHandler?.hideSweetErrorLayoutUI(nsvBeneficiary, layoutError)
        } else {
            Toaster.show(rootView, getString(R.string.internet_not_connected))
        }
    }

    /**
     * Submit a new Beneficiary application
     */
    private fun submitNewBeneficiaryApplication() {
        val beneficiaryPayload = BeneficiaryPayload()
        beneficiaryPayload.accountNumber = tilAccountNumber?.editText?.text.toString()
        beneficiaryPayload.officeName = tilOfficeName?.editText?.text.toString()
        beneficiaryPayload.accountType = accountTypeId
        beneficiaryPayload.name = tilBeneficiaryName?.editText?.text.toString()
        beneficiaryPayload.transferLimit = tilTransferLimit?.editText?.text.toString().toInt()
        presenter?.createBeneficiary(beneficiaryPayload)
    }

    /**
     * Updates an existing beneficiary application
     */
    private fun submitUpdateBeneficiaryApplication() {
        val payload = BeneficiaryUpdatePayload()
        payload.name = tilBeneficiaryName?.editText?.text.toString()
        payload.transferLimit = tilTransferLimit?.editText?.text.toString().toInt()
        presenter?.updateBeneficiary(beneficiary?.id?.toLong(), payload)
    }

    /**
     * Displays a {@link Snackbar} on successfully creation of
     * Beneficiary and pops fragments in order to go back to [BeneficiaryListFragment]
     */
    override fun showBeneficiaryCreatedSuccessfully() {
        Toaster.show(rootView, getString(R.string.beneficiary_created_successfully))
        activity?.finish()
    }

    /**
     * Displays a {@link Snackbar} on successfully updation of
     * Beneficiary and pops fragments in order to go back to [BeneficiaryListFragment]
     */
    override fun showBeneficiaryUpdatedSuccessfully() {
        Toaster.show(rootView, getString(R.string.beneficiary_updated_successfully))
        activity?.supportFragmentManager?.popBackStack()
        activity?.supportFragmentManager?.popBackStack()
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        accountTypeId = beneficiaryTemplate?.accountTypeOptions?.get(position)?.id
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {}

    /**
     * It is called whenever any error occurs while executing a request
     *
     * @param msg Error message that tells the user about the problem.
     */
    override fun showError(msg: String?) {
        if (!Network.isConnected(context)) {
            sweetUIErrorHandler?.showSweetNoInternetUI(nsvBeneficiary, layoutError)
        } else {
            sweetUIErrorHandler?.showSweetErrorUI(msg, nsvBeneficiary, layoutError)
            Toaster.show(rootView, msg)
        }
    }

    override fun setVisibility(state: Int) {
        llApplicationBeneficiary?.visibility = state
    }

    override fun showProgress() {
        showProgressBar()
    }

    override fun hideProgress() {
        hideProgressBar()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        hideProgress()
        presenter?.detachView()
    }

    companion object {
        fun newInstance(
                beneficiaryState: BeneficiaryState?,
                beneficiary: Beneficiary?
        ): BeneficiaryApplicationFragment {
            val fragment = BeneficiaryApplicationFragment()
            val args = Bundle()
            args.putSerializable(Constants.BENEFICIARY_STATE, beneficiaryState)
            if (beneficiary != null) {
                args.putParcelable(Constants.BENEFICIARY, beneficiary)
            }
            fragment.arguments = args
            return fragment
        }
    }
}