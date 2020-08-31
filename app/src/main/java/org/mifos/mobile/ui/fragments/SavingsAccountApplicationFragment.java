package org.mifos.mobile.ui.fragments;

/*
 * Created by saksham on 30/June/2018
 */

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.github.therajanmaurya.sweeterror.SweetUIErrorHandler;

import org.mifos.mobile.R;
import org.mifos.mobile.api.local.PreferencesHelper;
import org.mifos.mobile.models.accounts.savings.SavingsAccountApplicationPayload;
import org.mifos.mobile.models.accounts.savings.SavingsAccountUpdatePayload;
import org.mifos.mobile.models.accounts.savings.SavingsWithAssociations;
import org.mifos.mobile.models.templates.savings.ProductOptions;
import org.mifos.mobile.models.templates.savings.SavingsAccountTemplate;
import org.mifos.mobile.presenters.SavingsAccountApplicationPresenter;
import org.mifos.mobile.ui.activities.base.BaseActivity;
import org.mifos.mobile.ui.enums.SavingsAccountState;
import org.mifos.mobile.ui.fragments.base.BaseFragment;
import org.mifos.mobile.ui.views.SavingsAccountApplicationView;
import org.mifos.mobile.utils.Constants;
import org.mifos.mobile.utils.DateHelper;
import org.mifos.mobile.utils.MFDatePicker;
import org.mifos.mobile.utils.Network;
import org.mifos.mobile.utils.Toaster;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SavingsAccountApplicationFragment extends BaseFragment
        implements SavingsAccountApplicationView {

    @BindView(R.id.sp_product_id)
    Spinner spProductId;

    @BindView(R.id.tv_submission_date)
    TextView tvSubmissionDate;

    @BindView(R.id.tv_client_name)
    TextView tvClientName;

    @BindView(R.id.ll_savings_application)
    CardView layoutSavingsApplication;

    @BindView(R.id.layout_error)
    View layoutError;

    @Inject
    SavingsAccountApplicationPresenter presenter;

    @Inject
    PreferencesHelper preferencesHelper;

    private View rootView;
    private SavingsAccountState state;
    private SavingsWithAssociations savingsWithAssociations;
    private ArrayAdapter<String> productIdAdapter;
    private SavingsAccountTemplate template;
    private List<ProductOptions> productOptions;
    private List<String> productIdList = new ArrayList<>();
    private SweetUIErrorHandler sweetUIErrorHandler;

    public static SavingsAccountApplicationFragment newInstance(
            SavingsAccountState state, SavingsWithAssociations savingsWithAssociations) {
        SavingsAccountApplicationFragment fragment = new SavingsAccountApplicationFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(Constants.SAVINGS_ACCOUNT_STATE, state);
        bundle.putParcelable(Constants.SAVINGS_ACCOUNTS, savingsWithAssociations);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            state = (SavingsAccountState) getArguments()
                    .getSerializable(Constants.SAVINGS_ACCOUNT_STATE);
            savingsWithAssociations = getArguments().getParcelable(Constants.SAVINGS_ACCOUNTS);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_savings_account_application, container,
                false);
        ButterKnife.bind(this, rootView);
        ((BaseActivity) getActivity()).getActivityComponent().inject(this);

        presenter.attachView(this);
        presenter.loadSavingsAccountApplicationTemplate(preferencesHelper.getClientId(), state);

        sweetUIErrorHandler = new SweetUIErrorHandler(getContext(), rootView);

        return rootView;
    }

    @Override
    public void showUserInterfaceSavingAccountApplication(SavingsAccountTemplate template) {
        showUserInterface(template);
    }

    @Override
    public void showSavingsAccountApplicationSuccessfully() {
        showMessage(getString(R.string.new_saving_account_created_successfully));
        getActivity().finish();
    }

    @Override
    public void showUserInterfaceSavingAccountUpdate(SavingsAccountTemplate template) {
        showUserInterface(template);
        getActivity().setTitle(getString(R.string.string_savings_account,
                getString(R.string.update)));
        spProductId.setSelection(productIdAdapter.getPosition(savingsWithAssociations
                .getSavingsProductName()));
    }

    @Override
    public void showSavingsAccountUpdateSuccessfully() {
        showMessage(getString(R.string.saving_account_updated_successfully));
        getActivity().getSupportFragmentManager().popBackStack();
    }

    public void showUserInterface(SavingsAccountTemplate template) {
        this.template = template;
        productOptions = template.getProductOptions();
        for (ProductOptions item : productOptions) {
            productIdList.add(item.getName());
        }

        tvClientName.setText(template.getClientName());

        productIdAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item,
                productIdList);
        productIdAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spProductId.setAdapter(productIdAdapter);

        tvSubmissionDate.setText(MFDatePicker.getDatePickedAsString());
    }

    public void submitSavingsAccountApplication() {
        SavingsAccountApplicationPayload payload = new SavingsAccountApplicationPayload();
        payload.setClientId(template.getClientId());
        payload.setProductId(productOptions.get(spProductId.getSelectedItemPosition()).getId());
        payload.setSubmittedOnDate(DateHelper.getSpecificFormat(DateHelper.FORMAT_dd_MMMM_yyyy,
                MFDatePicker.getDatePickedAsString()));
        presenter.submitSavingsAccountApplication(payload);
    }

    public void updateSavingAccount() {
        SavingsAccountUpdatePayload payload = new SavingsAccountUpdatePayload();
        payload.setClientId(template.getClientId());
        payload.setProductId(productOptions.get(spProductId.getSelectedItemPosition()).getId());
        presenter.updateSavingsAccount(savingsWithAssociations.getAccountNo(), payload);
    }

    @OnClick(R.id.btn_submit)
    void onSubmit() {
        if (state == SavingsAccountState.CREATE) {
            submitSavingsAccountApplication();
        } else {
            updateSavingAccount();
        }
    }

    @Override
    public void showError(String error) {
        if (!Network.isConnected(getContext())) {
            sweetUIErrorHandler.showSweetNoInternetUI(layoutSavingsApplication, layoutError);
        } else {
            sweetUIErrorHandler.showSweetErrorUI(error, layoutSavingsApplication, layoutError);
        }
    }

    @OnClick(R.id.btn_try_again)
    void onRetry() {
        if (!Network.isConnected(getContext())) {
            Toaster.show(rootView, R.string.internet_not_connected);
        } else {
            sweetUIErrorHandler.hideSweetErrorLayoutUI(layoutSavingsApplication, layoutError);
            presenter.loadSavingsAccountApplicationTemplate(preferencesHelper.getClientId(), state);
        }
    }

    @Override
    public void showMessage(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
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
        hideProgress();
        presenter.detachView();
    }
}
