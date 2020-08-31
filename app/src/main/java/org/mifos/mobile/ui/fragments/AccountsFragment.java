package org.mifos.mobile.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.github.therajanmaurya.sweeterror.SweetUIErrorHandler;

import org.mifos.mobile.R;
import org.mifos.mobile.models.CheckboxStatus;
import org.mifos.mobile.models.accounts.loan.LoanAccount;
import org.mifos.mobile.models.accounts.savings.SavingAccount;
import org.mifos.mobile.models.accounts.share.ShareAccount;
import org.mifos.mobile.presenters.AccountsPresenter;
import org.mifos.mobile.ui.activities.LoanAccountContainerActivity;
import org.mifos.mobile.ui.activities.SavingsAccountContainerActivity;
import org.mifos.mobile.ui.activities.base.BaseActivity;
import org.mifos.mobile.ui.adapters.LoanAccountsListAdapter;
import org.mifos.mobile.ui.adapters.SavingAccountsListAdapter;
import org.mifos.mobile.ui.adapters.ShareAccountsListAdapter;
import org.mifos.mobile.ui.fragments.base.BaseFragment;
import org.mifos.mobile.ui.views.AccountsView;
import org.mifos.mobile.utils.ComparatorBasedOnId;
import org.mifos.mobile.utils.Constants;
import org.mifos.mobile.utils.DividerItemDecoration;
import org.mifos.mobile.utils.Network;
import org.mifos.mobile.utils.RecyclerItemClickListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Rajan Maurya on 23/10/16.
 */

public class AccountsFragment extends BaseFragment implements
        SwipeRefreshLayout.OnRefreshListener, AccountsView,
        RecyclerItemClickListener.OnItemClickListener {

    public static final String LOG_TAG = AccountsFragment.class.getSimpleName();

    @BindView(R.id.rv_accounts)
    RecyclerView rvAccounts;

    @BindView(R.id.swipe_container)
    SwipeRefreshLayout swipeRefreshLayout;

    @BindView(R.id.layout_error)
    View layoutError;

    @Inject
    AccountsPresenter accountsPresenter;

    @Inject
    LoanAccountsListAdapter loanAccountsListAdapter;

    @Inject
    SavingAccountsListAdapter savingAccountsListAdapter;

    @Inject
    ShareAccountsListAdapter shareAccountsListAdapter;

    View rootView;
    private String accountType;
    private List<LoanAccount> loanAccounts;
    private List<SavingAccount> savingAccounts;
    private List<ShareAccount> shareAccounts;
    private List<CheckboxStatus> currentFilterList;
    private SweetUIErrorHandler sweetUIErrorHandler;

    /**
     * Method to get the current filter list for the fragment
     *
     * @return currentFilterList
     */
    public List<CheckboxStatus> getCurrentFilterList() {
        return currentFilterList;
    }

    /**
     * Method to set current filter list value
     */
    public void setCurrentFilterList(List<CheckboxStatus> currentFilterList) {
        this.currentFilterList = currentFilterList;
    }

    public static AccountsFragment newInstance(String accountType) {
        AccountsFragment fragment = new AccountsFragment();
        Bundle args = new Bundle();
        args.putString(Constants.ACCOUNT_TYPE, accountType);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((BaseActivity) getActivity()).getActivityComponent().inject(this);
        loanAccounts = new ArrayList<>();
        savingAccounts = new ArrayList<>();
        shareAccounts = new ArrayList<>();
        if (getArguments() != null) {
            accountType = getArguments().getString(Constants.ACCOUNT_TYPE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_accounts, container, false);

        ButterKnife.bind(this, rootView);
        accountsPresenter.attachView(this);
        sweetUIErrorHandler = new SweetUIErrorHandler(getActivity(), rootView);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        rvAccounts.setLayoutManager(layoutManager);
        rvAccounts.setHasFixedSize(true);
        rvAccounts.addItemDecoration(new DividerItemDecoration(getActivity(),
                layoutManager.getOrientation()));
        rvAccounts.addOnItemTouchListener(new RecyclerItemClickListener(getActivity(), this));
        swipeRefreshLayout.setColorSchemeColors(getActivity()
                .getResources().getIntArray(R.array.swipeRefreshColors));
        swipeRefreshLayout.setOnRefreshListener(this);
        if (Network.isConnected(getContext())) {
            loadAccounts();
        }
        if (savedInstanceState == null) {
            showProgress();
        }

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(Constants.ACCOUNT_TYPE, accountType);
        switch (accountType) {
            case Constants.SAVINGS_ACCOUNTS:
                outState.putParcelableArrayList(Constants.SAVINGS_ACCOUNTS, new ArrayList
                        <Parcelable>(savingAccounts));
                break;
            case Constants.LOAN_ACCOUNTS:
                outState.putParcelableArrayList(Constants.LOAN_ACCOUNTS, new ArrayList
                        <Parcelable>(loanAccounts));
                break;
            case Constants.SHARE_ACCOUNTS:
                outState.putParcelableArrayList(Constants.SHARE_ACCOUNTS, new ArrayList
                        <Parcelable>(shareAccounts));
                break;
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null) {
            accountType = savedInstanceState.getString(Constants.ACCOUNT_TYPE);
            switch (accountType) {
                case Constants.SAVINGS_ACCOUNTS:
                    List<SavingAccount> savingAccountList = savedInstanceState.
                            getParcelableArrayList(Constants.SAVINGS_ACCOUNTS);
                    showSavingsAccounts(savingAccountList);
                    break;
                case Constants.LOAN_ACCOUNTS:
                    List<LoanAccount> loanAccountList = savedInstanceState.getParcelableArrayList(
                            Constants.LOAN_ACCOUNTS);
                    showLoanAccounts(loanAccountList);
                    break;
                case Constants.SHARE_ACCOUNTS:
                    List<ShareAccount> shareAccountList = savedInstanceState.getParcelableArrayList(
                            Constants.SHARE_ACCOUNTS);
                    showShareAccounts(shareAccountList);
                    break;
            }
        }
    }


    /**
     * Used for reloading account of a particular {@code accountType} in case of a network error.
     */

    @OnClick(R.id.btn_try_again)
    void onRetry() {
        if (Network.isConnected(getContext())) {
            loadAccounts();
        } else {
            Toast.makeText(getContext(), getString(R.string.internet_not_connected),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void loadAccounts() {
        sweetUIErrorHandler.hideSweetErrorLayoutUI(rvAccounts, layoutError);
        accountsPresenter.loadAccounts(accountType);
    }

    /**
     * This method is called when we swipe to refresh the view and is used for reloading account of
     * a particular {@code accountType}
     */
    @Override
    public void onRefresh() {
        if (Network.isConnected(getContext())) {
            clearFilter();
            accountsPresenter.loadAccounts(accountType);
        } else {
            hideProgress();
            sweetUIErrorHandler.showSweetNoInternetUI(rvAccounts, layoutError);
            Toast.makeText(getContext(), getString(R.string.internet_not_connected),
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Method to clear the current filters and set
     * currentFilterList = null
     */
    public void clearFilter() {
        sweetUIErrorHandler.hideSweetErrorLayoutUI(rvAccounts, layoutError);
        currentFilterList = null;
    }

    /**
     * Shows {@link List} of {@link LoanAccount} fetched from server using
     * {@link LoanAccountsListAdapter}
     *
     * @param loanAccounts {@link List} of {@link LoanAccount}
     */
    @Override
    public void showLoanAccounts(List<LoanAccount> loanAccounts) {
        Collections.sort(loanAccounts, new ComparatorBasedOnId());
        this.loanAccounts = loanAccounts;
        if (loanAccounts.size() != 0) {
            loanAccountsListAdapter.setLoanAccountsList(loanAccounts);
            rvAccounts.setAdapter(loanAccountsListAdapter);
        } else {
            showEmptyAccounts(getString(R.string.loan_account));
        }
    }

    /**
     * Shows {@link List} of {@link SavingAccount} fetched from server using
     * {@link SavingAccountsListAdapter}
     *
     * @param savingAccounts {@link List} of {@link SavingAccount}
     */
    @Override
    public void showSavingsAccounts(List<SavingAccount> savingAccounts) {
        Collections.sort(savingAccounts, new ComparatorBasedOnId());
        this.savingAccounts = savingAccounts;
        if (savingAccounts.size() != 0) {
            savingAccountsListAdapter.setSavingAccountsList(savingAccounts);
            rvAccounts.setAdapter(savingAccountsListAdapter);
        } else {
            showEmptyAccounts(getString(R.string.savings_account));
        }
    }

    /**
     * Shows {@link List} of {@link ShareAccount} fetched from server using
     * {@link ShareAccountsListAdapter}
     *
     * @param shareAccounts {@link List} of {@link ShareAccount}
     */
    @Override
    public void showShareAccounts(List<ShareAccount> shareAccounts) {
        Collections.sort(shareAccounts, new ComparatorBasedOnId());
        this.shareAccounts = shareAccounts;
        if (shareAccounts.size() != 0) {
            shareAccountsListAdapter.setShareAccountsList(shareAccounts);
            rvAccounts.setAdapter(shareAccountsListAdapter);
        } else {
            showEmptyAccounts(getString(R.string.share_account));
        }
    }


    /**
     * Shows an error layout when this function is called.
     *
     * @param emptyAccounts Text to show in {@code noAccountText}
     */
    public void showEmptyAccounts(String emptyAccounts) {
        sweetUIErrorHandler.showSweetEmptyUI(emptyAccounts,
                R.drawable.ic_account_balance_wallet_black_background_24dp,
                rvAccounts, layoutError);
    }

    /**
     * Used for searching an {@code input} String in {@code savingAccounts} and displaying it in the
     * recyclerview.
     *
     * @param input String which is needs to be searched in list
     */
    public void searchSavingsAccount(String input) {
        List<SavingAccount> searchResult = accountsPresenter.
                searchInSavingsList(savingAccounts, input);
        if (searchResult.size() == 0) {
            showEmptyAccounts(getString(R.string.no_saving_account));
        } else {
            sweetUIErrorHandler.hideSweetErrorLayoutUI(rvAccounts, layoutError);
            savingAccountsListAdapter.setSavingAccountsList(searchResult);
        }
    }

    /**
     * Used for searching an {@code input} String in {@code loanAccounts} and displaying it in the
     * recyclerview.
     *
     * @param input String which is needs to be searched in list
     */
    public void searchLoanAccount(String input) {
        List<LoanAccount> searchResult = accountsPresenter.
                searchInLoanList(loanAccounts, input);
        if (searchResult.size() == 0) {
            showEmptyAccounts(getString(R.string.no_loan_account));
        } else {
            sweetUIErrorHandler.hideSweetErrorLayoutUI(rvAccounts, layoutError);
            loanAccountsListAdapter.setLoanAccountsList(searchResult);
        }
    }

    /**
     * Used for searching an {@code input} String in {@code savingAccounts} and displaying it in the
     * recyclerview.
     *
     * @param input String which is needs to be searched in list
     */
    public void searchSharesAccount(String input) {
        List<ShareAccount> searchResult = accountsPresenter.
                searchInSharesList(shareAccounts, input);
        if (searchResult.size() == 0) {
            showEmptyAccounts(getString(R.string.no_sharing_account));
        } else {
            sweetUIErrorHandler.hideSweetErrorLayoutUI(rvAccounts, layoutError);
            shareAccountsListAdapter.setShareAccountsList(searchResult);
        }
    }

    /**
     * Used for filtering {@code savingAccounts} depending upon {@link List} of
     * {@link CheckboxStatus}
     *
     * @param statusModelList {@link List} of {@link CheckboxStatus}
     */
    public void filterSavingsAccount(List<CheckboxStatus> statusModelList) {
        List<SavingAccount> filteredSavings = new ArrayList<>();
        for (CheckboxStatus status : accountsPresenter.getCheckedStatus(statusModelList)) {
            filteredSavings.addAll(accountsPresenter.getFilteredSavingsAccount(savingAccounts,
                    status));
        }
        if (filteredSavings.size() == 0) {
            showEmptyAccounts(getString(R.string.no_saving_account));
        } else {
            savingAccountsListAdapter.setSavingAccountsList(filteredSavings);
        }
    }

    /**
     * Used for filtering {@code loanAccounts} depending upon {@link List} of
     * {@link CheckboxStatus}
     *
     * @param statusModelList {@link List} of {@link CheckboxStatus}
     */
    public void filterLoanAccount(List<CheckboxStatus> statusModelList) {
        List<LoanAccount> filteredSavings = new ArrayList<>();
        for (CheckboxStatus status : accountsPresenter.getCheckedStatus(statusModelList)) {
            filteredSavings.addAll(accountsPresenter.getFilteredLoanAccount(loanAccounts,
                    status));
        }
        if (filteredSavings.size() == 0) {
            showEmptyAccounts(getString(R.string.no_loan_account));
        } else {
            loanAccountsListAdapter.setLoanAccountsList(filteredSavings);
        }
    }

    /**
     * Used for filtering {@code shareAccounts} depending upon {@link List} of
     * {@link CheckboxStatus}
     *
     * @param statusModelList {@link List} of {@link CheckboxStatus}
     */
    public void filterShareAccount(List<CheckboxStatus> statusModelList) {
        List<ShareAccount> filteredSavings = new ArrayList<>();
        for (CheckboxStatus status : accountsPresenter.getCheckedStatus(statusModelList)) {
            filteredSavings.addAll(accountsPresenter.getFilteredShareAccount(shareAccounts,
                    status));
        }
        if (filteredSavings.size() == 0) {
            showEmptyAccounts(getString(R.string.no_sharing_account));
        } else {
            shareAccountsListAdapter.setShareAccountsList(filteredSavings);
        }
    }

    /**
     * It is called whenever any error occurs while executing a request
     *
     * @param errorMessage Error message that tells the user about the problem.
     */
    @Override
    public void showError(String errorMessage) {
        if (!Network.isConnected(getContext())) {
            sweetUIErrorHandler.showSweetNoInternetUI(rvAccounts, layoutError);
        } else {
            sweetUIErrorHandler.showSweetErrorUI(errorMessage, rvAccounts, layoutError);
            Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
        }
        hideProgress();
    }

    /**
     * Shows {@link SwipeRefreshLayout}
     */
    @Override
    public void showProgress() {
        showSwipeRefreshLayout(true);
    }

    /**
     * Hides {@link SwipeRefreshLayout}
     */
    @Override
    public void hideProgress() {
        showSwipeRefreshLayout(false);
    }

    public void showSwipeRefreshLayout(boolean show) {
        swipeRefreshLayout.setRefreshing(show);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        accountsPresenter.detachView();
    }

    @Override
    public void onItemClick(View childView, int position) {
        Intent intent = null;
        switch (accountType) {
            case Constants.SAVINGS_ACCOUNTS:
                intent = new Intent(getActivity(), SavingsAccountContainerActivity.class);
                intent.putExtra(Constants.SAVINGS_ID, savingAccountsListAdapter.
                        getSavingAccountsList().get(position).getId());
                break;
            case Constants.LOAN_ACCOUNTS:
                intent = new Intent(getActivity(), LoanAccountContainerActivity.class);
                intent.putExtra(Constants.LOAN_ID, loanAccountsListAdapter.getLoanAccountsList().
                        get(position).getId());
                break;
        }
        openActivity(intent);
    }

    @Override
    public void onItemLongPress(View childView, int position) {

    }

    /**
     * This function opens up an activity only if the intent
     * is not null.
     * <p>
     * This will prevent the application from crashing if the
     * intent is null.
     */
    private void openActivity(Intent intent) {
        if (intent != null) {
            startActivity(intent);
        }
    }
}
