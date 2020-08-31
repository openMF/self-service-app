package org.mifos.mobile.ui.adapters;

/*
 * Created by saksham on 18/June/2018
 */

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.mifos.mobile.R;
import org.mifos.mobile.models.beneficiary.BeneficiaryDetail;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import butterknife.BindView;
import butterknife.ButterKnife;

public class BeneficiarySpinnerAdapter extends ArrayAdapter<String> {

    @BindView(R.id.tv_account_number)
    TextView tvAccountNumber;

    @BindView(R.id.tv_beneficiary_name)
    TextView tvBeneficiaryName;

    int resource;
    LayoutInflater layoutInflater;
    Context context;
    List<BeneficiaryDetail> list;

    public BeneficiarySpinnerAdapter(Context context, int resource, List list) {
        super(context, resource, 0, list);
        this.resource = resource;
        this.context = context;
        this.list = list;
        layoutInflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return createItemView(position, convertView, parent);
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView,
                                @NonNull ViewGroup parent) {
        return createItemView(position, convertView, parent);
    }

    public View createItemView(int position, View convertView, ViewGroup parent) {
        View view = layoutInflater.inflate(resource, parent, false);
        ButterKnife.bind(this, view);

        tvAccountNumber.setText(list.get(position).getName());
        tvBeneficiaryName.setText(list.get(position).getDescription());
        return view;
    }

    @Nullable
    @Override
    public String getItem(int position) {
        return list.get(position).getName();
    }
}