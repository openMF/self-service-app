package org.mifos.mobile.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.mifos.mobile.R;
import org.mifos.mobile.injection.ActivityContext;
import org.mifos.mobile.models.accounts.savings.SavingAccount;
import org.mifos.mobile.utils.CurrencyUtil;
import org.mifos.mobile.utils.DateHelper;
import org.mifos.mobile.utils.ThemeHelper;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author Vishwajeet
 * @since 22/6/16.
 */
public class SavingAccountsListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final Context context;
    private ThemeHelper themeHelper;

    private List<SavingAccount> savingAccountsList = new ArrayList<>();

    @Inject
    public SavingAccountsListAdapter(@ActivityContext Context context, ThemeHelper themeHelper) {
        this.context = context;
        this.themeHelper = themeHelper;
    }

    public void setSavingAccountsList(List<SavingAccount> savingAccountsList) {
        this.savingAccountsList = savingAccountsList;
        notifyDataSetChanged();
    }

    public List<SavingAccount> getSavingAccountsList() {
        return savingAccountsList;
    }

    public SavingAccount getItem(int position) {
        return savingAccountsList.get(position);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        View v = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.row_saving_account, parent, false);
        vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ViewHolder) {

            SavingAccount savingAccount = getItem(position);

            ((ViewHolder) holder).tvClientSavingAccountNumber.setText(
                    savingAccount.getAccountNo());

            ((ViewHolder) holder).tvClientSavingAccountNumber.setTextColor(
                    themeHelper.getTextColor());

            ((ViewHolder) holder).tvSavingAccountProductName.setText(
                    savingAccount.getProductName());
            ((ViewHolder) holder).tvAccountBalance.setVisibility(View.GONE);

            if (savingAccount.getStatus().getActive()) {

                setSavingAccountsDetails(((ViewHolder) holder), savingAccount,
                        R.color.deposit_green);
                setSavingAccountsGeneralDetails(holder, R.color.deposit_green, DateHelper.
                        getDateAsString(savingAccount.getLastActiveTransactionDate()));

            } else if (savingAccount.getStatus().getApproved()) {

                setSavingAccountsGeneralDetails(holder, R.color.light_green, context.getString(R.
                        string.string_and_string, context.getString(R.string.approved), DateHelper.
                        getDateAsString(savingAccount.getTimeLine().getApprovedOnDate())));

            } else if (savingAccount.getStatus().getSubmittedAndPendingApproval()) {

                setSavingAccountsGeneralDetails(holder, R.color.light_yellow, context.getString(R.
                        string.string_and_string, context.getString(R.string.submitted), DateHelper.
                        getDateAsString(savingAccount.getTimeLine().getSubmittedOnDate())));

            } else if (savingAccount.getStatus().getMatured()) {

                setSavingAccountsDetails(((ViewHolder) holder), savingAccount, R.color.red_light);
                setSavingAccountsGeneralDetails(holder, R.color.red_light, DateHelper.
                        getDateAsString(savingAccount.getLastActiveTransactionDate()));

            } else {

                setSavingAccountsGeneralDetails(holder, R.color.black, context.getString(R.string.
                        string_and_string, context.getString(R.string.closed), DateHelper.
                        getDateAsString(savingAccount.getTimeLine().getClosedOnDate())));

            }
        }

    }

    private void setSavingAccountsDetails(ViewHolder viewHolder, SavingAccount savingAccount,
            int colorId) {
        viewHolder.tvAccountBalance.setVisibility(View.VISIBLE);
        viewHolder.tvAccountBalance.setTextColor(ContextCompat.getColor(context,
                colorId));
        viewHolder.tvAccountBalance.setText(context.getString(R.string.string_and_string,
                savingAccount.getCurrency().getDisplaySymbol(), CurrencyUtil.formatCurrency(context,
                        savingAccount.getAccountBalance())));
    }

    private void setSavingAccountsGeneralDetails(RecyclerView.ViewHolder holder, int colorId,
            String dateStr) {
        ((ViewHolder) holder).ivStatusIndicator.setColorFilter(ContextCompat.
                getColor(context, colorId));
        ((ViewHolder) holder).tvLastActive.setText(dateStr);
    }

    @Override
    public int getItemCount() {
        return savingAccountsList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.tv_clientSavingAccountNumber)
        TextView tvClientSavingAccountNumber;

        @BindView(R.id.tv_savingAccountProductName)
        TextView tvSavingAccountProductName;

        @BindView(R.id.iv_status_indicator)
        AppCompatImageView ivStatusIndicator;

        @BindView(R.id.tv_last_active)
        TextView tvLastActive;

        @BindView(R.id.tv_account_balance)
        TextView tvAccountBalance;

        public ViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
        }
    }
}