package org.mifos.mobile.ui.fragments;

/*
 * Created by saksham on 02/July/2018
 */

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputLayout;

import org.mifos.mobile.R;
import org.mifos.mobile.models.accounts.savings.SavingsAccountWithdrawPayload;
import org.mifos.mobile.models.accounts.savings.SavingsWithAssociations;
import org.mifos.mobile.presenters.SavingsAccountWithdrawPresenter;
import org.mifos.mobile.ui.activities.base.BaseActivity;
import org.mifos.mobile.ui.fragments.base.BaseFragment;
import org.mifos.mobile.ui.views.SavingsAccountWithdrawView;
import org.mifos.mobile.utils.Constants;
import org.mifos.mobile.utils.MFDatePicker;
import org.mifos.mobile.utils.Toaster;

import javax.inject.Inject;

import androidx.annotation.Nullable;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SavingsAccountWithdrawFragment extends BaseFragment
        implements SavingsAccountWithdrawView {

    @BindView(R.id.tv_client_name)
    TextView tvClientName;

    @BindView(R.id.tv_account_number)
    TextView tvAccountNumber;

    @BindView(R.id.tv_withdrawal_date)
    TextView tvWithdrawalDate;

    @BindView(R.id.til_remark)
    TextInputLayout tilRemark;

    @Inject
    SavingsAccountWithdrawPresenter presenter;

    private View rootView;
    private SavingsWithAssociations savingsWithAssociations;
    private SavingsAccountWithdrawPayload payload;

    public static SavingsAccountWithdrawFragment newInstance(
            SavingsWithAssociations savingsWithAssociations) {
        SavingsAccountWithdrawFragment fragment = new SavingsAccountWithdrawFragment();

        Bundle bundle = new Bundle();
        bundle.putParcelable(Constants.SAVINGS_ACCOUNTS, savingsWithAssociations);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            savingsWithAssociations = getArguments().getParcelable(Constants.SAVINGS_ACCOUNTS);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_savings_account_withdraw_fragment,
                container, false);
        ButterKnife.bind(this, rootView);
        ((BaseActivity) getActivity()).getActivityComponent().inject(this);
        presenter.attachView(this);
        showUserInterface();
        return rootView;
    }

    @OnClick(R.id.btn_withdraw_savings_account)
    void onWithdrawSavingsAccount() {
        submitWithdrawSavingsAccount();
    }

    @Override
    public void showUserInterface() {
        getActivity().setTitle(getString(R.string.withdraw_savings_account));
        tvAccountNumber.setText(savingsWithAssociations.getAccountNo());
        tvClientName.setText(savingsWithAssociations.getClientName());
        tvWithdrawalDate.setText(MFDatePicker.getDatePickedAsString());
    }

    public boolean isFormIncomplete() {
        boolean rv = false;
        if (tilRemark.getEditText().getText().toString().trim().length() == 0) {
            rv = true;
            tilRemark.setError(getString(R.string.error_validation_blank,
                    getString(R.string.remark)));
        }
        return rv;
    }

    public void submitWithdrawSavingsAccount() {
        tilRemark.setErrorEnabled(false);
        if (!isFormIncomplete()) {
            payload = new SavingsAccountWithdrawPayload();
            payload.setNote(tilRemark.getEditText().getText().toString());
            payload.setWithdrawnOnDate(MFDatePicker.getDatePickedAsString());
            presenter.submitWithdrawSavingsAccount(savingsWithAssociations.getAccountNo(), payload);
        }
    }

    @Override
    public void showSavingsAccountWithdrawSuccessfully() {
        showMessage(getString(R.string.savings_account_withdraw_successful));
        getActivity().getSupportFragmentManager().popBackStack();
    }

    @Override
    public void showMessage(String message) {
        Toaster.show(rootView, message);
    }

    @Override
    public void showError(String error) {
        Toaster.show(rootView, error);
    }

    @Override
    public void showProgress() {
        showMifosProgressDialog(getString(R.string.progress_message_loading));
    }

    @Override
    public void hideProgress() {
        hideMifosProgressDialog();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        presenter.detachView();
    }
}
