package org.mifos.mobile.ui.fragments;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.therajanmaurya.sweeterror.SweetUIErrorHandler;

import org.mifos.mobile.R;
import org.mifos.mobile.models.CheckboxStatus;
import org.mifos.mobile.models.accounts.savings.SavingsWithAssociations;
import org.mifos.mobile.models.accounts.savings.Transactions;
import org.mifos.mobile.presenters.SavingAccountsTransactionPresenter;
import org.mifos.mobile.ui.activities.base.BaseActivity;
import org.mifos.mobile.ui.adapters.CheckBoxAdapter;
import org.mifos.mobile.ui.adapters.SavingAccountsTransactionListAdapter;
import org.mifos.mobile.ui.fragments.base.BaseFragment;
import org.mifos.mobile.ui.views.SavingAccountsTransactionView;
import org.mifos.mobile.utils.Constants;
import org.mifos.mobile.utils.DateHelper;
import org.mifos.mobile.utils.DatePick;
import org.mifos.mobile.utils.MFDatePicker;
import org.mifos.mobile.utils.MaterialDialog;
import org.mifos.mobile.utils.Network;
import org.mifos.mobile.utils.StatusUtils;
import org.mifos.mobile.utils.Toaster;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by dilpreet on 6/3/17.
 */

public class SavingAccountsTransactionFragment extends BaseFragment
        implements SavingAccountsTransactionView,
//        RadioGroup.OnCheckedChangeListener,
        MFDatePicker.OnDatePickListener {

    @BindView(R.id.ll_account)
    LinearLayout layoutAccount;

    @BindView(R.id.layout_error)
    View layoutError;

    @BindView(R.id.rv_saving_accounts_transaction)
    RecyclerView rvSavingAccountsTransaction;

    @Inject
    SavingAccountsTransactionListAdapter transactionListAdapter;

    @Inject
    SavingAccountsTransactionPresenter savingAccountsTransactionPresenter;

    @Inject
    CheckBoxAdapter checkBoxAdapter;

    private TextView tvStartDate, tvEndDate;
    private SweetUIErrorHandler sweetUIErrorHandler;
    private View rootView;
    private long savingsId;
    private List<Transactions> transactionsList;
    private SavingsWithAssociations savingsWithAssociations;
    private DatePick datePick;
    private MFDatePicker mfDatePicker;
    private long startDate, endDate;
    private boolean isReady;
    private List<CheckboxStatus> statusList;
    private boolean isCheckBoxPeriod;
    private int selectedRadioButtonId;
    public boolean active = false;


    public static SavingAccountsTransactionFragment newInstance(long savingsId) {
        SavingAccountsTransactionFragment fragment = new SavingAccountsTransactionFragment();
        Bundle args = new Bundle();
        args.putLong(Constants.SAVINGS_ID, savingsId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        ((BaseActivity) getActivity()).getActivityComponent().inject(this);
        setToolbarTitle(getString(R.string.saving_account_transactions_details));
        if (getArguments() != null) {
            savingsId = getArguments().getLong(Constants.SAVINGS_ID);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
            @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_saving_account_transactions,
                container, false);

        ButterKnife.bind(this, rootView);
        savingAccountsTransactionPresenter.attachView(this);

        sweetUIErrorHandler = new SweetUIErrorHandler(getContext(), rootView);
        showUserInterface();
        if (savedInstanceState == null) {
            savingAccountsTransactionPresenter.loadSavingsWithAssociations(savingsId);
        }
        initializeFilterVariables();
        return rootView;
    }

    private void initializeFilterVariables() {
        statusList = StatusUtils.getSavingsAccountTransactionList(getActivity());
        startDate = -1;
        endDate = -2;
        isCheckBoxPeriod = false;
        isReady = false;
        selectedRadioButtonId = -1;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(Constants.SAVINGS_ACCOUNTS, savingsWithAssociations);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            showSavingAccountsDetail((SavingsWithAssociations) savedInstanceState.
                    getParcelable(Constants.SAVINGS_ACCOUNTS));
        }
    }

    /**
     * Setting up basic components
     */
    @Override
    public void showUserInterface() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rvSavingAccountsTransaction.setHasFixedSize(true);
        rvSavingAccountsTransaction.setLayoutManager(layoutManager);
        rvSavingAccountsTransaction.setAdapter(transactionListAdapter);

//        radioGroup.setOnCheckedChangeListener(this);
        mfDatePicker = MFDatePicker.newInstance(this, MFDatePicker.ALL_DAYS, active);
        active = true;
    }

    /**
     * Provides with {@code savingsWithAssociations} fetched from server which is used to update the
     * {@code transactionListAdapter}
     *
     * @param savingsWithAssociations Contains {@link Transactions} for given Savings account.
     */
    @Override
    public void showSavingAccountsDetail(SavingsWithAssociations
            savingsWithAssociations) {
        layoutAccount.setVisibility(View.VISIBLE);
        this.savingsWithAssociations = savingsWithAssociations;
        transactionsList = savingsWithAssociations.getTransactions();

        if (transactionsList != null && !transactionsList.isEmpty()) {
            transactionListAdapter.setContext(getContext());
            transactionListAdapter.setSavingAccountsTransactionList(transactionsList);
        } else {
            showEmptyTransactions();
        }
    }

    /**
     * It is called whenever any error occurs while executing a request
     *
     * @param message Error message that tells the user about the problem.
     */
    @Override
    public void showErrorFetchingSavingAccountsDetail(String message) {
        if (!Network.isConnected(getActivity())) {
            sweetUIErrorHandler.showSweetNoInternetUI(rvSavingAccountsTransaction, layoutError);
        } else {
            sweetUIErrorHandler.showSweetErrorUI(message, rvSavingAccountsTransaction, layoutError);
        }
    }

    @OnClick(R.id.btn_try_again)
    public void retryClicked() {
        if (Network.isConnected(getContext())) {
            sweetUIErrorHandler.hideSweetErrorLayoutUI(rvSavingAccountsTransaction, layoutError);
            savingAccountsTransactionPresenter.loadSavingsWithAssociations(savingsId);
        } else {
            Toast.makeText(getContext(), getString(R.string.internet_not_connected),
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Provides with a filtered list according to the constraints used in {@code filter()} function
     */
    @Override
    public void showFilteredList(List<Transactions> list) {
        if (list.size() != 0) {
            Toaster.show(rootView, getString(R.string.filtered));
            transactionListAdapter.setSavingAccountsTransactionList(list);
        } else {
            showEmptyTransactions();
        }
    }

    @Override
    public void showEmptyTransactions() {
        sweetUIErrorHandler.showSweetEmptyUI(getString(R.string.transactions),
                R.drawable.ic_compare_arrows_black_24dp, rvSavingAccountsTransaction, layoutError);
    }

    /**
     * Opens up Phone Dialer
     */
    @OnClick(R.id.tv_help_line_number)
    void dialHelpLineNumber() {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + getString(R.string.help_line_number)));
        startActivity(intent);
    }

    private void startDatePick() {
        datePick = DatePick.START;
        mfDatePicker.show(getActivity().getSupportFragmentManager(), Constants
                .DFRAG_DATE_PICKER);
    }

    private void endDatePick() {
        datePick = DatePick.END;
        mfDatePicker.show(getActivity().getSupportFragmentManager(), Constants
                .DFRAG_DATE_PICKER);
    }

    /**
     * A CallBack for {@link MFDatePicker} which provides us with the date selected from the
     * {@link android.app.DatePickerDialog} by {@code mfDatePicker}
     *
     * @param date Date selected by user in {@link String}
     */
    @Override
    public void onDatePicked(String date) {
        long timeInMillis = DateHelper.getDateAsLongFromString(date, "dd-MM-yyyy");
        if (datePick == DatePick.START) {
            tvEndDate.setEnabled(true);
            tvStartDate.setText(DateHelper.getDateAsStringFromLong(timeInMillis));
            startDate = timeInMillis;
        } else {
            endDate = timeInMillis;
            tvEndDate.setText(DateHelper.getDateAsStringFromLong(timeInMillis));
            isReady = true;
        }
    }

    /**
     * Checks if {@code startDate} is less than {@code endDate}
     *
     * @return Returns true if {@code startDate} is less than {@code endDate}
     */
    private boolean isEndDateLargeThanStartDate() {
        return startDate <= endDate;
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
     * Shows a filter dialog
     */

    private void showFilterDialog() {

        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.layout_filter_dialog, null , false);

        final LinearLayout llcheckBoxPeriod =  dialogView.findViewById(R.id.ll_row_checkbox);
        final AppCompatCheckBox checkBoxPeriod = dialogView.findViewById(R.id.cb_select);
        final RadioGroup radioGroupFilter = dialogView.findViewById(R.id.rg_date_filter);
        tvStartDate = dialogView.findViewById(R.id.tv_start_date);
        tvEndDate = dialogView.findViewById(R.id.tv_end_date);
        tvStartDate.setEnabled(false);
        tvEndDate.setEnabled(false);

        //setup listeners
        tvStartDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startDatePick();
            }
        });
        tvEndDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                endDatePick();
            }
        });

        llcheckBoxPeriod.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    checkBoxPeriod.setChecked(!checkBoxPeriod.isChecked());
            }
        });

        checkBoxPeriod.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                isCheckBoxPeriod = isChecked;
                if (!isChecked) {
                    isReady = false;
                    radioGroupFilter.clearCheck();
                    selectedRadioButtonId = -1;
                } else {
                    if (selectedRadioButtonId == -1) {
                        RadioButton btn = dialogView.findViewById(R.id.rb_date);
                        btn.setChecked(true);
                    }
                }
            }
        });

        radioGroupFilter.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                isCheckBoxPeriod = true;
                selectedRadioButtonId = radioGroup.getCheckedRadioButtonId();
                switch (radioGroup.getCheckedRadioButtonId()) {
                    case R.id.rb_four_weeks:
                        tvStartDate.setEnabled(false);
                        tvEndDate.setEnabled(false);
                        startDate = DateHelper.subtractWeeks(4);
                        endDate = System.currentTimeMillis();
                        isReady = true;
                        break;
                    case R.id.rb_three_months:
                        tvStartDate.setEnabled(false);
                        tvEndDate.setEnabled(false);
                        startDate = DateHelper.subtractMonths(3);
                        endDate = System.currentTimeMillis();
                        isReady = true;
                        break;
                    case R.id.rb_six_months:
                        tvStartDate.setEnabled(false);
                        tvEndDate.setEnabled(false);
                        startDate = DateHelper.subtractMonths(6);
                        endDate = System.currentTimeMillis();
                        isReady = true;
                        break;
                    case R.id.rb_date:
                        tvStartDate.setEnabled(true);
                        tvEndDate.setEnabled(false);
                        break;
                }
            }
        });

        //restore prev state
        checkBoxPeriod.setChecked(isCheckBoxPeriod);
        if (selectedRadioButtonId != -1) {
            RadioButton btn = dialogView.findViewById(selectedRadioButtonId);
            btn.setChecked(true);
        }

        RecyclerView checkBoxRecyclerView = dialogView.findViewById(R.id.recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        checkBoxRecyclerView.setLayoutManager(layoutManager);
        checkBoxRecyclerView.setAdapter(checkBoxAdapter);

        checkBoxAdapter.setStatusList(statusList);

        new MaterialDialog.Builder().init(getActivity())
                .setTitle(R.string.savings_account_transaction)
                .addView(dialogView)
                .setPositiveButton(getString(R.string.filter), new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (checkBoxPeriod.isChecked()) {
                            if (!isReady) {
                                Toaster.show(rootView, getString(R.string.select_date));
                                return;
                            } else if (!isEndDateLargeThanStartDate()) {
                                Toaster.show(rootView,
                                        getString(R.string.end_date_must_be_greater));
                                return;
                            }
                            filter(startDate, endDate, checkBoxAdapter.getStatusList());
                        } else {
                            filter(checkBoxAdapter.getStatusList());
                        }
                        filterSavingsAccountTransactionsbyType(checkBoxAdapter.getStatusList());
                    }
                })
                .setNeutralButton(getString(R.string.clear_filters),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                transactionListAdapter
                                        .setSavingAccountsTransactionList(transactionsList);
                                initializeFilterVariables();
                            }
                        })
                .setNegativeButton(R.string.cancel)
                .createMaterialDialog()
                .show();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_saving_accounts_transaction, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    /**
     * Will filter {@code transactionsList} according to {@code startDate} and {@code endDate}
     *
     * @param startDate Starting date
     * @param endDate   Ending date
     */
    private void filter(long startDate, long endDate, List<CheckboxStatus> statusModelList) {
        List<Transactions> dummyTransactionList =
                filterSavingsAccountTransactionsbyType(statusModelList);
        savingAccountsTransactionPresenter.filterTransactionList(dummyTransactionList,
                startDate, endDate);
    }

    /**
     * Will filter {@code transactionsList} according to {@code startDate} and {@code endDate}
     *
     * @param statusModelList Status Model List
     */
    private void filter(List<CheckboxStatus> statusModelList) {
        showFilteredList(filterSavingsAccountTransactionsbyType(statusModelList));
    }
    /**
     * Will filter {@code transactionsList} according to {@code startDate} and {@code endDate}
     * @param statusModelList Status Model List
     */
    private  List<Transactions> filterSavingsAccountTransactionsbyType(List<CheckboxStatus>
                                                                               statusModelList) {
        List<Transactions> filteredSavingsTransactions = new ArrayList<>();
        for (CheckboxStatus status:savingAccountsTransactionPresenter
                .getCheckedStatus(statusModelList)) {
            filteredSavingsTransactions.addAll(savingAccountsTransactionPresenter
                    .filterTranactionListbyType(transactionsList, status));
        }

        return filteredSavingsTransactions;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_filter_savings_transactions:
                showFilterDialog();
                break;
        }
        return true;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        hideProgress();
        savingAccountsTransactionPresenter.detachView();
    }

}
