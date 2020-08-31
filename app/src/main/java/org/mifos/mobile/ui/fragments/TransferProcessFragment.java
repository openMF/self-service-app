package org.mifos.mobile.ui.fragments;

import android.graphics.drawable.Animatable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;

import org.mifos.mobile.R;
import org.mifos.mobile.models.payload.TransferPayload;
import org.mifos.mobile.presenters.TransferProcessPresenter;
import org.mifos.mobile.ui.activities.SavingsAccountContainerActivity;
import org.mifos.mobile.ui.activities.base.BaseActivity;
import org.mifos.mobile.ui.enums.TransferType;
import org.mifos.mobile.ui.fragments.base.BaseFragment;
import org.mifos.mobile.ui.views.TransferProcessView;
import org.mifos.mobile.utils.Constants;
import org.mifos.mobile.utils.CurrencyUtil;
import org.mifos.mobile.utils.Network;
import org.mifos.mobile.utils.Toaster;

import javax.inject.Inject;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by dilpreet on 1/7/17.
 */

public class TransferProcessFragment extends BaseFragment implements TransferProcessView {

    @BindView(R.id.tv_amount)
    TextView tvAmount;

    @BindView(R.id.tv_pay_to)
    TextView tvPayTo;

    @BindView(R.id.tv_pay_from)
    TextView tvPayFrom;

    @BindView(R.id.tv_date)
    TextView tvDate;

    @BindView(R.id.tv_remark)
    TextView tvRemark;

    @BindView(R.id.iv_success)
    ImageView ivSuccess;

    @BindView(R.id.ll_transfer)
    LinearLayout llTransfer;

    @BindView(R.id.btn_close)
    AppCompatButton btnClose;

    @Inject
    TransferProcessPresenter presenter;

    private View rootView;
    private TransferPayload payload;
    private TransferType transferType;

    /**
     * Used for TPT Transfer and own Account Transfer.<br>
     * Use {@code type} as TransferType.TPT for TPT and TransferType.SELF for self Account Transfer
     *
     * @param payload Transfer Information
     * @param type    enum of {@link TransferType}
     * @return Instance of {@link TransferProcessFragment}
     */
    public static TransferProcessFragment newInstance(TransferPayload payload, TransferType type) {
        TransferProcessFragment fragment = new TransferProcessFragment();
        Bundle args = new Bundle();
        args.putParcelable(Constants.PAYLOAD, payload);
        args.putSerializable(Constants.TRANSFER_TYPE, type);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getActivity() != null) {
            payload = getArguments().getParcelable(Constants.PAYLOAD);
            transferType = (TransferType) getArguments().getSerializable(Constants.TRANSFER_TYPE);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_transfer, container, false);
        ((BaseActivity) getActivity()).getActivityComponent().inject(this);
        setToolbarTitle(getString(R.string.transfer));

        ButterKnife.bind(this, rootView);
        presenter.attachView(this);

        tvAmount.setText(CurrencyUtil.formatCurrency(getActivity(), payload.getTransferAmount()));
        tvPayFrom.setText(String.valueOf(payload.getFromAccountNumber()));
        tvPayTo.setText(String.valueOf(payload.getToAccountNumber()));
        tvDate.setText(payload.getTransferDate());
        tvRemark.setText(payload.getTransferDescription());

        return rootView;
    }

    /**
     * Initiates a transfer depending upon {@code transferType}
     */
    @OnClick(R.id.btn_start_transfer)
    public void startTransfer() {
        if (!Network.isConnected(getActivity())) {
            Toaster.show(rootView, getString(R.string.internet_not_connected));
            return;
        }
        if (transferType == TransferType.SELF) {
            presenter.makeSavingsTransfer(payload);
        } else if (transferType == TransferType.TPT) {
            presenter.makeTPTTransfer(payload);
        }
    }

    /**
     * Cancels the Transfer and pops fragment
     */
    @OnClick(R.id.btn_cancel_transfer)
    public void cancelTransfer() {
        Toaster.cancelTransfer(rootView, getString(R.string.cancel_transfer),
                getString(R.string.yes), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        getActivity().getSupportFragmentManager().popBackStack();
                        getActivity().getSupportFragmentManager().popBackStack();
                    }
                });
    }

    /**
     * Closes the transfer fragment
     */
    @OnClick(R.id.btn_close)
    public void closeClicked() {
        getActivity().getSupportFragmentManager().popBackStack();
        getActivity().getSupportFragmentManager().popBackStack();
    }

    /**
     * Shows a {@link Snackbar} on succesfull transfer of money
     */
    @Override
    public void showTransferredSuccessfully() {
        Toaster.show(rootView, getString(R.string.transferred_successfully));
        ivSuccess.setVisibility(View.VISIBLE);
        ((Animatable) ivSuccess.getDrawable()).start();
        btnClose.setVisibility(View.VISIBLE);
        llTransfer.setVisibility(View.GONE);
        SavingsAccountContainerActivity.transferSuccess = true;
    }

    /**
     * It is called whenever any error occurs while executing a request
     *
     * @param msg Error message that tells the user about the problem.
     */
    @Override
    public void showError(String msg) {
        Toaster.show(rootView, msg);
    }

    @Override
    public void showProgress() {
        showMifosProgressDialog(getString(R.string.please_wait));
    }

    @Override
    public void hideProgress() {
        hideMifosProgressDialog();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        presenter.detachView();
    }
}
