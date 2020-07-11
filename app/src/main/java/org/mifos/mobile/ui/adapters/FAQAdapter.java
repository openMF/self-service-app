package org.mifos.mobile.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.mifos.mobile.R;
import org.mifos.mobile.injection.ActivityContext;
import org.mifos.mobile.models.FAQ;
import org.mifos.mobile.utils.FaqDiffUtil;
import org.mifos.mobile.utils.ThemeHelper;

import java.util.ArrayList;

import javax.inject.Inject;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by dilpreet on 12/8/17.
 */

public class FAQAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ArrayList<FAQ> faqArrayList;
    private int alreadySelectedPosition;
    private Context context;
    private ThemeHelper themeHelper;

    @Inject
    public FAQAdapter(@ActivityContext Context context, ThemeHelper themeHelper) {
        faqArrayList = new ArrayList<>();
        this.themeHelper = themeHelper;
        this.context = context;
    }

    public void setFaqArrayList(ArrayList<FAQ> faqArrayList) {
        this.faqArrayList = faqArrayList;
        alreadySelectedPosition = -1;
    }

    public void updateList(ArrayList<FAQ> faqArrayList) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new FaqDiffUtil(this.faqArrayList,
                faqArrayList));
        diffResult.dispatchUpdatesTo(this);
        setFaqArrayList(faqArrayList);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_faq, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        FAQ faq = faqArrayList.get(position);

        ((ViewHolder) holder).tvFaqQs.setText(faq.getQuestion());
        ((ViewHolder) holder).tvFaqQs.setTextColor(themeHelper.getTextColor());
        ((ViewHolder) holder).tvFaqAns.setText(faq.getAnswer());

        if (faq.isSelected()) {
            ((ViewHolder) holder).tvFaqAns.setVisibility(View.VISIBLE);
            ((ViewHolder) holder).ivArrow.setImageDrawable(ContextCompat.getDrawable(context,
                    R.drawable.ic_arrow_up));
        } else {
            ((ViewHolder) holder).tvFaqAns.setVisibility(View.GONE);
            ((ViewHolder) holder).ivArrow.setImageDrawable(ContextCompat.getDrawable(context,
                    R.drawable.ic_arrow_drop_down));
        }

    }

    private void updateView(int position) {
        if (alreadySelectedPosition == position) {
            faqArrayList.get(alreadySelectedPosition).setSelected(false);
            notifyItemChanged(alreadySelectedPosition);
            alreadySelectedPosition = -1;
            return;
        }
        if (alreadySelectedPosition != -1) {
            faqArrayList.get(alreadySelectedPosition).setSelected(false);
            notifyItemChanged(alreadySelectedPosition);
        }

        faqArrayList.get(position).setSelected(true);
        notifyItemChanged(position);
        alreadySelectedPosition = position;
    }

    @Override
    public int getItemCount() {
        return faqArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.tv_qs)
        TextView tvFaqQs;

        @BindView(R.id.tv_ans)
        TextView tvFaqAns;

        @BindView(R.id.ll_faq)
        LinearLayout llFaq;

        @BindView(R.id.iv_arrow)
        ImageView ivArrow;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @OnClick(R.id.ll_faq)
        public void faqHeaderClicked() {
            updateView(getAdapterPosition());
        }
    }
}
