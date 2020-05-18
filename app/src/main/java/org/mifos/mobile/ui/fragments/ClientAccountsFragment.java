package org.mifos.mobile.ui.fragments;
/*
~This project is licensed under the open source MPL V2.
~See https://github.com/openMF/self-service-app/blob/master/LICENSE.md
*/

import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import org.mifos.mobile.R;
import org.mifos.mobile.models.accounts.loan.LoanAccount;
import org.mifos.mobile.models.accounts.savings.SavingAccount;
import org.mifos.mobile.models.accounts.share.ShareAccount;
import org.mifos.mobile.presenters.AccountsPresenter;
import org.mifos.mobile.ui.activities.HomeActivity;
import org.mifos.mobile.ui.activities.LoanApplicationActivity;
import org.mifos.mobile.ui.activities.SavingsAccountApplicationActivity;
import org.mifos.mobile.ui.activities.base.BaseActivity;
import org.mifos.mobile.ui.adapters.CheckBoxAdapter;
import org.mifos.mobile.ui.adapters.ViewPagerAdapter;
import org.mifos.mobile.ui.enums.AccountType;
import org.mifos.mobile.ui.fragments.base.BaseFragment;
import org.mifos.mobile.ui.views.AccountsView;
import org.mifos.mobile.utils.Constants;
import org.mifos.mobile.utils.MaterialDialog;
import org.mifos.mobile.utils.StatusUtils;

import java.util.List;

import javax.inject.Inject;

import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ClientAccountsFragment extends BaseFragment implements AccountsView {

    @BindView(R.id.viewpager)
    ViewPager viewPager;

    @BindView(R.id.tabs)
    TabLayout tabLayout;


    @BindView(R.id.fab_create_loan)
    FloatingActionButton fabCreateLoan;

    @Inject
    AccountsPresenter accountsPresenter;

    @Inject
    CheckBoxAdapter checkBoxAdapter;

    private RecyclerView checkBoxRecyclerView;
    private AccountType accountType;
    private boolean isDialogBoxSelected = false;

    public static ClientAccountsFragment newInstance(AccountType accountType) {
        ClientAccountsFragment clientAccountsFragment = new ClientAccountsFragment();
        Bundle args = new Bundle();
        args.putSerializable(Constants.ACCOUNT_TYPE, accountType);
        clientAccountsFragment.setArguments(args);
        return clientAccountsFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        if (getArguments() != null) {
            accountType = (AccountType) getArguments().getSerializable(Constants.ACCOUNT_TYPE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_client_accounts, container, false);
        ((BaseActivity) getActivity()).getActivityComponent().inject(this);
        ButterKnife.bind(this, view);
        accountsPresenter.attachView(this);
        setToolbarTitle(getString(R.string.accounts));

        setUpViewPagerAndTabLayout();

        if (savedInstanceState == null) {
            accountsPresenter.loadClientAccounts();
        }

        return view;
    }

    /**
     * Setting up {@link ViewPagerAdapter} and {@link TabLayout} for Savings, Loans and Share
     * accounts. {@code accountType} is used for setting the current Fragment
     */
    private void setUpViewPagerAndTabLayout() {
        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getChildFragmentManager());

        viewPagerAdapter.addFragment(AccountsFragment.newInstance(Constants.SAVINGS_ACCOUNTS),
                getString(R.string.savings));
        viewPagerAdapter.addFragment(AccountsFragment.newInstance(Constants.LOAN_ACCOUNTS),
                getString(R.string.loan));
        viewPagerAdapter.addFragment(AccountsFragment.newInstance(Constants.SHARE_ACCOUNTS),
                getString(R.string.share));

        viewPager.setOffscreenPageLimit(2);
        viewPager.setAdapter(viewPagerAdapter);
        switch (accountType) {
            case SAVINGS:
                viewPager.setCurrentItem(0);
                break;
            case LOAN:
                viewPager.setCurrentItem(1);
                break;
            case SHARE:
                viewPager.setCurrentItem(2);
                break;
        }
        tabLayout.setupWithViewPager(viewPager);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset,
                    int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                getActivity().invalidateOptionsMenu();
                ((HomeActivity) getActivity()).hideKeyboard(getView());

                if (position == 0 || position == 1) {
                    fabCreateLoan.setVisibility(View.VISIBLE);
                } else {
                    fabCreateLoan.hide();
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    /**
     * Returns tag of Fragment present at {@code position}
     *
     * @param position position of Fragment
     * @return Tag of Fragment
     */
    private String getFragmentTag(int position) {
        return "android:switcher:" + R.id.viewpager + ":" + position;
    }

    /**
     * It provides with {@code shareAccounts} fetched from server which is then passed to fragment
     * implementing {@link AccountsView} i.e. {@link AccountsFragment} which further displays them
     * in a recyclerView
     *
     * @param shareAccounts {@link List} of {@link ShareAccount}
     */
    @Override
    public void showShareAccounts(List<ShareAccount> shareAccounts) {
        ((AccountsView) getChildFragmentManager().findFragmentByTag(getFragmentTag(2))).
                showShareAccounts(shareAccounts);
        ((AccountsView) getChildFragmentManager().findFragmentByTag(getFragmentTag(2))).
                hideProgress();
    }

    /**
     * It provides with {@code loanAccounts} fetched from server which is then passed to fragment
     * implementing {@link AccountsView} i.e. {@link AccountsFragment} which further displays them
     * in a recyclerView
     *
     * @param loanAccounts {@link List} of {@link LoanAccount}
     */
    @Override
    public void showLoanAccounts(List<LoanAccount> loanAccounts) {
        ((AccountsView) getChildFragmentManager().findFragmentByTag(getFragmentTag(1)))
                .showLoanAccounts(loanAccounts);
        ((AccountsView) getChildFragmentManager().findFragmentByTag(getFragmentTag(1)))
                .hideProgress();
    }

    /**
     * It provides with {@code savingAccounts} fetched from server which is then passed to fragment
     * implementing {@link AccountsView} i.e. {@link AccountsFragment} which further displays them
     * in a recyclerView
     *
     * @param savingAccounts {@link List} of {@link SavingAccount}
     */
    @Override
    public void showSavingsAccounts(List<SavingAccount> savingAccounts) {
        ((AccountsView) getChildFragmentManager().findFragmentByTag(getFragmentTag(0)))
                .showSavingsAccounts(savingAccounts);
        ((AccountsView) getChildFragmentManager().findFragmentByTag(getFragmentTag(0)))
                .hideProgress();
    }

    /**
     * Responds to {@link FloatingActionButton} for creating new loans
     */
    @OnClick(R.id.fab_create_loan)
    public void createLoan() {
        switch (viewPager.getCurrentItem()) {
            case 0:
                startActivity(new Intent(getActivity(), SavingsAccountApplicationActivity.class));
                break;
            case 1:
                startActivity(new Intent(getActivity(), LoanApplicationActivity.class));
                break;
        }
    }


    /**
     * It is called whenever any error occurs while executing a request which passes errorMessage to
     * fragment implementing {@link AccountsView} i.e. {@link AccountsFragment} which further
     * displays the errorMessage
     *
     * @param errorMessage Error message that tells the user about the problem.
     */
    @Override
    public void showError(String errorMessage) {
        ((AccountsView) getChildFragmentManager().findFragmentByTag(getFragmentTag(0)))
                .showError(getString(R.string.error_fetching_accounts));
        ((AccountsView) getChildFragmentManager().findFragmentByTag(getFragmentTag(1)))
                .showError(getString(R.string.error_fetching_accounts));
        ((AccountsView) getChildFragmentManager().findFragmentByTag(getFragmentTag(2)))
                .showError(getString(R.string.error_fetching_accounts));
        Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showProgress() { }

    @Override
    public void hideProgress() { }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        accountsPresenter.detachView();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_account, menu);
        switch (viewPager.getCurrentItem()) {
            case 0:
                setItemVisibility(menu, true, false, false,
                        true, false, false);
                initSearch(menu, AccountType.SAVINGS);
                break;
            case 1:
                setItemVisibility(menu, false, true, false,
                        false, true, false);
                initSearch(menu, AccountType.LOAN);
                break;
            case 2:
                setItemVisibility(menu, false, false, true,
                        false, false, true);
                initSearch(menu, AccountType.SHARE);
                break;
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    private void setItemVisibility( Menu menu, boolean fSavings, boolean fLoan, boolean fShare,
                                   boolean sSavings, boolean sLoan, boolean sShare) {
        menu.findItem(R.id.menu_filter_savings).setVisible(fSavings);
        menu.findItem(R.id.menu_filter_loan).setVisible(fLoan);
        menu.findItem(R.id.menu_filter_shares).setVisible(fShare);
        menu.findItem(R.id.menu_search_saving).setVisible(sSavings);
        menu.findItem(R.id.menu_search_loan).setVisible(sLoan);
        menu.findItem(R.id.menu_search_share).setVisible(sShare);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_filter_savings:
                showFilterDialog(AccountType.SAVINGS);
                break;
            case R.id.menu_filter_loan:
                showFilterDialog(AccountType.LOAN);
                break;
            case R.id.menu_filter_shares:
                showFilterDialog(AccountType.SHARE);
                break;
        }
        return true;
    }

    /**
     * Initializes the search option in {@link Menu} depending upon {@code account}
     *
     * @param menu    Interface for managing the items in a menu.
     * @param account An enum of {@link AccountType}
     */
    private void initSearch(Menu menu, final AccountType account) {
        SearchManager manager = (SearchManager) getActivity().
                getSystemService(Context.SEARCH_SERVICE);
        SearchView search = null;

        if (account == AccountType.SAVINGS) {
            search = (SearchView) menu.findItem(R.id.menu_search_saving).getActionView();
        } else if (account == AccountType.LOAN) {
            search = (SearchView) menu.findItem(R.id.menu_search_loan).getActionView();
        } else if (account == AccountType.SHARE) {
            search = (SearchView) menu.findItem(R.id.menu_search_share).getActionView();
        }

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;
        search.setMaxWidth((int) (0.75 * width));
        search.setSearchableInfo(manager.getSearchableInfo(getActivity().getComponentName()));
        search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                if (account == AccountType.SAVINGS) {
                    ((AccountsFragment) getChildFragmentManager().findFragmentByTag(
                            getFragmentTag(0))).searchSavingsAccount(newText);
                } else if (account == AccountType.LOAN) {
                    ((AccountsFragment) getChildFragmentManager().findFragmentByTag(
                            getFragmentTag(1))).searchLoanAccount(newText);
                } else if (account == AccountType.SHARE) {
                    ((AccountsFragment) getChildFragmentManager().findFragmentByTag(
                            getFragmentTag(2))).searchSharesAccount(newText);
                }

                return false;
            }
        });
    }

    /**
     * Displays a filter dialog according to the {@code account} provided in the parameter
     *
     * @param account An enum of {@link AccountType}
     */
    private void showFilterDialog(final AccountType account) {
        if (isDialogBoxSelected) {
            return;
        }
        isDialogBoxSelected = true;
        String title = "";
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(RecyclerView.VERTICAL);

        checkBoxRecyclerView = new RecyclerView(getActivity());
        checkBoxRecyclerView.setLayoutManager(layoutManager);
        checkBoxRecyclerView.setAdapter(checkBoxAdapter);

        if (account == AccountType.SAVINGS) {
            if (((AccountsFragment) getChildFragmentManager().findFragmentByTag(
                    getFragmentTag(0))).getCurrentFilterList() == null) {
                checkBoxAdapter.setStatusList(StatusUtils.
                        getSavingsAccountStatusList(getActivity()));
            } else {
                checkBoxAdapter.setStatusList(((AccountsFragment) getChildFragmentManager()
                        .findFragmentByTag(getFragmentTag(0))).getCurrentFilterList());
            }

            title = getString(R.string.filter_savings);
        } else if (account == AccountType.LOAN) {
            if (((AccountsFragment) getChildFragmentManager().findFragmentByTag(
                    getFragmentTag(1))).getCurrentFilterList() == null) {
                checkBoxAdapter.setStatusList(StatusUtils.
                        getLoanAccountStatusList(getActivity()));
            } else {
                checkBoxAdapter.setStatusList(((AccountsFragment) getChildFragmentManager()
                        .findFragmentByTag(getFragmentTag(1))).getCurrentFilterList());
            }

            title = getString(R.string.filter_loan);
        } else if (account == AccountType.SHARE) {
            if (((AccountsFragment) getChildFragmentManager().findFragmentByTag(
                    getFragmentTag(2))).getCurrentFilterList() == null) {
                checkBoxAdapter.setStatusList(StatusUtils.
                        getShareAccountStatusList(getActivity()));
            } else {
                checkBoxAdapter.setStatusList(((AccountsFragment) getChildFragmentManager()
                        .findFragmentByTag(getFragmentTag(2))).getCurrentFilterList());
            }

            title = getString(R.string.filter_share);
        }

        new MaterialDialog.Builder().init(getActivity())
                .setTitle(title)
                .setCancelable(false)
                .setMessage(getString(R.string.select_you_want))
                .addView(checkBoxRecyclerView)
                .setPositiveButton(getString(R.string.filter), new DialogInterface.
                        OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        isDialogBoxSelected = false;
                        if (account == AccountType.SAVINGS) {
                            ((AccountsFragment) getChildFragmentManager().findFragmentByTag(
                                    getFragmentTag(0)))
                                    .setCurrentFilterList(checkBoxAdapter.getStatusList());

                            ((AccountsFragment) getChildFragmentManager().findFragmentByTag(
                                    getFragmentTag(0)))
                                    .filterSavingsAccount(checkBoxAdapter.getStatusList());
                        } else if (account == AccountType.LOAN) {
                            ((AccountsFragment) getChildFragmentManager().findFragmentByTag(
                                    getFragmentTag(1)))
                                    .setCurrentFilterList(checkBoxAdapter.getStatusList());

                            ((AccountsFragment) getChildFragmentManager().findFragmentByTag(
                                    getFragmentTag(1)))
                                    .filterLoanAccount(checkBoxAdapter.getStatusList());
                        } else if (account == AccountType.SHARE) {
                            ((AccountsFragment) getChildFragmentManager().findFragmentByTag(
                                    getFragmentTag(2)))
                                    .setCurrentFilterList(checkBoxAdapter.getStatusList());

                            ((AccountsFragment) getChildFragmentManager().findFragmentByTag(
                                    getFragmentTag(2)))
                                    .filterShareAccount(checkBoxAdapter.getStatusList());
                        }

                    }
                })
                .setNeutralButton(R.string.clear_filters, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        isDialogBoxSelected = false;
                        if (account == AccountType.SAVINGS) {
                            ((AccountsFragment) getChildFragmentManager().findFragmentByTag(
                                    getFragmentTag(0))).clearFilter();
                            checkBoxAdapter.setStatusList(StatusUtils.
                                    getSavingsAccountStatusList(getActivity()));
                            accountsPresenter.loadAccounts(Constants.SAVINGS_ACCOUNTS);
                        } else if (account == AccountType.LOAN) {
                            ((AccountsFragment) getChildFragmentManager().findFragmentByTag(
                                    getFragmentTag(1))).clearFilter();
                            checkBoxAdapter.setStatusList(StatusUtils.
                                    getLoanAccountStatusList(getActivity()));
                            accountsPresenter.loadAccounts(Constants.LOAN_ACCOUNTS);
                        } else if (account == AccountType.SHARE) {
                            ((AccountsFragment) getChildFragmentManager().findFragmentByTag(
                                    getFragmentTag(2))).clearFilter();
                            checkBoxAdapter.setStatusList(StatusUtils.
                                    getShareAccountStatusList(getActivity()));
                            accountsPresenter.loadAccounts(Constants.SHARE_ACCOUNTS);
                        }

                    }
                })
                .setNegativeButton(getString(R.string.cancel),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                isDialogBoxSelected = false;
                            }
                        })
                .createMaterialDialog()
                .show();
    }

    @Override
    public void onResume() {
        super.onResume();
        ((BaseActivity) getActivity()).hideToolbarElevation();
    }

    @Override
    public void onPause() {
        super.onPause();
        ((BaseActivity) getActivity()).setToolbarElevation();
    }
}