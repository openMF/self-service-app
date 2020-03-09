package org.mifos.mobile.ui.fragments;
/*
~This project is licensed under the open source MPL V2.
~See https://github.com/openMF/self-service-app/blob/master/LICENSE.md
*/

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.therajanmaurya.sweeterror.SweetUIErrorHandler;

import org.mifos.mobile.R;
import org.mifos.mobile.api.local.PreferencesHelper;
import org.mifos.mobile.models.accounts.loan.LoanWithAssociations;
import org.mifos.mobile.models.accounts.loan.Periods;
import org.mifos.mobile.presenters.LoanAccountsDetailPresenter;
import org.mifos.mobile.ui.activities.base.BaseActivity;
import org.mifos.mobile.ui.enums.AccountType;
import org.mifos.mobile.ui.enums.ChargeType;
import org.mifos.mobile.ui.enums.LoanState;
import org.mifos.mobile.ui.fragments.base.BaseFragment;
import org.mifos.mobile.ui.views.LoanAccountsDetailView;
import org.mifos.mobile.utils.Constants;
import org.mifos.mobile.utils.CurrencyUtil;
import org.mifos.mobile.utils.DateHelper;
import org.mifos.mobile.utils.Network;
import org.mifos.mobile.utils.QrCodeGenerator;

import java.util.List;

import javax.inject.Inject;

import androidx.annotation.Nullable;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * @author Vishwajeet
 * @since 19/08/16
 */

public class LoanAccountsDetailFragment extends BaseFragment implements LoanAccountsDetailView {

    @Inject
    LoanAccountsDetailPresenter loanAccountDetailsPresenter;

    @BindView(R.id.tv_outstanding_balance)
    TextView tvOutstandingBalanceName;

    @BindView(R.id.tv_next_installment)
    TextView tvNextInstallmentName;

    @BindView(R.id.tv_due_date)
    TextView tvDueDateName;

    @BindView(R.id.tv_account_number)
    TextView tvAccountNumberName;

    @BindView(R.id.tv_loan_type)
    TextView tvLoanTypeName;

    @BindView(R.id.tv_currency)
    TextView tvCurrencyName;

    @BindView(R.id.ll_account_detail)
    LinearLayout llAccountDetail;

    @BindView(R.id.layout_error)
    View layoutError;

    @BindView(R.id.btn_make_payment)
    Button btMakePayment;

    @Inject
    PreferencesHelper preferencesHelper;


    private LoanWithAssociations loanWithAssociations;
    private boolean showLoanUpdateOption = false;
    private long loanId;
    private SweetUIErrorHandler sweetUIErrorHandler;

    View rootView;

    public static LoanAccountsDetailFragment newInstance(long loanId) {
        LoanAccountsDetailFragment fragment = new LoanAccountsDetailFragment();
        Bundle args = new Bundle();
        args.putLong(Constants.LOAN_ID, loanId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            loanId = getArguments().getLong(Constants.LOAN_ID);
        }
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        ((BaseActivity) getActivity()).getActivityComponent().inject(this);

        rootView = inflater.inflate(R.layout.fragment_loan_account_details, container, false);
        setToolbarTitle(getString(R.string.loan_account_details));

        ButterKnife.bind(this, rootView);
        loanAccountDetailsPresenter.attachView(this);

        sweetUIErrorHandler = new SweetUIErrorHandler(getActivity(), rootView);

        if (savedInstanceState == null && this.loanWithAssociations == null) {
            loanAccountDetailsPresenter.loadLoanAccountDetails(loanId);
        } else {
            showLoanAccountsDetail(this.loanWithAssociations);
        }

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(Constants.LOAN_ACCOUNT, loanWithAssociations);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            showLoanAccountsDetail((LoanWithAssociations) savedInstanceState.
                    getParcelable(Constants.LOAN_ACCOUNT));
        }
    }


    /**
     * Shows details about loan account fetched from server is status is Active else shows and
     * error layout i.e. {@code layoutError} with a msg related to the status.
     *
     * @param loanWithAssociations object containing details of each loan account,
     */
    @Override
    public void showLoanAccountsDetail(LoanWithAssociations loanWithAssociations) {
        llAccountDetail.setVisibility(View.VISIBLE);
        this.loanWithAssociations = loanWithAssociations;
        if (loanWithAssociations.getStatus().getActive()) {
            List<Integer> overdueSinceDate =
                    loanWithAssociations.getSummary().getOverdueSinceDate();
            if (overdueSinceDate == null) {
                tvDueDateName.setText(R.string.not_available);
            } else {
                tvDueDateName.setText(DateHelper.getDateAsString(overdueSinceDate));
            }
            showDetails(loanWithAssociations);
        } else if (loanWithAssociations.getStatus().getPendingApproval()) {
            sweetUIErrorHandler.showSweetCustomErrorUI(getString(R.string.approval_pending),
                    R.drawable.ic_assignment_turned_in_black_24dp, llAccountDetail, layoutError);
            showLoanUpdateOption = true;
        } else if (loanWithAssociations.getStatus().getWaitingForDisbursal()) {
            sweetUIErrorHandler.showSweetCustomErrorUI(getString(R.string.waiting_for_disburse),
                    R.drawable.ic_assignment_turned_in_black_24dp, llAccountDetail, layoutError);
        } else {
            btMakePayment.setVisibility(View.GONE);
            tvDueDateName.setText(R.string.not_available);
            showDetails(loanWithAssociations);
        }

        getActivity().invalidateOptionsMenu();
    }

    /**
     * Sets basic information about a loan
     *
     * @param loanWithAssociations object containing details of each loan account,
     */
    public void showDetails(LoanWithAssociations loanWithAssociations) {

        tvOutstandingBalanceName.setText(getResources().getString(R.string.string_and_string,
                loanWithAssociations.getSummary().getCurrency().getDisplaySymbol(), CurrencyUtil.
                        formatCurrency(getActivity(),
                                loanWithAssociations.getSummary().getTotalOutstanding())));
        for (Periods thisPeriod : loanWithAssociations.getRepaymentSchedule().getPeriods()) {
            if (thisPeriod.getDueDate().equals(loanWithAssociations.
                    getSummary().getOverdueSinceDate())) {
                tvNextInstallmentName.setText(getResources().getString(R.string.string_and_string,
                        loanWithAssociations.getSummary().getCurrency().getDisplaySymbol(),
                        CurrencyUtil.formatCurrency(getActivity(),
                                thisPeriod.getTotalDueForPeriod())));
                break;
            } else if (loanWithAssociations.getSummary().getOverdueSinceDate() == null) {
                tvNextInstallmentName.setText(R.string.not_available);
            }
        }
        tvAccountNumberName.setText(loanWithAssociations.getAccountNo());
        tvLoanTypeName.setText(loanWithAssociations.getLoanType().getValue());
        tvCurrencyName.setText(loanWithAssociations.getSummary().getCurrency().getCode());
    }

    /**
     * Opens {@link SavingsMakeTransferFragment} to Make a payment for loan account with given
     * {@code loanId}
     */
    @OnClick(R.id.btn_make_payment)
    public void onMakePaymentClicked() {
        ((BaseActivity) getActivity()).replaceFragment(SavingsMakeTransferFragment
                .newInstance(loanId, loanWithAssociations.getSummary().getTotalOutstanding(),
                        Constants.TRANSFER_PAY_TO), true, R.id.container);
    }

    /**
     * Opens {@link LoanAccountSummaryFragment}
     */
    @OnClick(R.id.ll_summary)
    public void onLoanSummaryClicked() {
        ((BaseActivity) getActivity()).replaceFragment(LoanAccountSummaryFragment
                .newInstance(loanWithAssociations), true, R.id.container);
    }

    /**
     * Opens {@link LoanRepaymentScheduleFragment}
     */
    @OnClick(R.id.ll_repayment)
    public void onRepaymentScheduleClicked() {
        ((BaseActivity) getActivity()).replaceFragment(LoanRepaymentScheduleFragment
                .newInstance(loanId), true, R.id.container);
    }

    /**
     * Opens {@link LoanAccountTransactionFragment}
     */
    @OnClick(R.id.ll_loan_transactions)
    public void onTransactionsClicked() {
        ((BaseActivity) getActivity()).replaceFragment(LoanAccountTransactionFragment
                .newInstance(loanId), true, R.id.container);
    }

    @OnClick(R.id.ll_loan_charges)
    public void chargesClicked() {
        ((BaseActivity) getActivity()).replaceFragment(ClientChargeFragment
                .newInstance(loanWithAssociations.getId(), ChargeType.LOAN), true, R.id.container);
    }

    @OnClick(R.id.ll_loan_qr_code)
    public void onQrCodeClicked() {
        String accountDetailsInJson = QrCodeGenerator.
                getAccountDetailsInString(loanWithAssociations.getAccountNo(),
                        preferencesHelper.getOfficeName(), AccountType.LOAN);
        ((BaseActivity) getActivity()).replaceFragment(QrCodeDisplayFragment.
                newInstance(accountDetailsInJson), true, R.id.container);
    }

    /**
     * It is called whenever any error occurs while executing a request
     *
     * @param message Error message that tells the user about the problem.
     */
    @Override
    public void showErrorFetchingLoanAccountsDetail(String message) {
        if (!Network.isConnected(getActivity())) {
            sweetUIErrorHandler.showSweetNoInternetUI(llAccountDetail, layoutError);
        } else {
            sweetUIErrorHandler.showSweetErrorUI(message,
                    llAccountDetail, layoutError);
            Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
        }
    }

    @OnClick(R.id.btn_try_again)
    public void retryClicked() {
        if (Network.isConnected(getContext())) {
            sweetUIErrorHandler.hideSweetErrorLayoutUI(llAccountDetail, layoutError);
            loanAccountDetailsPresenter.loadLoanAccountDetails(loanId);
        } else {
            Toast.makeText(getContext(), getString(R.string.internet_not_connected),
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void showProgress() {
        showProgressBar();
    }

    @Override
    public void hideProgress() {
        hideProgressBar();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        hideProgressBar();
        loanAccountDetailsPresenter.detachView();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_loan_details, menu);
        if (showLoanUpdateOption) {
            menu.findItem(R.id.menu_update_loan).setVisible(true);
            menu.findItem(R.id.menu_withdraw_loan).setVisible(true);
            menu.findItem(R.id.menu_view_guarantor).setVisible(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_update_loan) {
            ((BaseActivity) getActivity()).replaceFragment(LoanApplicationFragment
                    .newInstance(LoanState.UPDATE, loanWithAssociations), true, R.id.container);
            return true;
        } else if (id == R.id.menu_withdraw_loan) {
            ((BaseActivity) getActivity()).replaceFragment(LoanAccountWithdrawFragment
                    .newInstance(loanWithAssociations), true, R.id.container);
        } else if (id == R.id.menu_view_guarantor) {
            ((BaseActivity) getActivity()).replaceFragment(GuarantorListFragment
                    .newInstance(loanId), true, R.id.container);
        }
        return super.onOptionsItemSelected(item);
    }
}
