package org.mifos.mobile.ui.fragments;
/*
~This project is licensed under the open source MPL V2.
~See https://github.com/openMF/self-service-app/blob/master/LICENSE.md
*/

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.therajanmaurya.sweeterror.SweetUIErrorHandler;

import org.mifos.mobile.R;
import org.mifos.mobile.models.accounts.loan.LoanWithAssociations;
import org.mifos.mobile.presenters.LoanAccountsTransactionPresenter;
import org.mifos.mobile.ui.activities.base.BaseActivity;
import org.mifos.mobile.ui.adapters.RecentTransactionListAdapter;
import org.mifos.mobile.ui.enums.TransactionType;
import org.mifos.mobile.ui.fragments.base.BaseFragment;
import org.mifos.mobile.ui.views.LoanAccountsTransactionView;
import org.mifos.mobile.utils.Constants;
import org.mifos.mobile.utils.Network;
import org.mifos.mobile.utils.RecyclerItemClickListener;

import javax.inject.Inject;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by dilpreet on 4/3/17.
 */

public class LoanAccountTransactionFragment extends BaseFragment
        implements LoanAccountsTransactionView, RecyclerItemClickListener.OnItemClickListener {

    @BindView(R.id.layout_error)
    View layoutError;

    @BindView(R.id.ll_loan_account_trans)
    LinearLayout llLoanAccountTrans;

    @BindView(R.id.tv_loan_product_name)
    TextView tvLoanProductName;

    @BindView(R.id.rv_loan_transactions)
    RecyclerView rvLoanTransactions;

    @Inject
    RecentTransactionListAdapter transactionsListAdapter;

    @Inject
    LoanAccountsTransactionPresenter loanAccountsTransactionPresenter;

    private long loanId;
    private View rootView;
    private LoanWithAssociations loanWithAssociations;
    private SweetUIErrorHandler sweetUIErrorHandler;

    public static LoanAccountTransactionFragment newInstance(long loanId) {
        LoanAccountTransactionFragment fragment = new LoanAccountTransactionFragment();
        Bundle args = new Bundle();
        args.putLong(Constants.LOAN_ID, loanId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((BaseActivity) getActivity()).getActivityComponent().inject(this);
        if (getArguments() != null) {
            loanId = getArguments().getLong(Constants.LOAN_ID);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_loan_account_transactions, container, false);
        setToolbarTitle(getString(R.string.transactions));

        ButterKnife.bind(this, rootView);
        loanAccountsTransactionPresenter.attachView(this);

        sweetUIErrorHandler = new SweetUIErrorHandler(getContext(), rootView);
        showUserInterface();
        if (savedInstanceState == null) {
            loanAccountsTransactionPresenter.loadLoanAccountDetails(loanId);
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
            showLoanTransactions((LoanWithAssociations) savedInstanceState.
                    getParcelable(Constants.LOAN_ACCOUNT));
        }
    }

    /**
     * Initialized {@link RecyclerView} {@code rvLoanTransactions}
     */
    @Override
    public void showUserInterface() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rvLoanTransactions.setHasFixedSize(true);
        rvLoanTransactions.setLayoutManager(layoutManager);
        rvLoanTransactions.setAdapter(transactionsListAdapter);
        rvLoanTransactions.addOnItemTouchListener(
                new RecyclerItemClickListener(getActivity(), this));
    }

    /**
     * Fetches {@code loanWithAssociations} from server and intializes it in
     * {@code transactionsListAdapter}
     *
     * @param loanWithAssociations object containing details about a Loan Account with Associations
     */
    @Override
    public void showLoanTransactions(LoanWithAssociations loanWithAssociations) {
        this.loanWithAssociations = loanWithAssociations;
        llLoanAccountTrans.setVisibility(View.VISIBLE);
        tvLoanProductName.setText(loanWithAssociations.getLoanProductName());
        transactionsListAdapter.setTransactions(loanWithAssociations.getTransactions());
    }

    /**
     * Sets a {@link TextView} with a msg if Transactions list is empty
     */
    @Override
    public void showEmptyTransactions(LoanWithAssociations loanWithAssociations) {
        sweetUIErrorHandler.showSweetEmptyUI(getString(R.string.transactions),
                R.drawable.ic_compare_arrows_black_24dp, rvLoanTransactions, layoutError);
    }

    /**
     * It is called whenever any error occurs while executing a request
     *
     * @param message Error message that tells the user about the problem.
     */
    @Override
    public void showErrorFetchingLoanAccountsDetail(String message) {
        if (!Network.isConnected(getActivity())) {
            sweetUIErrorHandler.showSweetNoInternetUI(rvLoanTransactions, layoutError);
        } else {
            sweetUIErrorHandler.showSweetErrorUI(message,
                    rvLoanTransactions, layoutError);
            Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
        }
    }

    @OnClick(R.id.btn_try_again)
    public void retryClicked() {
        if (Network.isConnected(getContext())) {
            sweetUIErrorHandler.hideSweetErrorLayoutUI(rvLoanTransactions, layoutError);
            loanAccountsTransactionPresenter.loadLoanAccountDetails(loanId);
        } else {
            Toast.makeText(getContext(), getString(R.string.internet_not_connected),
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onItemClick(View childView, int position) {
        ((BaseActivity) getActivity()).replaceFragment(TransactionDetailsFragment
                        .newInstance(loanId,
                                loanWithAssociations.getTransactions().get(position).getId(),
                                TransactionType.LOAN)
                , true, R.id.container);
    }

    @Override
    public void onItemLongPress(View childView, int position) {

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
        loanAccountsTransactionPresenter.detachView();
    }
}
