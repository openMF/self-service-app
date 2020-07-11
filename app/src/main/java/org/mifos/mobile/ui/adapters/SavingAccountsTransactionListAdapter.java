package org.mifos.mobile.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.mifos.mobile.R;
import org.mifos.mobile.models.accounts.savings.TransactionType;
import org.mifos.mobile.models.accounts.savings.Transactions;
import org.mifos.mobile.utils.CurrencyUtil;
import org.mifos.mobile.utils.DateHelper;
import org.mifos.mobile.utils.ThemeHelper;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by dilpreet on 6/3/17.
 */

public class SavingAccountsTransactionListAdapter extends
        RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Transactions> savingAccountsTransactionList;
    private Context context;
    private ThemeHelper themeHelper;

    @Inject
    public SavingAccountsTransactionListAdapter(ThemeHelper themeHelper) {
        savingAccountsTransactionList = new ArrayList<>();
        this.themeHelper = themeHelper;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public Transactions getItem(int position) {
        return savingAccountsTransactionList.get(position);
    }

    public void setSavingAccountsTransactionList(List<Transactions> savingAccountsTransactionList) {
        this.savingAccountsTransactionList = savingAccountsTransactionList;
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        View v = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.row_saving_account_transaction, parent, false);
        vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {


        Transactions transaction = getItem(position);

        ((ViewHolder) holder).tvSavingAccountAmount.setText(context.getString(R.string.
                string_and_string, transaction.getCurrency().getDisplaySymbol(), CurrencyUtil.
                formatCurrency(context, transaction.getAmount())));

        ((ViewHolder) holder).tvSavingAccountAmount.setTextColor(themeHelper.getTextColor());

        ((ViewHolder) holder).tvSavingAccountRunningBalance.setText(context.getString(R.string.
                string_and_string, transaction.getCurrency().getDisplaySymbol(), CurrencyUtil.
                formatCurrency(context, transaction.getRunningBalance())));

        ((ViewHolder) holder).tvTransactionType.setText(transaction.
                getTransactionType().getValue());

        if (transaction.getPaymentDetailData() != null) {
            ((ViewHolder) holder).tvTransactionDetailData.setVisibility(View.VISIBLE);
            ((ViewHolder) holder).tvTransactionDetailData.setText(transaction.
                    getPaymentDetailData().getPaymentType().getName());
        }

        ((ViewHolder) holder).tvTransactionDate.setText(DateHelper.
                getDateAsString(transaction.getDate()));
        ((ViewHolder) holder).tvTransactionDate.setTextColor(themeHelper.getTextColor());

        ColorSelect color = getColor(transaction.getTransactionType());
        if (color == ColorSelect.RED) {
            ((ViewHolder) holder).vIndicator.setRotation(180);
            ((ViewHolder) holder).vIndicator.setBackgroundDrawable(
                    ContextCompat.getDrawable(context, R.drawable.triangular_red_view));
        } else {
            ((ViewHolder) holder).vIndicator.setRotation(0);
            ((ViewHolder) holder).vIndicator.setBackgroundDrawable(
                    ContextCompat.getDrawable(context, R.drawable.triangular_green_view));
        }

    }

    private enum ColorSelect {
        RED,
        GREEN
    }

    @Override
    public int getItemCount() {
        return savingAccountsTransactionList.size();
    }

    private ColorSelect getColor(TransactionType transactionType) {
        if (transactionType.getDeposit()) {
            return ColorSelect.GREEN;
        }
        if (transactionType.getDividendPayout()) {
            return ColorSelect.RED;
        }
        if (transactionType.getWithdrawal()) {
            return ColorSelect.RED;
        }
        if (transactionType.getInterestPosting()) {
            return ColorSelect.GREEN;
        }
        if (transactionType.getFeeDeduction()) {
            return ColorSelect.RED;
        }
        if (transactionType.getInitiateTransfer()) {
            return ColorSelect.RED;
        }
        if (transactionType.getApproveTransfer()) {
            return ColorSelect.RED;
        }
        if (transactionType.getWithdrawTransfer()) {
            return ColorSelect.RED;
        }
        if (transactionType.getRejectTransfer()) {
            return ColorSelect.GREEN;
        }
        if (transactionType.getOverdraftFee()) {
            return ColorSelect.RED;
        }

        return ColorSelect.GREEN;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.tv_transaction_date)
        TextView tvTransactionDate;

        @BindView(R.id.tv_transaction_type)
        TextView tvTransactionType;

        @BindView(R.id.tv_transaction_detail_data)
        TextView tvTransactionDetailData;

        @BindView(R.id.tv_savingAccountAmount)
        TextView tvSavingAccountAmount;

        @BindView(R.id.tv_saving_account_running_balance)
        TextView tvSavingAccountRunningBalance;

        @BindView(R.id.v_indicator)
        View vIndicator;

        public ViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
        }
    }
}