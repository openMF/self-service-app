package org.mifos.mobile.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.github.therajanmaurya.sweeterror.SweetUIErrorHandler;
import com.google.android.material.snackbar.Snackbar;

import org.mifos.mobile.R;
import org.mifos.mobile.models.payload.AccountDetail;
import org.mifos.mobile.models.payload.TransferPayload;
import org.mifos.mobile.models.templates.account.AccountOption;
import org.mifos.mobile.models.templates.account.AccountOptionsTemplate;
import org.mifos.mobile.presenters.SavingsMakeTransferPresenter;
import org.mifos.mobile.ui.activities.base.BaseActivity;
import org.mifos.mobile.ui.adapters.AccountsSpinnerAdapter;
import org.mifos.mobile.ui.enums.TransferType;
import org.mifos.mobile.ui.fragments.base.BaseFragment;
import org.mifos.mobile.ui.views.SavingsMakeTransferMvpView;
import org.mifos.mobile.utils.Constants;
import org.mifos.mobile.utils.DateHelper;
import org.mifos.mobile.utils.MFDatePicker;
import org.mifos.mobile.utils.Network;
import org.mifos.mobile.utils.ProcessView;
import org.mifos.mobile.utils.Toaster;
import org.mifos.mobile.utils.Utils;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Rajan Maurya on 10/03/17.
 */
public class SavingsMakeTransferFragment extends BaseFragment implements
        SavingsMakeTransferMvpView, AdapterView.OnItemSelectedListener {

    @BindView(R.id.sp_pay_to)
    Spinner spPayTo;

    @BindView(R.id.sp_pay_from)
    Spinner spPayFrom;

    @BindView(R.id.et_amount)
    EditText etAmount;

    @BindView(R.id.process_one)
    ProcessView pvOne;

    @BindView(R.id.process_two)
    ProcessView pvTwo;

    @BindView(R.id.process_three)
    ProcessView pvThree;

    @BindView(R.id.process_four)
    ProcessView pvFour;

    @BindView(R.id.btn_pay_to)
    AppCompatButton btnPayTo;

    @BindView(R.id.btn_pay_from)
    AppCompatButton btnPayFrom;

    @BindView(R.id.btn_amount)
    AppCompatButton btnAmount;

    @BindView(R.id.ll_review)
    LinearLayout llReview;

    @BindView(R.id.tv_select_pay_from)
    TextView tvSelectPayFrom;

    @BindView(R.id.tv_select_amount)
    TextView tvEnterAmount;

    @BindView(R.id.tv_enter_remark)
    TextView tvEnterRemark;

    @BindView(R.id.et_remark)
    EditText etRemark;

    @BindView(R.id.ll_make_transfer)
    LinearLayout layoutMakeTransfer;

    @BindView(R.id.layout_error)
    View layoutError;

    @Inject
    SavingsMakeTransferPresenter savingsMakeTransferPresenter;

    View rootView;

    private List<AccountDetail> listPayTo = new ArrayList<>();
    private List<AccountDetail> listPayFrom = new ArrayList<>();

    private AccountsSpinnerAdapter payToAdapter;
    private AccountsSpinnerAdapter payFromAdapter;

    private TransferPayload transferPayload;
    private String transferDate;
    private AccountOption toAccountOption, fromAccountOption;
    private AccountOptionsTemplate accountOptionsTemplate;
    private String transferType, payTo, payFrom;
    private long accountId;
    private double outStandingBalance;
    private boolean isLoanRepayment;
    private SweetUIErrorHandler sweetUIErrorHandler;

    /**
     * Provides an instance of {@link SavingsMakeTransferFragment}, use {@code transferType} as
     * {@code Constants.TRANSFER_PAY_TO} when we want to deposit and
     * {@code Constants.TRANSFER_PAY_FROM} when we want to make a transfer
     *
     * @param accountId    Saving account Id
     * @param transferType Type of transfer i.e. {@code Constants.TRANSFER_PAY_TO} or
     *                     {@code Constants.TRANSFER_PAY_FROM}
     * @return Instance of {@link SavingsMakeTransferFragment}
     */
    public static SavingsMakeTransferFragment newInstance(long accountId, String transferType) {
        SavingsMakeTransferFragment transferFragment = new SavingsMakeTransferFragment();
        Bundle args = new Bundle();
        args.putLong(Constants.ACCOUNT_ID, accountId);
        args.putString(Constants.TRANSFER_TYPE, transferType);
        transferFragment.setArguments(args);
        return transferFragment;
    }

    public static SavingsMakeTransferFragment newInstance(long accountId, double outstandingBalance,
                                                          String transferType) {
        SavingsMakeTransferFragment transferFragment = new SavingsMakeTransferFragment();
        Bundle args = new Bundle();
        args.putLong(Constants.ACCOUNT_ID, accountId);
        args.putString(Constants.TRANSFER_TYPE, transferType);
        args.putDouble(Constants.OUTSTANDING_BALANCE, outstandingBalance);
        args.putBoolean(Constants.LOAN_REPAYMENT, true);
        transferFragment.setArguments(args);
        return transferFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            accountId = getArguments().getLong(Constants.ACCOUNT_ID);
            transferType = getArguments().getString(Constants.TRANSFER_TYPE);

            if (getArguments().getBoolean(Constants.LOAN_REPAYMENT, false)) {
                isLoanRepayment = true;
                outStandingBalance = getArguments().getDouble(Constants.OUTSTANDING_BALANCE);
            }
        }
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_savings_make_transfer, container, false);
        ((BaseActivity) getActivity()).getActivityComponent().inject(this);
        setToolbarTitle(getString(R.string.transfer));
        ButterKnife.bind(this, rootView);
        savingsMakeTransferPresenter.attachView(this);
        sweetUIErrorHandler = new SweetUIErrorHandler(getActivity(), rootView);
        showUserInterface();
        if (savedInstanceState == null) {
            savingsMakeTransferPresenter.loanAccountTransferTemplate();
        }
        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(Constants.TEMPLATE, accountOptionsTemplate);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            showSavingsAccountTemplate((AccountOptionsTemplate) savedInstanceState.
                    getParcelable(Constants.TEMPLATE));
        }
    }

    /**
     * Checks validation of {@code etRemark} and then opens {@link TransferProcessFragment} for
     * initiating the transfer
     */
    @OnClick(R.id.btn_review_transfer)
    void reviewTransfer() {

        if (etRemark.getText().toString().trim().equals("")) {
            showToaster(getString(R.string.remark_is_mandatory));
            return;
        }

        transferPayload = new TransferPayload();
        transferPayload.setFromAccountId(fromAccountOption.getAccountId());
        transferPayload.setFromClientId(fromAccountOption.getClientId());
        transferPayload.setFromAccountType(fromAccountOption.getAccountType().getId());
        transferPayload.setFromOfficeId(fromAccountOption.getOfficeId());
        transferPayload.setToOfficeId(toAccountOption.getOfficeId());
        transferPayload.setToAccountId(toAccountOption.getAccountId());
        transferPayload.setToClientId(toAccountOption.getClientId());
        transferPayload.setToAccountType(toAccountOption.getAccountType().getId());
        transferPayload.setTransferDate(transferDate);
        transferPayload.setTransferAmount(Double.parseDouble(etAmount.getText().toString()));
        transferPayload.setTransferDescription(etRemark.getText().toString());
        transferPayload.setFromAccountNumber(fromAccountOption.getAccountNo());
        transferPayload.setToAccountNumber(toAccountOption.getAccountNo());

        ((BaseActivity) getActivity()).replaceFragment(TransferProcessFragment.
                newInstance(transferPayload, TransferType.SELF), true, R.id.container);
    }

    /**
     * Cancels the transfer by poping current Fragment
     */
    @OnClick(R.id.btn_cancel_transfer)
    void cancelTransfer() {
        getActivity().getSupportFragmentManager().popBackStack();
    }

    @OnClick(R.id.btn_try_again)
    void onRetry() {
        if (Network.isConnected(getContext())) {
            sweetUIErrorHandler.hideSweetErrorLayoutUI(layoutMakeTransfer, layoutError);
            savingsMakeTransferPresenter.loanAccountTransferTemplate();
        } else {
            Toaster.show(rootView, getString(R.string.internet_not_connected));
        }
    }

    /**
     * Setting up basic components
     */
    @Override
    public void showUserInterface() {
        pvOne.setCurrentActive();
        payFromAdapter = new AccountsSpinnerAdapter(getActivity(), R.layout.account_spinner_layout,
                listPayFrom);
        payFromAdapter.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
        spPayFrom.setAdapter(payFromAdapter);
        spPayFrom.setOnItemSelectedListener(this);

        payToAdapter = new AccountsSpinnerAdapter(getActivity(), R.layout.account_spinner_layout,
                listPayTo);
        payToAdapter.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
        spPayTo.setAdapter(payToAdapter);
        spPayTo.setOnItemSelectedListener(this);
        transferDate = DateHelper.getSpecificFormat(DateHelper.FORMAT_dd_MMMM_yyyy,
                MFDatePicker.getDatePickedAsString());

        if (isLoanRepayment) {
            etAmount.setText(String.valueOf(outStandingBalance));
            etAmount.setFocusable(false);
        }

    }

    /**
     * Provides with {@code accountOptionsTemplate} fetched from server which is used to update
     * {@code listPayFrom} and {@code listPayTo}
     *
     * @param accountOptionsTemplate Template for account transfer
     */
    @Override
    public void showSavingsAccountTemplate(AccountOptionsTemplate accountOptionsTemplate) {
        this.accountOptionsTemplate = accountOptionsTemplate;
        listPayFrom.clear();
        listPayFrom.addAll(savingsMakeTransferPresenter.getAccountNumbers(
                accountOptionsTemplate.getFromAccountOptions(), true));
        listPayTo.clear();
        listPayTo.addAll(savingsMakeTransferPresenter.getAccountNumbers(
                accountOptionsTemplate.getToAccountOptions(), false));
        payToAdapter.notifyDataSetChanged();
        payFromAdapter.notifyDataSetChanged();
    }

    /**
     * Shows a {@link Snackbar} with {@code message}
     *
     * @param message String to be shown
     */
    @Override
    public void showToaster(String message) {
        Toaster.show(rootView, message);
    }

    /**
     * It is called whenever any error occurs while executing a request
     *
     * @param message Error message that tells the user about the problem.
     */
    @Override
    public void showError(String message) {
        if (!Network.isConnected(getContext())) {
            sweetUIErrorHandler.showSweetNoInternetUI(layoutMakeTransfer, layoutError);
        } else {
            sweetUIErrorHandler.showSweetErrorUI(message, layoutMakeTransfer, layoutError);
            Toaster.show(rootView, message);
        }
    }

    @Override
    public void showProgressDialog() {
        showMifosProgressDialog(getString(R.string.making_transfer));
    }

    @Override
    public void hideProgressDialog() {
        hideMifosProgressDialog();
    }

    @Override
    public void showProgress() {
        layoutMakeTransfer.setVisibility(View.GONE);
        showProgressBar();
    }

    @Override
    public void hideProgress() {
        layoutMakeTransfer.setVisibility(View.VISIBLE);
        hideProgressBar();
    }

    /**
     * Callback for {@code spPayFrom} and {@code spPayTo}
     */
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId()) {
            case R.id.sp_pay_to:
                toAccountOption = accountOptionsTemplate.getToAccountOptions().get(position);
                payTo = toAccountOption.getAccountNo();
                break;
            case R.id.sp_pay_from:
                fromAccountOption = accountOptionsTemplate.getFromAccountOptions().get(position);
                payFrom = fromAccountOption.getAccountNo();
                break;
        }

        switch (transferType) {
            case Constants.TRANSFER_PAY_TO:
                setToolbarTitle(getString(R.string.deposit));
                toAccountOption = savingsMakeTransferPresenter
                        .searchAccount(accountOptionsTemplate.getToAccountOptions(), accountId);
                spPayTo.setSelection(accountOptionsTemplate.getToAccountOptions()
                        .indexOf(toAccountOption));
                spPayTo.setEnabled(false);
                pvOne.setCurrentCompeleted();
                break;
            case Constants.TRANSFER_PAY_FROM:
                setToolbarTitle(getString(R.string.transfer));
                fromAccountOption = savingsMakeTransferPresenter
                        .searchAccount(accountOptionsTemplate.getFromAccountOptions(), accountId);
                spPayFrom.setSelection(accountOptionsTemplate.getFromAccountOptions()
                        .indexOf(fromAccountOption));
                spPayFrom.setEnabled(false);
                spPayFrom.setVisibility(View.VISIBLE);
                tvSelectPayFrom.setVisibility(View.GONE);
                pvTwo.setCurrentCompeleted();
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    /**
     * Disables {@code spPayTo} {@link Spinner} and sets {@code pvOne} to completed and make
     * {@code pvTwo} active
     */
    @OnClick(R.id.btn_pay_to)
    public void payToSelected() {
        String payToIdString = payTo.replaceFirst("^0+(?!$)", "");
        int payToId = Integer.parseInt(payToIdString);
        if (payToId == Math.round(accountId)) {
            showToaster(getString(R.string.error_same_account_transfer));
            return;
        }
        pvOne.setCurrentCompeleted();
        pvTwo.setCurrentActive();

        btnPayTo.setVisibility(View.GONE);
        tvSelectPayFrom.setVisibility(View.GONE);
        btnPayFrom.setVisibility(View.VISIBLE);
        spPayFrom.setVisibility(View.VISIBLE);
        spPayTo.setEnabled(false);
    }

    /**
     * Checks validation of {@code spPayTo} {@link Spinner}.<br>
     * Disables {@code spPayFrom} {@link Spinner} and sets {@code pvTwo} to completed and make
     * {@code pvThree} active
     */
    @OnClick(R.id.btn_pay_from)
    public void payFromSelected() {
        if (payTo.equals(payFrom)) {
            showToaster(getString(R.string.error_same_account_transfer));
            return;
        }
        pvTwo.setCurrentCompeleted();
        pvThree.setCurrentActive();

        btnPayFrom.setVisibility(View.GONE);
        tvEnterAmount.setVisibility(View.GONE);
        etAmount.setVisibility(View.VISIBLE);
        btnAmount.setVisibility(View.VISIBLE);
        spPayFrom.setEnabled(false);
    }

    /**
     * Checks validation of {@code etAmount} {@link EditText}.<br>
     * Disables {@code etAmount} and sets {@code pvThree} to completed and make
     * {@code pvFour} active
     */
    @OnClick(R.id.btn_amount)
    public void amountSet() {

        if (etAmount.getText().toString().equals("")) {
            showToaster(getString(R.string.enter_amount));
            return;
        }

        if (etAmount.getText().toString().equals(".")) {
            showToaster(getString(R.string.invalid_amount));
            return;
        }

        if (Double.parseDouble(etAmount.getText().toString()) == 0) {
            showToaster(getString(R.string.amount_greater_than_zero));
            return;
        }

        pvThree.setCurrentCompeleted();
        pvFour.setCurrentActive();

        btnAmount.setVisibility(View.GONE);
        tvEnterRemark.setVisibility(View.GONE);
        etRemark.setVisibility(View.VISIBLE);
        llReview.setVisibility(View.VISIBLE);
        etAmount.setEnabled(false);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_transfer, menu);
        Utils.setToolbarIconColor(getActivity(), menu, R.color.white);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_refresh_transfer) {
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            Fragment currFragment = getActivity().getSupportFragmentManager()
                    .findFragmentById(R.id.container);
            transaction.detach(currFragment);
            transaction.attach(currFragment);
            transaction.commit();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        hideProgress();
        hideMifosProgressDialog();
        savingsMakeTransferPresenter.detachView();
    }
}

