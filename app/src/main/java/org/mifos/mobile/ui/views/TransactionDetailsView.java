package org.mifos.mobile.ui.views;

import org.mifos.mobile.models.Transaction;
import org.mifos.mobile.models.accounts.savings.Transactions;
import org.mifos.mobile.ui.views.base.MVPView;

public interface TransactionDetailsView extends MVPView {

    // used to show Client and Loan Account Transaction details
    void showTransactionDetails(Transaction transaction);

    void showSavingsAccountTransactionDetails(Transactions transactions);

    void showErrorFetchingTransactionDetails(String message);
}