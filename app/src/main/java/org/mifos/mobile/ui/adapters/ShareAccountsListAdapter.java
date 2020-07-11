package org.mifos.mobile.ui.adapters;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.mifos.mobile.R;
import org.mifos.mobile.injection.ActivityContext;
import org.mifos.mobile.models.accounts.share.ShareAccount;
import org.mifos.mobile.utils.ThemeHelper;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;

public class ShareAccountsListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final Context context;
    private ThemeHelper themeHelper;
    private List<ShareAccount> shareAccountsList = new ArrayList<>();

    @Inject
    public ShareAccountsListAdapter(@ActivityContext Context context, ThemeHelper themeHelper) {
        this.context = context;
        this.themeHelper = themeHelper;
    }

    public void setShareAccountsList(List<ShareAccount> shareAccountsList) {
        this.shareAccountsList = shareAccountsList;
        notifyDataSetChanged();
    }

    public ShareAccount getItem(int position) {
        return shareAccountsList.get(position);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        View v = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.row_share_account, parent, false);
        vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ViewHolder) {

            ShareAccount shareAccount = getItem(position);
            ((ViewHolder) holder).tvClientShareAccountsNumber
                    .setText(shareAccount.getAccountNo());
            ((ViewHolder) holder).tvClientShareAccountsNumber
                    .setTextColor(themeHelper.getTextColor());
            ((ViewHolder) holder).tvShareAccountsProductName
                    .setText(shareAccount.getProductName());
            ((ViewHolder) holder).llAccountDetail.setVisibility(View.GONE);

            if (shareAccount.getStatus().getActive()) {

                ((ViewHolder) holder).ivStatusIndicator.setColorFilter(ContextCompat.
                        getColor(context, R.color.deposit_green));
                setSharingAccountDetail(((ViewHolder) holder), shareAccount);

            } else if (shareAccount.getStatus().getApproved()) {

                ((ViewHolder) holder).ivStatusIndicator.setColorFilter(ContextCompat.
                        getColor(context, R.color.light_green));

            } else if (shareAccount.getStatus().getSubmittedAndPendingApproval()) {

                ((ViewHolder) holder).ivStatusIndicator.setColorFilter(ContextCompat.
                        getColor(context, R.color.light_yellow));

            } else {

                ((ViewHolder) holder).ivStatusIndicator.setColorFilter(ContextCompat.
                        getColor(context, R.color.light_blue));

            }
        }
    }

    private void setSharingAccountDetail(ViewHolder viewHolder, ShareAccount shareAccount) {
        viewHolder.llAccountDetail.setVisibility(View.VISIBLE);
        viewHolder.tvSharesPending.setText(String.valueOf(shareAccount
                .getTotalPendingForApprovalShares()));
        viewHolder.tvSharesApproved.setText(String.valueOf(shareAccount
                .getTotalApprovedShares()));
    }

    @Override
    public int getItemCount() {
        return shareAccountsList.size();
    }

    private LayerDrawable setCircularBackground(int colorId) {
        Drawable color = new ColorDrawable(ContextCompat.getColor(context, colorId));
        Drawable image = ContextCompat.getDrawable(context, R.drawable.circular_background);
        LayerDrawable ld = new LayerDrawable(new Drawable[]{image, color});
        return ld;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.tv_clientSharingAccountNumber)
        TextView tvClientShareAccountsNumber;

        @BindView(R.id.tv_shareAccountProductName)
        TextView tvShareAccountsProductName;

        @BindView(R.id.iv_status_indicator)
        AppCompatImageView ivStatusIndicator;

        @BindView(R.id.ll_account_detail)
        LinearLayout llAccountDetail;

        @BindView(R.id.tv_shares_pending)
        TextView tvSharesPending;

        @BindView(R.id.tv_shares_approved)
        TextView tvSharesApproved;

        public ViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
        }
    }
}
