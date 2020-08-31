package org.mifos.mobile.ui.fragments;

import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.evrencoskun.tableview.TableView;
import com.github.therajanmaurya.sweeterror.SweetUIErrorHandler;

import org.mifos.mobile.R;
import org.mifos.mobile.models.accounts.loan.LoanWithAssociations;
import org.mifos.mobile.models.accounts.loan.Periods;
import org.mifos.mobile.models.accounts.loan.tableview.Cell;
import org.mifos.mobile.models.accounts.loan.tableview.ColumnHeader;
import org.mifos.mobile.models.accounts.loan.tableview.RowHeader;
import org.mifos.mobile.presenters.LoanRepaymentSchedulePresenter;
import org.mifos.mobile.ui.activities.base.BaseActivity;
import org.mifos.mobile.ui.adapters.LoanRepaymentScheduleAdapter;
import org.mifos.mobile.ui.fragments.base.BaseFragment;
import org.mifos.mobile.ui.views.LoanRepaymentScheduleMvpView;
import org.mifos.mobile.utils.Constants;
import org.mifos.mobile.utils.DateHelper;
import org.mifos.mobile.utils.Network;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Rajan Maurya on 03/03/17.
 */
@SuppressWarnings("SuspiciousNameCombination")
public class LoanRepaymentScheduleFragment extends BaseFragment implements
        LoanRepaymentScheduleMvpView {

    @BindView(R.id.tv_repayment_schedule)
    TableView tvRepaymentSchedule;


    @BindView(R.id.tv_account_number)
    TextView tvAccountNumber;

    @BindView(R.id.tv_disbursement_date)
    TextView tvDisbursementDate;

    @BindView(R.id.tv_number_of_payments)
    TextView tvNumberOfPayments;

    @BindView(R.id.layout_error)
    View layoutError;

    @Inject
    LoanRepaymentSchedulePresenter loanRepaymentSchedulePresenter;

    @Inject
    LoanRepaymentScheduleAdapter loanRepaymentScheduleAdapter;

    SweetUIErrorHandler sweetUIErrorHandler;

    View rootView;
    private long loanId;
    private LoanWithAssociations loanWithAssociations;

    public static LoanRepaymentScheduleFragment newInstance(long loanId) {
        LoanRepaymentScheduleFragment loanRepaymentScheduleFragment =
                new LoanRepaymentScheduleFragment();
        Bundle args = new Bundle();
        args.putLong(Constants.LOAN_ID, loanId);
        loanRepaymentScheduleFragment.setArguments(args);
        return loanRepaymentScheduleFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((BaseActivity) getActivity()).getActivityComponent().inject(this);
        setToolbarTitle(getString(R.string.loan_repayment_schedule));
        if (getArguments() != null) {
            loanId = getArguments().getLong(Constants.LOAN_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_loan_repayment_schedule, container, false);
        ButterKnife.bind(this, rootView);
        loanRepaymentSchedulePresenter.attachView(this);

        sweetUIErrorHandler = new SweetUIErrorHandler(getContext(), rootView);

        showUserInterface();
        if (savedInstanceState == null) {
            loanRepaymentSchedulePresenter.loanLoanWithAssociations(loanId);
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
            showLoanRepaymentSchedule((LoanWithAssociations) savedInstanceState.
                    getParcelable(Constants.LOAN_ACCOUNT));
        }
    }

    /**
     * Initializes the layout
     */
    @Override
    public void showUserInterface() {
        double columnWidth;
        tvRepaymentSchedule.setHasFixedWidth(true);
        int orientation = getActivity().getResources().getConfiguration().orientation;
        tvRepaymentSchedule.getLayoutParams().width =
                Resources.getSystem().getDisplayMetrics().widthPixels;
        tvRepaymentSchedule.setAdapter(loanRepaymentScheduleAdapter);
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            columnWidth = 2 * (Resources.getSystem().getDisplayMetrics().widthPixels / 7.2);
        } else {
            columnWidth = 2 * (Resources.getSystem().getDisplayMetrics().widthPixels / 6.6);
        }
        loanRepaymentScheduleAdapter.setColumnWidth(columnWidth);
    }

    @Override
    public void showProgress() {
        showProgressBar();
    }

    @Override
    public void hideProgress() {
        hideProgressBar();
    }

    /**
     * Fetches {@link LoanWithAssociations} for a loan with {@code loanId}
     *
     * @param loanWithAssociations Contains details about Repayment Schedule
     */
    @Override
    public void showLoanRepaymentSchedule(LoanWithAssociations loanWithAssociations) {
        this.loanWithAssociations = loanWithAssociations;
        String currencyRepresentation = loanWithAssociations.getCurrency().getDisplaySymbol();
        if (currencyRepresentation == null) {
            currencyRepresentation = loanWithAssociations.getCurrency().getCode();
        }
        loanRepaymentScheduleAdapter
                .setCurrency(currencyRepresentation);
        setTableViewList(loanWithAssociations.getRepaymentSchedule().getPeriods());
        tvAccountNumber.setText(loanWithAssociations.getAccountNo());
        tvDisbursementDate.setText(DateHelper.getDateAsString(loanWithAssociations.
                getTimeline().getExpectedDisbursementDate()));
        tvNumberOfPayments.setText(String.
                valueOf(loanWithAssociations.getNumberOfRepayments()));
    }

    private void setTableViewList(List<Periods> periods) {
        List<ColumnHeader> mColumnHeaderList = new ArrayList<>();
        List<RowHeader> mRowHeaders = new ArrayList<>();
        List<List<Cell>> mCellList = new ArrayList<>();
        mColumnHeaderList.add(new ColumnHeader(getString(R.string.date)));
        mColumnHeaderList.add(new ColumnHeader(getString(R.string.loan_balance)));
        mColumnHeaderList.add(new ColumnHeader(getString(R.string.repayment)));
        int i = 0;
        for (Periods period : periods) {
            List<Cell> cells = new ArrayList<>();
            cells.add(new Cell(period));
            cells.add(new Cell(period));
            cells.add(new Cell(period));
            mCellList.add(cells);
            mRowHeaders.add(new RowHeader(++i));
        }
        loanRepaymentScheduleAdapter.setAllItems(mColumnHeaderList, mRowHeaders, mCellList);
    }

    /**
     * Shows an empty layout for a loan with {@code loanId} which has no Repayment Schedule
     *
     * @param loanWithAssociations Contains details about Repayment Schedule
     */
    @Override
    public void showEmptyRepaymentsSchedule(LoanWithAssociations loanWithAssociations) {
        tvAccountNumber.setText(loanWithAssociations.getAccountNo());
        tvDisbursementDate.setText(DateHelper.getDateAsString(loanWithAssociations.
                getTimeline().getExpectedDisbursementDate()));
        tvNumberOfPayments.setText(String.
                valueOf(loanWithAssociations.getNumberOfRepayments()));
        sweetUIErrorHandler.showSweetEmptyUI(getString(R.string.repayment_schedule),
                R.drawable.ic_charges, tvRepaymentSchedule, layoutError);
    }

    /**
     * It is called whenever any error occurs while executing a request
     *
     * @param message Error message that tells the user about the problem.
     */
    @Override
    public void showError(String message) {
        if (!Network.isConnected(getActivity())) {
            sweetUIErrorHandler.showSweetNoInternetUI(tvRepaymentSchedule, layoutError);
        } else {
            sweetUIErrorHandler.showSweetErrorUI(message,
                    tvRepaymentSchedule, layoutError);
            Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
        }
    }

    @OnClick(R.id.btn_try_again)
    public void retryClicked() {
        if (Network.isConnected(getContext())) {
            sweetUIErrorHandler.hideSweetErrorLayoutUI(tvRepaymentSchedule, layoutError);
            loanRepaymentSchedulePresenter.loanLoanWithAssociations(loanId);
        } else {
            Toast.makeText(getContext(), getString(R.string.internet_not_connected),
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        hideProgressBar();
        loanRepaymentSchedulePresenter.detachView();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        showUserInterface();
    }
}
