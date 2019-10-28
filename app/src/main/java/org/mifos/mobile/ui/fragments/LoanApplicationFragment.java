package org.mifos.mobile.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.textfield.TextInputLayout;

import org.mifos.mobile.R;
import org.mifos.mobile.models.accounts.loan.LoanAccount;
import org.mifos.mobile.models.accounts.loan.LoanWithAssociations;
import org.mifos.mobile.models.payload.LoansPayload;
import org.mifos.mobile.models.templates.loans.LoanPurposeOptions;
import org.mifos.mobile.models.templates.loans.LoanTemplate;
import org.mifos.mobile.models.templates.loans.ProductOptions;
import org.mifos.mobile.presenters.LoanApplicationPresenter;
import org.mifos.mobile.ui.activities.base.BaseActivity;
import org.mifos.mobile.ui.enums.LoanState;
import org.mifos.mobile.ui.fragments.base.BaseFragment;
import org.mifos.mobile.ui.views.LoanApplicationMvpView;
import org.mifos.mobile.utils.Constants;
import org.mifos.mobile.utils.DateHelper;
import org.mifos.mobile.utils.MFDatePicker;
import org.mifos.mobile.utils.Network;
import org.mifos.mobile.utils.Toaster;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Rajan Maurya on 06/03/17.
 */
public class LoanApplicationFragment extends BaseFragment implements LoanApplicationMvpView,
        MFDatePicker.OnDatePickListener, AdapterView.OnItemSelectedListener {

    @BindView(R.id.tv_new_loan_application)
    TextView tvNewLoanApplication;

    @BindView(R.id.tv_account_number)
    TextView tvAccountNumber;

    @BindView(R.id.tv_submission_date)
    TextView tvSubmissionDate;

    @BindView(R.id.sp_loan_products)
    Spinner spLoanProducts;

    @BindView(R.id.sp_loan_purpose)
    Spinner spLoanPurpose;

    @BindView(R.id.til_principal_amount)
    TextInputLayout tilPrincipalAmount;

    @BindView(R.id.tv_currency)
    TextView tvCurrency;

    @BindView(R.id.tv_expected_disbursement_date)
    TextView tvExpectedDisbursementDate;

    @BindView(R.id.ll_add_loan)
    LinearLayout llAddLoan;

    @BindView(R.id.ll_error)
    RelativeLayout llError;

    @BindView(R.id.tv_status)
    TextView tvErrorStatus;

    @BindView(R.id.iv_status)
    ImageView ivReload;

    @Inject
    LoanApplicationPresenter loanApplicationPresenter;

    View rootView;

    private List<String> listLoanProducts = new ArrayList<>();
    private List<String> listLoanPurpose = new ArrayList<>();

    private ArrayAdapter<String> loanProductAdapter;
    private ArrayAdapter<String> loanPurposeAdapter;

    private LoanTemplate loanTemplate;
    private DialogFragment mfDatePicker;
    private LoanState loanState;
    private LoanWithAssociations loanWithAssociations;
    private int productId;
    private int purposeId = -1;
    private String disbursementDate;
    private String submittedDate;
    private boolean isDisbursementDate = false;
    private boolean isLoanUpdatePurposesInitialization = true;


    /**
     * Used when we want to apply for a Loan
     *
     * @param loanState {@link LoanState} is set to {@code LoanState.CREATE}
     * @return Instance of {@link LoanApplicationFragment}
     */
    public static LoanApplicationFragment newInstance(LoanState loanState) {
        LoanApplicationFragment fragment = new LoanApplicationFragment();
        Bundle args = new Bundle();
        args.putSerializable(Constants.LOAN_STATE, loanState);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Used when we want to update a Loan Application
     *
     * @param loanState            {@link LoanState} is set to {@code LoanState.UPDATE}
     * @param loanWithAssociations {@link LoanAccount} to modify
     * @return Instance of {@link LoanApplicationFragment}
     */
    public static LoanApplicationFragment newInstance(LoanState loanState,
            LoanWithAssociations loanWithAssociations) {
        LoanApplicationFragment fragment = new LoanApplicationFragment();
        Bundle args = new Bundle();
        args.putSerializable(Constants.LOAN_STATE, loanState);
        args.putParcelable(Constants.LOAN_ACCOUNT, loanWithAssociations);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((BaseActivity) getActivity()).getActivityComponent().inject(this);
        if (getArguments() != null) {
            loanState = (LoanState) getArguments().getSerializable(Constants.LOAN_STATE);

            if (loanState == LoanState.CREATE) {
                setToolbarTitle(getString(R.string.apply_for_loan));
            } else {
                setToolbarTitle(getString(R.string.update_loan));
                loanWithAssociations = getArguments().getParcelable(Constants.LOAN_ACCOUNT);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_add_loan_application, container, false);

        ButterKnife.bind(this, rootView);
        loanApplicationPresenter.attachView(this);

        showUserInterface();
        if (savedInstanceState == null) {
            loadLoanTemplate();
        }

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(Constants.TEMPLATE, loanTemplate);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            LoanTemplate template = savedInstanceState.getParcelable(Constants.TEMPLATE);
            if (loanState == LoanState.CREATE) {
                showLoanTemplate(template);
            } else {
                showUpdateLoanTemplate(template);
            }
        }
    }

    /**
     * Loads {@link LoanTemplate} according to the {@code loanState}
     */
    private void loadLoanTemplate() {
        if (loanState == LoanState.CREATE) {
            loanApplicationPresenter.loadLoanApplicationTemplate(LoanState.CREATE);
        } else {
            loanApplicationPresenter.loadLoanApplicationTemplate(LoanState.UPDATE);
        }
    }

    /**
     * Calls function which applies for a new Loan Application or updates a Loan Application
     * according to {@code loanState}
     */
    @OnClick(R.id.btn_loan_review)
    void onReviewLoanApplication() {
        if (tilPrincipalAmount.getEditText().getText().toString().equals("")) {
            tilPrincipalAmount.setError(getString(R.string.enter_amount));
            return;
        }

        if (tilPrincipalAmount.getEditText().getText().toString().equals(".")) {
            tilPrincipalAmount.setError(getString(R.string.invalid_amount));
            return;
        }

        if (tilPrincipalAmount.getEditText().getText().toString().matches("^0*")) {
            tilPrincipalAmount.setError(getString(R.string.amount_greater_than_zero));
            return;
        }
        if (loanState == LoanState.CREATE) {
            reviewNewLoanApplication();
        } else {
            submitUpdateLoanApplication();
        }
    }

    /**
     * Submits a New Loan Application to the server
     */
    private void reviewNewLoanApplication() {
        LoansPayload loansPayload = new LoansPayload();
        loansPayload.setClientId(loanTemplate.getClientId());
        loansPayload.setLoanPurpose(spLoanPurpose.getSelectedItem().toString());
        loansPayload.setProductName(spLoanProducts.getSelectedItem().toString());
        loansPayload.setCurrency(tvCurrency.getText().toString());
        if (purposeId > 0)
            loansPayload.setLoanPurposeId(purposeId);
        loansPayload.setProductId(productId);
        loansPayload.setPrincipal(Double.
                parseDouble(tilPrincipalAmount.getEditText().getText().toString()));
        loansPayload.setLoanTermFrequency(loanTemplate.getTermFrequency());
        loansPayload.setLoanTermFrequencyType(loanTemplate.getInterestRateFrequencyType().getId());
        loansPayload.setLoanType("individual");
        loansPayload.setNumberOfRepayments(loanTemplate.getNumberOfRepayments());
        loansPayload.setRepaymentEvery(loanTemplate.getRepaymentEvery());
        loansPayload.setRepaymentFrequencyType(loanTemplate.getInterestRateFrequencyType().getId());
        loansPayload.setInterestRatePerPeriod(loanTemplate.getInterestRatePerPeriod());
        loansPayload.setExpectedDisbursementDate(disbursementDate);
        loansPayload.setSubmittedOnDate(submittedDate);

        loansPayload.setTransactionProcessingStrategyId(
                loanTemplate.getTransactionProcessingStrategyId());
        loansPayload.setAmortizationType(loanTemplate.getAmortizationType().getId());
        loansPayload.setInterestCalculationPeriodType(
                loanTemplate.getInterestCalculationPeriodType().getId());
        loansPayload.setInterestType(loanTemplate.getInterestType().getId());

        ((BaseActivity) getActivity()).replaceFragment(ReviewLoanApplicationFragment
                        .Companion.newInstance(loanState, loansPayload,
                        tvNewLoanApplication.getText().toString(),
                        tvAccountNumber.getText().toString()),
                true, R.id.container);
    }

    /**
     * Requests server to update the Loan Application with new values
     */
    private void submitUpdateLoanApplication() {
        LoansPayload loansPayload = new LoansPayload();
        loansPayload.setPrincipal(Double.
                parseDouble(tilPrincipalAmount.getEditText().getText().toString()));
        loansPayload.setProductId(productId);
        loansPayload.setLoanPurpose(spLoanPurpose.getSelectedItem().toString());
        loansPayload.setProductName(spLoanProducts.getSelectedItem().toString());
        loansPayload.setCurrency(tvCurrency.getText().toString());
        if (purposeId > 0)
            loansPayload.setLoanPurposeId(purposeId);
        loansPayload.setLoanTermFrequency(loanTemplate.getTermFrequency());
        loansPayload.setLoanTermFrequencyType(loanTemplate.getInterestRateFrequencyType().getId());
        loansPayload.setNumberOfRepayments(loanTemplate.getNumberOfRepayments());
        loansPayload.setRepaymentEvery(loanTemplate.getRepaymentEvery());
        loansPayload.setRepaymentFrequencyType(loanTemplate.getInterestRateFrequencyType().getId());
        loansPayload.setInterestRatePerPeriod(loanTemplate.getInterestRatePerPeriod());
        loansPayload.setInterestType(loanTemplate.getInterestType().getId());
        loansPayload.setInterestCalculationPeriodType(
                loanTemplate.getInterestCalculationPeriodType().getId());
        loansPayload.setAmortizationType(loanTemplate.getAmortizationType().getId());
        loansPayload.setTransactionProcessingStrategyId(
                loanTemplate.getTransactionProcessingStrategyId());
        loansPayload.setExpectedDisbursementDate(disbursementDate);

        ((BaseActivity) getActivity()).replaceFragment(ReviewLoanApplicationFragment
                        .Companion.newInstance(loanState,
                        loansPayload,
                        loanWithAssociations.getId(),
                        tvNewLoanApplication.getText().toString(),
                        tvAccountNumber.getText().toString()),
                false, R.id.container);
    }

    /**
     * Retries to fetch {@link LoanTemplate} by calling {@code loadLoanTemplate()}
     */
    @OnClick(R.id.iv_status)
    void onRetry() {
        llError.setVisibility(View.GONE);
        llAddLoan.setVisibility(View.VISIBLE);
        loadLoanTemplate();
    }

    /**
     * Initializes {@code tvSubmissionDate} with current Date
     */
    public void inflateSubmissionDate() {
        tvSubmissionDate.setText(MFDatePicker.getDatePickedAsString());
    }

    /**
     * Initializes {@code tvExpectedDisbursementDate} with current Date
     */
    public void inflateDisbursementDate() {
        mfDatePicker = MFDatePicker.newInstance(this, MFDatePicker.FUTURE_DAYS);
        tvExpectedDisbursementDate.setText(MFDatePicker.getDatePickedAsString());
    }

    /**
     * Sets {@code submittedDate} and {@code disbursementDate} in a specific format
     */
    public void setSubmissionDisburseDate() {
        disbursementDate = tvExpectedDisbursementDate.getText().toString();
        submittedDate = tvSubmissionDate.getText().toString();
        submittedDate = DateHelper.getSpecificFormat(DateHelper.FORMAT_dd_MMMM_yyyy, submittedDate);
        disbursementDate = DateHelper.getSpecificFormat(
                DateHelper.FORMAT_dd_MMMM_yyyy, disbursementDate);
    }

    /**
     * Shows a {@link DialogFragment} for selecting a Date for Disbursement
     */
    @OnClick(R.id.ll_expected_disbursement_date_edit)
    public void setTvDisbursementOnDate() {
        isDisbursementDate = true;
        mfDatePicker.show(getActivity().getSupportFragmentManager(), Constants
                .DFRAG_DATE_PICKER);
    }

    /**
     * A CallBack for {@link MFDatePicker} which provides us with the date selected from the
     * {@link android.app.DatePickerDialog}
     *
     * @param date Date selected by user in {@link String}
     */
    @Override
    public void onDatePicked(String date) {
        if (isDisbursementDate) {
            tvExpectedDisbursementDate.setText(date);
            disbursementDate = date;
            isDisbursementDate = false;
        }
        setSubmissionDisburseDate();
    }


    /**
     * Initializes the layout
     */
    @Override
    public void showUserInterface() {
        loanProductAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item,
                listLoanProducts);
        loanProductAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spLoanProducts.setAdapter(loanProductAdapter);
        spLoanProducts.setOnItemSelectedListener(this);

        loanPurposeAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item,
                listLoanPurpose);
        loanPurposeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spLoanPurpose.setAdapter(loanPurposeAdapter);
        spLoanPurpose.setOnItemSelectedListener(this);

        inflateSubmissionDate();
        inflateDisbursementDate();
        setSubmissionDisburseDate();
    }

    /**
     * Fetches the {@link LoanTemplate} from server for {@code loanState} as CREATE
     *
     * @param loanTemplate Template for Loan Application
     */
    @Override
    public void showLoanTemplate(LoanTemplate loanTemplate) {
        this.loanTemplate = loanTemplate;
        for (ProductOptions productOption : loanTemplate.getProductOptions()) {
            listLoanProducts.add(productOption.getName());
        }
        loanProductAdapter.notifyDataSetChanged();
    }

    /**
     * Fetches the {@link LoanTemplate} from server for {@code loanState} as UPDATE
     *
     * @param loanTemplate Template for Loan Application
     */
    @Override
    public void showUpdateLoanTemplate(LoanTemplate loanTemplate) {
        this.loanTemplate = loanTemplate;

        for (ProductOptions productOption : loanTemplate.getProductOptions()) {
            listLoanProducts.add(productOption.getName());
        }
        loanProductAdapter.notifyDataSetChanged();

        spLoanProducts.setSelection(loanProductAdapter
                .getPosition(loanWithAssociations.getLoanProductName()));
        tvAccountNumber.setText(getString(R.string.string_and_string,
                getString(R.string.account_number) + " ", loanWithAssociations.getAccountNo()));
        tvNewLoanApplication.setText(getString(R.string.string_and_string,
                getString(R.string.update_loan_application) + " ",
                loanWithAssociations.getClientName()));
        tilPrincipalAmount.getEditText().setText(String.format(Locale.getDefault(),
                "%.2f", loanWithAssociations.getPrincipal()));
        tvCurrency.setText(loanWithAssociations.getCurrency().getDisplayLabel());

        tvSubmissionDate.setText(DateHelper.getDateAsString(loanWithAssociations.
                getTimeline().getSubmittedOnDate(), "dd-MM-yyyy"));
        tvExpectedDisbursementDate.setText(DateHelper.getDateAsString(loanWithAssociations.
                getTimeline().getExpectedDisbursementDate(), "dd-MM-yyyy"));
        setSubmissionDisburseDate();
    }

    /**
     * Fetches the {@link LoanTemplate} according to product from server for {@code loanState} as
     * CREATE
     *
     * @param loanTemplate Template for Loan Application
     */
    @Override
    public void showLoanTemplateByProduct(LoanTemplate loanTemplate) {
        this.loanTemplate = loanTemplate;
        tvAccountNumber.setText(getString(R.string.string_and_string,
                getString(R.string.account_number) + " ", loanTemplate.getClientAccountNo()));
        tvNewLoanApplication.setText(getString(R.string.string_and_string,
                getString(R.string.new_loan_application) + " ", loanTemplate.getClientName()));
        tilPrincipalAmount.getEditText().setText(String.valueOf(loanTemplate.getPrincipal()));
        tvCurrency.setText(loanTemplate.getCurrency().getDisplayLabel());

        listLoanPurpose.clear();
        listLoanPurpose.add(getActivity().getString(R.string.loan_purpose_not_provided));

        if (loanTemplate.getLoanPurposeOptions() != null) {
            for (LoanPurposeOptions loanPurposeOptions : loanTemplate.getLoanPurposeOptions()) {
                listLoanPurpose.add(loanPurposeOptions.getName());
            }
        }
        loanPurposeAdapter.notifyDataSetChanged();
        spLoanPurpose.setSelection(0);

    }

    /**
     * Fetches the {@link LoanTemplate} according to product from server for {@code loanState} as
     * UPDATE
     *
     * @param loanTemplate Template for Loan Application
     */
    @Override
    public void showUpdateLoanTemplateByProduct(LoanTemplate loanTemplate) {
        this.loanTemplate = loanTemplate;
        listLoanPurpose.clear();
        listLoanPurpose.add(getActivity().getString(R.string.loan_purpose_not_provided));

        if (loanTemplate.getLoanPurposeOptions() != null) {
            for (LoanPurposeOptions loanPurposeOptions : loanTemplate.getLoanPurposeOptions()) {
                listLoanPurpose.add(loanPurposeOptions.getName());
            }
        }
        loanPurposeAdapter.notifyDataSetChanged();
        spLoanPurpose.setSelection(0);

        if (isLoanUpdatePurposesInitialization &&
                loanWithAssociations.getLoanPurposeName() != null) {
            spLoanPurpose.setSelection(loanPurposeAdapter
                    .getPosition(loanWithAssociations.getLoanPurposeName()));
            isLoanUpdatePurposesInitialization = false;
        } else {
            tvAccountNumber.setText(getString(R.string.string_and_string,
                    getString(R.string.account_number) + " ", loanTemplate.getClientAccountNo()));
            tvNewLoanApplication.setText(getString(R.string.string_and_string,
                    getString(R.string.new_loan_application) + " ", loanTemplate.getClientName()));
            tilPrincipalAmount.getEditText().setText(String.valueOf(loanTemplate.getPrincipal()));
            tvCurrency.setText(loanTemplate.getCurrency().getDisplayLabel());
        }
    }

    /**
     * It is called whenever any error occurs while executing a request
     *
     * @param message Error message that tells the user about the problem.
     */
    @Override
    public void showError(String message) {
        if (!Network.isConnected(getActivity())) {
            ivReload.setImageResource(R.drawable.ic_error_black_24dp);
            tvErrorStatus.setText(getString(R.string.internet_not_connected));
            llAddLoan.setVisibility(View.GONE);
            llError.setVisibility(View.VISIBLE);
        } else {
            Toaster.show(rootView, message);
        }
    }

    @Override
    public void showProgress() {
        llAddLoan.setVisibility(View.GONE);
        showProgressBar();
    }

    @Override
    public void hideProgress() {
        llAddLoan.setVisibility(View.VISIBLE);
        hideProgressBar();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId()) {
            case R.id.sp_loan_products:
                productId = loanTemplate.getProductOptions().get(position).getId();

                if (loanState == LoanState.CREATE) {
                    loanApplicationPresenter.loadLoanApplicationTemplateByProduct(productId,
                            LoanState.CREATE);
                } else {
                    loanApplicationPresenter.loadLoanApplicationTemplateByProduct(productId,
                            LoanState.UPDATE);
                }
                break;

            case R.id.sp_loan_purpose:
                if (loanTemplate.getLoanPurposeOptions() != null &&
                        loanTemplate.getLoanPurposeOptions().size() > position) {
                    purposeId = loanTemplate.getLoanPurposeOptions().get(position).getId();
                }
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        hideProgressBar();
        loanApplicationPresenter.detachView();
    }
}
