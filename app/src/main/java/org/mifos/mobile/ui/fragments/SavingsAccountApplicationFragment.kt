package org.mifos.mobile.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast

import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick

import org.mifos.mobile.R
import org.mifos.mobile.api.local.PreferencesHelper
import org.mifos.mobile.models.accounts.savings.SavingsAccountApplicationPayload
import org.mifos.mobile.models.accounts.savings.SavingsAccountUpdatePayload
import org.mifos.mobile.models.accounts.savings.SavingsWithAssociations
import org.mifos.mobile.models.templates.savings.ProductOptions
import org.mifos.mobile.models.templates.savings.SavingsAccountTemplate
import org.mifos.mobile.presenters.SavingsAccountApplicationPresenter
import org.mifos.mobile.ui.activities.base.BaseActivity
import org.mifos.mobile.ui.enums.SavingsAccountState
import org.mifos.mobile.ui.fragments.base.BaseFragment
import org.mifos.mobile.ui.views.SavingsAccountApplicationView
import org.mifos.mobile.utils.Constants
import org.mifos.mobile.utils.DateHelper
import org.mifos.mobile.utils.MFDatePicker

import java.util.*
import javax.inject.Inject

/*
* Created by saksham on 30/June/2018
*/
class SavingsAccountApplicationFragment : BaseFragment(), SavingsAccountApplicationView {

    @kotlin.jvm.JvmField
    @BindView(R.id.sp_product_id)
    var spProductId: Spinner? = null

    @kotlin.jvm.JvmField
    @BindView(R.id.tv_submission_date)
    var tvSubmissionDate: TextView? = null

    @kotlin.jvm.JvmField
    @BindView(R.id.tv_client_name)
    var tvClientName: TextView? = null

    @kotlin.jvm.JvmField
    @Inject
    var presenter: SavingsAccountApplicationPresenter? = null

    @kotlin.jvm.JvmField
    @Inject
    var preferencesHelper: PreferencesHelper? = null
    private var rootView: View? = null
    private var state: SavingsAccountState? = null
    private var savingsWithAssociations: SavingsWithAssociations? = null
    private var productIdAdapter: ArrayAdapter<String?>? = null
    private var template: SavingsAccountTemplate? = null
    private var productOptions: List<ProductOptions>? = null
    private val productIdList: MutableList<String?> = ArrayList()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            state = arguments!!
                    .getSerializable(Constants.SAVINGS_ACCOUNT_STATE) as SavingsAccountState
            savingsWithAssociations = arguments?.getParcelable(Constants.SAVINGS_ACCOUNTS)
        }
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.fragment_savings_account_application, container,
                false)
        ButterKnife.bind(this, rootView!!)
        (activity as BaseActivity?)?.activityComponent?.inject(this)
        presenter?.attachView(this)
        presenter?.loadSavingsAccountApplicationTemplate(preferencesHelper?.clientId, state)
        return rootView
    }

    override fun showUserInterfaceSavingAccountApplication(template: SavingsAccountTemplate?) {
        showUserInterface(template)
    }

    override fun showSavingsAccountApplicationSuccessfully() {
        showMessage(getString(R.string.new_saving_account_created_successfully))
        activity?.finish()
    }

    override fun showUserInterfaceSavingAccountUpdate(template: SavingsAccountTemplate?) {
        showUserInterface(template)
        activity?.title = getString(R.string.string_savings_account,
                getString(R.string.update))
        productIdAdapter?.getPosition(savingsWithAssociations
                ?.savingsProductName)?.let { spProductId?.setSelection(it) }
    }

    override fun showSavingsAccountUpdateSuccessfully() {
        showMessage(getString(R.string.saving_account_updated_successfully))
        activity?.supportFragmentManager?.popBackStack()
    }

    fun showUserInterface(template: SavingsAccountTemplate?) {
        this.template = template
        productOptions = template?.productOptions
        if (productOptions != null)
            for ((_, name) in productOptions as ArrayList<ProductOptions>) {
                productIdList.add(name)
            }
        tvClientName?.text = template?.clientName
        productIdAdapter = ArrayAdapter(context, android.R.layout.simple_spinner_item,
                productIdList)
        productIdAdapter?.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spProductId?.adapter = productIdAdapter
        tvSubmissionDate?.text = MFDatePicker.datePickedAsString
    }

    private fun submitSavingsAccountApplication() {
        val payload = SavingsAccountApplicationPayload()
        payload.clientId = template?.clientId
        payload.productId = spProductId?.selectedItemPosition?.let { productOptions?.get(it)?.id }
        payload.submittedOnDate = DateHelper.getSpecificFormat(DateHelper.FORMAT_dd_MMMM_yyyy,
                MFDatePicker.datePickedAsString)
        presenter?.submitSavingsAccountApplication(payload)
    }

    private fun updateSavingAccount() {
        val payload = SavingsAccountUpdatePayload()
        payload.clientId = template?.clientId?.toLong()
        payload.productId = spProductId?.selectedItemPosition?.let { productOptions?.get(it)?.id }?.toLong()
        presenter?.updateSavingsAccount(savingsWithAssociations?.id, payload)
    }

    @OnClick(R.id.btn_submit)
    fun onSubmit() {
        if (state == SavingsAccountState.CREATE) {
            submitSavingsAccountApplication()
        } else {
            updateSavingAccount()
        }
    }

    override fun showError(error: String?) {
        Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
    }

    override fun showMessage(showMessage: String?) {
        Toast.makeText(context, showMessage, Toast.LENGTH_SHORT).show()
    }

    override fun showProgress() {
        showMifosProgressDialog(getString(R.string.progress_message_loading))
    }

    override fun hideProgress() {
        hideMifosProgressDialog()
    }

    override fun onDestroy() {
        super.onDestroy()
        hideProgress()
        presenter?.detachView()
    }

    companion object {
        @kotlin.jvm.JvmStatic
        fun newInstance(
                state: SavingsAccountState?, savingsWithAssociations: SavingsWithAssociations?
        ): SavingsAccountApplicationFragment {
            val fragment = SavingsAccountApplicationFragment()
            val bundle = Bundle()
            bundle.putSerializable(Constants.SAVINGS_ACCOUNT_STATE, state)
            bundle.putParcelable(Constants.SAVINGS_ACCOUNTS, savingsWithAssociations)
            fragment.arguments = bundle
            return fragment
        }
    }
}