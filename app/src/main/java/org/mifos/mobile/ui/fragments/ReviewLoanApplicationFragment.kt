package org.mifos.mobile.ui.fragments

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_review_loan_application.*
import kotlinx.android.synthetic.main.layout_error.*
import okhttp3.ResponseBody

import org.mifos.mobile.R
import org.mifos.mobile.models.payload.LoansPayload
import org.mifos.mobile.ui.activities.base.BaseActivity
import org.mifos.mobile.ui.enums.AccountType
import org.mifos.mobile.ui.enums.LoanState
import org.mifos.mobile.ui.fragments.base.BaseFragment
import org.mifos.mobile.utils.MFErrorParser
import org.mifos.mobile.utils.Network
import org.mifos.mobile.utils.Toaster
import org.mifos.mobile.viewModels.ReviewLoanApplicationViewModel
import org.mifos.mobile.viewModels.ReviewLoanApplicationViewModelFactory
import javax.inject.Inject

class ReviewLoanApplicationFragment : BaseFragment() {

    companion object {
        const val LOANS_PAYLOAD = "loans_payload"
        const val LOAN_NAME = "loan_name"
        const val ACCOUNT_NO = "account_no"
        const val LOAN_STATE = "loan_state"
        const val LOAN_ID = "loan_id"

        fun newInstance(loanState: LoanState, loansPayload: LoansPayload, loanName: String, accountNo: String)
                : ReviewLoanApplicationFragment {
            val fragment = ReviewLoanApplicationFragment()
            val args = Bundle().apply {
                putSerializable(LOAN_STATE, loanState)
                putParcelable(LOANS_PAYLOAD, loansPayload)
                putString(LOAN_NAME, loanName)
                putString(ACCOUNT_NO, accountNo)
            }
            fragment.arguments = args
            return fragment
        }

        fun newInstance(loanState: LoanState, loansPayload: LoansPayload, loanId: Long, loanName: String, accountNo: String)
                : ReviewLoanApplicationFragment {
            val fragment = ReviewLoanApplicationFragment()
            val args = Bundle().apply {
                putSerializable(LOAN_STATE, loanState)
                putLong(LOAN_ID, loanId)
                putParcelable(LOANS_PAYLOAD, loansPayload)
                putString(LOAN_NAME, loanName)
                putString(ACCOUNT_NO, accountNo)
            }
            fragment.arguments = args
            return fragment
        }
    }

    @Inject
    lateinit var viewModelFactory: ReviewLoanApplicationViewModelFactory

    lateinit var rootView: View

    private lateinit var viewModel: ReviewLoanApplicationViewModel


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.fragment_review_loan_application, container, false)
        (activity as BaseActivity).activityComponent.inject(this)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(ReviewLoanApplicationViewModel::class.java)
        val loanState = arguments?.getSerializable(LOAN_STATE) as LoanState
        if (loanState == LoanState.CREATE) {
            viewModel.insertData(
                    loanState,
                    arguments?.getParcelable(LOANS_PAYLOAD)!!,
                    arguments?.getString(LOAN_NAME)!!,
                    arguments?.getString(ACCOUNT_NO)!!)
        } else {
            viewModel.insertData(
                    loanState,
                    arguments?.getLong(LOAN_ID)!!,
                    arguments?.getParcelable(LOANS_PAYLOAD)!!,
                    arguments?.getString(LOAN_NAME)!!,
                    arguments?.getString(ACCOUNT_NO)!!)
        }
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tv_loan_product.text = viewModel.getLoanProduct()
        tv_loan_purpose.text = viewModel.getLoanPurpose()
        tv_principal_amount.text = viewModel.getPrincipal().toString()
        tv_expected_disbursement_date.text = viewModel.getDisbursementDate()
        tv_submission_date.text = viewModel.getSubmissionDate()
        tv_currency.text = viewModel.getCurrency()
        tv_new_loan_application.text = viewModel.getLoanName()
        tv_account_number.text = viewModel.getAccountNo()

        btn_loan_submit.setOnClickListener {
            showProgress()
            viewModel.submitLoan()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribeWith(object : DisposableObserver<ResponseBody>() {
                        override fun onComplete() {
                        }

                        override fun onNext(t: ResponseBody) {
                            hideProgress()
                            if (viewModel.getLoanState() == LoanState.CREATE)
                                showLoanAccountCreatedSuccessfully()
                            else
                                showLoanAccountUpdatedSuccessfully()
                        }

                        override fun onError(e: Throwable) {
                            hideProgress()
                            showError(MFErrorParser.errorMessage(e))
                        }
                    })
        }
    }

    fun showLoanAccountUpdatedSuccessfully() {
        Toaster.show(rootView, R.string.loan_application_updated_successfully)
        activity!!.supportFragmentManager.popBackStack()
    }

    fun showError(message: String) = if (!Network.isConnected(activity)) {
        iv_status.setImageResource(R.drawable.ic_error_black_24dp)
        tv_status.text = getString(R.string.internet_not_connected)
        ll_add_loan.visibility = View.GONE
        ll_error.visibility = View.VISIBLE
    } else {
        Toaster.show(rootView, message)
    }

    fun showProgress() {
        ll_add_loan.visibility = View.GONE
        showProgressBar()
    }

    fun hideProgress() {
        ll_add_loan.visibility = View.VISIBLE
        hideProgressBar()
    }

    fun showLoanAccountCreatedSuccessfully() {
        Toaster.show(rootView, R.string.loan_application_submitted_successfully)
        activity!!.supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        (activity as BaseActivity?)!!.replaceFragment(ClientAccountsFragment.newInstance(AccountType.LOAN), false, R.id.container)
    }
}
