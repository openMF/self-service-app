package org.mifos.mobile.ui.fragments;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.therajanmaurya.sweeterror.SweetUIErrorHandler;

import org.mifos.mobile.R;
import org.mifos.mobile.models.Transaction;
import org.mifos.mobile.models.accounts.savings.PaymentDetailData;
import org.mifos.mobile.models.accounts.savings.Transactions;
import org.mifos.mobile.presenters.TransactionDetailsPresenter;
import org.mifos.mobile.ui.activities.base.BaseActivity;
import org.mifos.mobile.ui.enums.TransactionType;
import org.mifos.mobile.ui.fragments.base.BaseFragment;
import org.mifos.mobile.ui.views.TransactionDetailsView;
import org.mifos.mobile.utils.Constants;
import org.mifos.mobile.utils.CurrencyUtil;
import org.mifos.mobile.utils.DateHelper;
import org.mifos.mobile.utils.Network;

import javax.inject.Inject;

import androidx.annotation.Nullable;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class TransactionDetailsFragment extends BaseFragment implements
        TransactionDetailsView {

    @BindView(R.id.ll_transaction)
    LinearLayout transactionLayout;

    @BindView(R.id.layout_error)
    View layoutError;

    @BindView(R.id.tv_transaction_id)
    TextView tvTransactionId;

    @BindView(R.id.tv_transaction_type)
    TextView tvTransactionType;

    @BindView(R.id.tv_transaction_date)
    TextView tvTransactionDate;

    @BindView(R.id.tv_transaction_currency)
    TextView tvTransactionCurrency;

    @BindView(R.id.tv_transaction_amount)
    TextView tvTransactionAmount;

    @BindView(R.id.tv_payment_type)
    TextView tvPaymentType;

    @BindView(R.id.tv_payment_acc_no)
    TextView tvPaymentAccountNumber;

    @BindView(R.id.tv_receipt_no)
    TextView tvReceiptNumber;

    @Inject
    TransactionDetailsPresenter transactionDetailsPresenter;

    private View rootView;
    private SweetUIErrorHandler sweetUIErrorHandler;

    private long transactionId;
    private long accountId;
    private TransactionType transactionType;

    // for Client and Loan Account transactions
    private Transaction transaction;

    // for Savings Account transaction
    private Transactions transactions;

    public static TransactionDetailsFragment newInstance(long accountId, long transactionId,
                                                         TransactionType transactionType) {
        TransactionDetailsFragment fragment = new TransactionDetailsFragment();
        Bundle args = new Bundle();
        args.putLong(Constants.ACCOUNT_ID, accountId);
        args.putLong(Constants.TRANSACTION_ID, transactionId);
        args.putSerializable(Constants.TRANSACTION_TYPE, transactionType);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            accountId = getArguments().getLong(Constants.ACCOUNT_ID);
            transactionId = getArguments().getLong(Constants.TRANSACTION_ID);
            transactionType = (TransactionType) getArguments()
                    .getSerializable(Constants.TRANSACTION_TYPE);
        }
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_transaction_details, container, false);
        ((BaseActivity) getActivity()).getActivityComponent().inject(this);
        setToolbarTitle(getString(R.string.transaction_details));
        ButterKnife.bind(this, rootView);
        transactionDetailsPresenter.attachView(this);
        sweetUIErrorHandler = new SweetUIErrorHandler(getContext(), rootView);
        if (savedInstanceState == null) {
            loadTransactionDetails();
        }

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (transactionType == TransactionType.SAVINGS) {
            outState.putParcelable(Constants.TRANSACTION, transactions);
        } else {
            outState.putParcelable(Constants.TRANSACTION, transaction);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null && transactionType == TransactionType.SAVINGS) {
            showSavingsAccountTransactionDetails((Transactions) savedInstanceState.
                    getParcelable(Constants.TRANSACTION));
        } else if (savedInstanceState != null && transactionType != TransactionType.SAVINGS) {
            showTransactionDetails((Transaction) savedInstanceState.
                    getParcelable(Constants.TRANSACTION));
        }
    }

    @Override
    public void showSavingsAccountTransactionDetails(Transactions transactions) {
        this.transactions = transactions;
        String currencySymbol = transactions.getCurrency().getDisplaySymbol();
        PaymentDetailData paymentDetailData = transactions.getPaymentDetailData();

        tvTransactionId.setText(String.valueOf(transactions.getId()));
        tvTransactionType.setText(transactions.getTransactionType().getValue());
        tvTransactionDate.setText(DateHelper.getDateAsString(transactions.getDate()));
        tvTransactionCurrency.setText(transactions.getCurrency().getName());
        tvTransactionAmount.setText(getString(R.string.string_and_string,
                currencySymbol, CurrencyUtil.formatCurrency(getActivity(),
                        transactions.getAmount())));
        tvPaymentType.setText(paymentDetailData.getPaymentType().getName());
        tvPaymentAccountNumber.setText(paymentDetailData.getAccountNumber());
        tvReceiptNumber.setText(paymentDetailData.getReceiptNumber());
    }

    @Override
    public void showTransactionDetails(Transaction transaction) {
        this.transaction = transaction;
        String currencySymbol = transaction.getCurrency().getDisplaySymbol();

        tvTransactionId.setText(String.valueOf(transaction.getId()));
        tvTransactionType.setText(transaction.getType().getValue());
        tvTransactionDate.setText(DateHelper.getDateAsString(transaction.getDate()));
        tvTransactionCurrency.setText(transaction.getCurrency().getName());
        tvTransactionAmount.setText(getString(R.string.string_and_string,
                currencySymbol, CurrencyUtil.formatCurrency(getActivity(),
                        transaction.getAmount())));
    }

    @Override
    public void showErrorFetchingTransactionDetails(String message) {
        if (!Network.isConnected(getContext())) {
            sweetUIErrorHandler.showSweetNoInternetUI(transactionLayout, layoutError);
            Toast.makeText(getContext(), getString(R.string.internet_not_connected),
                    Toast.LENGTH_SHORT).show();
        } else {
            sweetUIErrorHandler.showSweetErrorUI(message, transactionLayout, layoutError);
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    private void loadTransactionDetails() {
        switch (transactionType) {
            case CLIENT:
                transactionDetailsPresenter.loadClientTransactionDetails(transactionId);
                break;
            case LOAN:
                transactionDetailsPresenter.loadLoanAccountTransactionDetails(accountId,
                        transactionId);
                break;
            case SAVINGS:
                transactionDetailsPresenter.loadSavingsAccountTransactionDetails(accountId,
                        transactionId);
                break;
        }
    }

    @OnClick(R.id.btn_try_again)
    void onRetry() {
        if (!Network.isConnected(getContext())) {
            Toast.makeText(getContext(), getString(R.string.internet_not_connected),
                    Toast.LENGTH_SHORT).show();
        } else {
            sweetUIErrorHandler.hideSweetErrorLayoutUI(transactionLayout, layoutError);
            loadTransactionDetails();
        }
    }

    @Override
    public void showProgress() {
        transactionLayout.setVisibility(View.GONE);
        showProgressBar();
    }

    @Override
    public void hideProgress() {
        transactionLayout.setVisibility(View.VISIBLE);
        hideProgressBar();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        hideProgressBar();
        transactionDetailsPresenter.detachView();
    }

}