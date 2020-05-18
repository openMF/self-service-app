package org.mifos.mobile.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import com.github.therajanmaurya.sweeterror.SweetUIErrorHandler;

import org.mifos.mobile.R;
import org.mifos.mobile.models.Charge;
import org.mifos.mobile.presenters.ChargeDetailsPresenter;
import org.mifos.mobile.ui.activities.base.BaseActivity;
import org.mifos.mobile.ui.enums.ChargeType;
import org.mifos.mobile.ui.fragments.base.BaseFragment;
import org.mifos.mobile.ui.views.ChargeDetailsView;
import org.mifos.mobile.utils.Constants;
import org.mifos.mobile.utils.Network;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class ChargeDetailsFragment extends BaseFragment implements ChargeDetailsView {

    @BindView(R.id.ll_charge)
    LinearLayout chargeLayout;

    @BindView(R.id.layout_error)
    View layoutError;

    @BindView(R.id.tv_name)
    TextView tvName;

    @BindView(R.id.tv_charge_type)
    TextView tvChargeType;

    @BindView(R.id.tv_currency)
    TextView tvCurrency;

    @BindView(R.id.tv_payment_at)
    TextView tvPaymentDueAt;

    @BindView(R.id.tv_payment_due)
    TextView tvPaymentDueAsOf;

    @BindView(R.id.tv_calculation_type)
    TextView tvCalculationType;

    @BindView(R.id.tv_due)
    TextView tvDue;

    @BindView(R.id.tv_paid)
    TextView tvPaid;

    @BindView(R.id.tv_waived)
    TextView tvWaived;

    @BindView(R.id.tv_outstanding)
    TextView tvOutstanding;

    @Inject
    ChargeDetailsPresenter chargeDetailsPresenter;


    private long id;
    private int chargeId;
    private ChargeType chargeType;
    private View rootView;
    private Charge charge;
    private SweetUIErrorHandler sweetUIErrorHandler;

    public static ChargeDetailsFragment newInstance(
            long id, int chargeId, ChargeType chargeType) {
        ChargeDetailsFragment fragment = new ChargeDetailsFragment();
        Bundle args = new Bundle();
        args.putLong(Constants.CLIENT_ID, id);
        args.putInt(Constants.CHARGE_ID, chargeId);
        args.putSerializable(Constants.CHARGE_TYPE, chargeType);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            id = getArguments().getLong(Constants.CLIENT_ID);
            chargeId = getArguments().getInt(Constants.CHARGE_ID);
            chargeType = (ChargeType) getArguments().getSerializable(Constants.CHARGE_TYPE);
        }
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_charge_details, container, false);
        ((BaseActivity) getActivity()).getActivityComponent().inject(this);
        setToolbarTitle(getString(R.string.charge_detail));
        ButterKnife.bind(this, rootView);
        chargeDetailsPresenter.attachView(this);
        sweetUIErrorHandler = new SweetUIErrorHandler(getContext(), rootView);

        if (savedInstanceState == null) {
            loadChargeDetails();
        }
        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(Constants.CHARGE, charge);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            showChargeDetails((Charge) savedInstanceState.
                    getParcelable(Constants.CHARGE));
        }
    }

    @Override
    public void showChargeDetails(Charge charge) {
        tvName.setText(charge.getName());
        tvChargeType.setText(charge.getChargeCalculationType().getValue());


    }

    @Override
    public void showErrorFetchingChargeDetails(String message) {
        if (!Network.isConnected(getContext())) {
            sweetUIErrorHandler.showSweetNoInternetUI(chargeLayout, layoutError);
            Toast.makeText(getContext(), getString(R.string.internet_not_connected),
                    Toast.LENGTH_SHORT).show();
        } else {
            sweetUIErrorHandler.showSweetErrorUI(message, chargeLayout, layoutError);
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }


    @OnClick(R.id.btn_try_again)
    void onRetry() {
        if (!Network.isConnected(getContext())) {
            Toast.makeText(getContext(), getString(R.string.internet_not_connected),
                    Toast.LENGTH_SHORT).show();
        } else {
            sweetUIErrorHandler.hideSweetErrorLayoutUI(chargeLayout, layoutError);
            loadChargeDetails();
        }
    }

    private void loadChargeDetails() {
        switch (chargeType) {
            case CLIENT:
                chargeDetailsPresenter.loadClientChargeDetails(chargeId);
                break;
            case LOAN:
                chargeDetailsPresenter.loadLoanChargeDetails(id, chargeId);
                break;
            case SAVINGS:
                chargeDetailsPresenter.loadSavingsChargeDetails(id, chargeId);
                break;
        }

    }

    @Override
    public void showProgress() {
        chargeLayout.setVisibility(View.GONE);
        showProgressBar();
    }

    @Override
    public void hideProgress() {
        chargeLayout.setVisibility(View.VISIBLE);
        hideProgressBar();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        hideProgressBar();
        chargeDetailsPresenter.detachView();
    }

}