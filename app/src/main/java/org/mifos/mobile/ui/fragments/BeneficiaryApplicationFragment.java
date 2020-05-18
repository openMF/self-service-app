package org.mifos.mobile.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.github.therajanmaurya.sweeterror.SweetUIErrorHandler;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;

import org.mifos.mobile.R;
import org.mifos.mobile.models.beneficiary.Beneficiary;
import org.mifos.mobile.models.beneficiary.BeneficiaryPayload;
import org.mifos.mobile.models.beneficiary.BeneficiaryUpdatePayload;
import org.mifos.mobile.models.templates.beneficiary.AccountTypeOption;
import org.mifos.mobile.models.templates.beneficiary.BeneficiaryTemplate;
import org.mifos.mobile.presenters.BeneficiaryApplicationPresenter;
import org.mifos.mobile.ui.activities.base.BaseActivity;
import org.mifos.mobile.ui.enums.BeneficiaryState;
import org.mifos.mobile.ui.fragments.base.BaseFragment;
import org.mifos.mobile.ui.views.BeneficiaryApplicationView;
import org.mifos.mobile.utils.Constants;
import org.mifos.mobile.utils.Network;
import org.mifos.mobile.utils.Toaster;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by dilpreet on 16/6/17.
 */

public class BeneficiaryApplicationFragment extends BaseFragment implements
        BeneficiaryApplicationView, AdapterView.OnItemSelectedListener {

    @BindView(R.id.sp_account_type)
    Spinner spAccountType;

    @BindView(R.id.ll_application_beneficiary)
    LinearLayout llApplicationBeneficiary;

    @BindView(R.id.til_account_number)
    TextInputLayout tilAccountNumber;

    @BindView(R.id.til_office_name)
    TextInputLayout tilOfficeName;

    @BindView(R.id.til_transfer_limit)
    TextInputLayout tilTransferLimit;

    @BindView(R.id.til_beneficiary_name)
    TextInputLayout tilBeneficiaryName;

    @BindView(R.id.layout_error)
    View layoutError;

    @BindView(R.id.view_flipper)
    NestedScrollView nsvBeneficiary;

    @Inject
    BeneficiaryApplicationPresenter presenter;

    private List<String> listAccountType = new ArrayList<>();
    private ArrayAdapter<String> accountTypeAdapter;

    private BeneficiaryState beneficiaryState;
    private Beneficiary beneficiary;
    private BeneficiaryTemplate beneficiaryTemplate;
    private int accountTypeId = -1;
    private View rootView;
    private SweetUIErrorHandler sweetUIErrorHandler;

    public static BeneficiaryApplicationFragment newInstance(BeneficiaryState beneficiaryState,
            @Nullable Beneficiary beneficiary) {
        BeneficiaryApplicationFragment fragment = new BeneficiaryApplicationFragment();
        Bundle args = new Bundle();
        args.putSerializable(Constants.BENEFICIARY_STATE, beneficiaryState);
        if (beneficiary != null) {
            args.putParcelable(Constants.BENEFICIARY, beneficiary);
        }
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setToolbarTitle(getString(R.string.add_beneficiary));
        if (getArguments() != null) {
            beneficiaryState = (BeneficiaryState) getArguments()
                    .getSerializable(Constants.BENEFICIARY_STATE);
            if (beneficiaryState == BeneficiaryState.UPDATE) {
                beneficiary = getArguments().getParcelable(Constants.BENEFICIARY);
                setToolbarTitle(getString(R.string.update_beneficiary));
            } else if (beneficiaryState == BeneficiaryState.CREATE_QR) {
                beneficiary = getArguments().getParcelable(Constants.BENEFICIARY);
                setToolbarTitle(getString(R.string.add_beneficiary));
            } else {
                setToolbarTitle(getString(R.string.add_beneficiary));
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_beneficiary_application, container, false);
        ((BaseActivity) getActivity()).getActivityComponent().inject(this);
        ButterKnife.bind(this, rootView);
        sweetUIErrorHandler = new SweetUIErrorHandler(getActivity(), rootView);
        showUserInterface();

        presenter.attachView(this);
        if (savedInstanceState == null) {
            presenter.loadBeneficiaryTemplate();
        }
        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(Constants.TEMPLATE, beneficiaryTemplate);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            showBeneficiaryTemplate((BeneficiaryTemplate) savedInstanceState.
                    getParcelable(Constants.TEMPLATE));
        }
    }

    /**
     * Setting up {@code accountTypeAdapter} and {@code} spAccountType
     */
    @Override
    public void showUserInterface() {
        accountTypeAdapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_spinner_item, listAccountType);
        accountTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spAccountType.setOnItemSelectedListener(this);
        if (!Network.isConnected(getContext())) {
            spAccountType.setEnabled(false);
        }
        spAccountType.setAdapter(accountTypeAdapter);
    }

    /**
     * Fetches {@link BeneficiaryTemplate} from server and further updates the UI according to
     * {@link BeneficiaryState} which is initialized in {@code newInstance()} as
     * {@code beneficiaryState}
     *
     * @param beneficiaryTemplate {@link BeneficiaryTemplate} fetched from server
     */
    @Override
    public void showBeneficiaryTemplate(BeneficiaryTemplate beneficiaryTemplate) {
        this.beneficiaryTemplate = beneficiaryTemplate;
        for (AccountTypeOption accountTypeOption : beneficiaryTemplate.getAccountTypeOptions()) {
            listAccountType.add(accountTypeOption.getValue());
        }
        accountTypeAdapter.notifyDataSetChanged();

        if (beneficiaryState == BeneficiaryState.UPDATE) {
            spAccountType.setSelection(accountTypeAdapter.getPosition(beneficiary
                    .getAccountType().getValue()));
            spAccountType.setEnabled(false);
            tilAccountNumber.getEditText().setText(beneficiary.getAccountNumber());
            tilAccountNumber.setEnabled(false);
            tilOfficeName.getEditText().setText(beneficiary.getOfficeName());
            tilOfficeName.setEnabled(false);
            tilBeneficiaryName.getEditText().setText(beneficiary.getName());
            tilTransferLimit.getEditText().setText(String.valueOf(beneficiary.getTransferLimit()));
        } else if (beneficiaryState == BeneficiaryState.CREATE_QR) {
            spAccountType.setSelection(beneficiary.getAccountType().getId());
            tilAccountNumber.getEditText().setText(beneficiary.getAccountNumber());
            tilOfficeName.getEditText().setText(beneficiary.getOfficeName());
        }
    }

    /**
     * Used for submitting a new or updating beneficiary application
     */
    @OnClick(R.id.btn_beneficiary_submit)
    public void submitBeneficiary() {

        tilAccountNumber.setErrorEnabled(false);
        tilOfficeName.setErrorEnabled(false);
        tilTransferLimit.setErrorEnabled(false);
        tilBeneficiaryName.setErrorEnabled(false);

        if (accountTypeId == -1) {
            Toaster.show(rootView, getString(R.string.choose_account_type));
            return;
        } else if (tilAccountNumber.getEditText().getText().toString().trim().equals("")) {
            tilAccountNumber.setError(getString(R.string.enter_account_number));
            return;
        } else if (tilOfficeName.getEditText().getText().toString().trim().equals("")) {
            tilOfficeName.setError(getString(R.string.enter_office_name));
            return;
        } else if (tilTransferLimit.getEditText().getText().toString().equals("")) {
            tilTransferLimit.setError(getString(R.string.enter_transfer_limit));
            return;
        } else if (tilTransferLimit.getEditText().getText().toString().equals(".")) {
            tilTransferLimit.setError(getString(R.string.invalid_amount));
            return;
        } else if (tilTransferLimit.getEditText().getText().toString().matches("^0*")) {
            tilTransferLimit.setError(getString(R.string.amount_greater_than_zero));
            return;
        } else if (tilBeneficiaryName.getEditText().getText().toString().trim().equals("")) {
            tilBeneficiaryName.setError(getString(R.string.enter_beneficiary_name));
            return;
        }

        if (beneficiaryState == BeneficiaryState.CREATE_MANUAL ||
                beneficiaryState == BeneficiaryState.CREATE_QR) {
            submitNewBeneficiaryApplication();
        } else if (beneficiaryState == BeneficiaryState.UPDATE) {
            submitUpdateBeneficiaryApplication();
        }

    }

    @OnClick(R.id.btn_try_again)
    public void onRetry() {
        if (Network.isConnected(getContext())) {
            presenter.loadBeneficiaryTemplate();
            sweetUIErrorHandler.hideSweetErrorLayoutUI(nsvBeneficiary, layoutError);
        } else {
            Toaster.show(rootView, getString(R.string.internet_not_connected));
        }
    }

    /**
     * Submit a new Beneficiary application
     */
    private void submitNewBeneficiaryApplication() {
        BeneficiaryPayload beneficiaryPayload = new BeneficiaryPayload();
        beneficiaryPayload.setAccountNumber(tilAccountNumber.getEditText().getText().toString());
        beneficiaryPayload.setOfficeName(tilOfficeName.getEditText().getText().toString());
        beneficiaryPayload.setAccountType(accountTypeId);
        beneficiaryPayload.setName(tilBeneficiaryName.getEditText().getText().toString());
        beneficiaryPayload.setTransferLimit(Integer.parseInt(tilTransferLimit.getEditText().
                getText().toString()));
        presenter.createBeneficiary(beneficiaryPayload);
    }

    /**
     * Updates an existing beneficiary application
     */
    private void submitUpdateBeneficiaryApplication() {
        BeneficiaryUpdatePayload payload = new BeneficiaryUpdatePayload();
        payload.setName(tilBeneficiaryName.getEditText().getText().toString());
        payload.setTransferLimit(Float.parseFloat(tilTransferLimit.getEditText().getText().
                toString()));
        presenter.updateBeneficiary(beneficiary.getId(), payload);
    }

    /**
     * Displays a {@link Snackbar} on successfully creation of
     * Beneficiary and pops fragments in order to go back to {@link BeneficiaryListFragment}
     */
    @Override
    public void showBeneficiaryCreatedSuccessfully() {
        Toaster.show(rootView, getString(R.string.beneficiary_created_successfully));
        getActivity().finish();
    }

    /**
     * Displays a {@link Snackbar} on successfully updation of
     * Beneficiary and pops fragments in order to go back to {@link BeneficiaryListFragment}
     */
    @Override
    public void showBeneficiaryUpdatedSuccessfully() {
        Toaster.show(rootView, getString(R.string.beneficiary_updated_successfully));
        getActivity().getSupportFragmentManager().popBackStack();
        getActivity().getSupportFragmentManager().popBackStack();

    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        accountTypeId = beneficiaryTemplate.getAccountTypeOptions().get(position).getId();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) { }

    /**
     * It is called whenever any error occurs while executing a request
     *
     * @param msg Error message that tells the user about the problem.
     */
    @Override
    public void showError(String msg) {
        if (!Network.isConnected(getContext())) {
            sweetUIErrorHandler.showSweetNoInternetUI(nsvBeneficiary, layoutError);
        } else {
            sweetUIErrorHandler.showSweetErrorUI(msg, nsvBeneficiary, layoutError);
            Toaster.show(rootView, msg);
        }
    }

    @Override
    public void setVisibility(int state) {
        llApplicationBeneficiary.setVisibility(state);
    }

    @Override
    public void showProgress() {
        showProgressBar();
    }

    @Override
    public void hideProgress() {
        hideProgressBar();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        hideProgress();
        presenter.detachView();
    }
}
