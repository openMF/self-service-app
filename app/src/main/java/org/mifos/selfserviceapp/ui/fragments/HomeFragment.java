package org.mifos.selfserviceapp.ui.fragments;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.mifos.selfserviceapp.R;
import org.mifos.selfserviceapp.api.local.PreferencesHelper;
import org.mifos.selfserviceapp.presenters.HomePresenter;
import org.mifos.selfserviceapp.ui.activities.HomeActivity;
import org.mifos.selfserviceapp.ui.activities.LoanApplicationActivity;
import org.mifos.selfserviceapp.ui.activities.UserProfileActivity;
import org.mifos.selfserviceapp.ui.activities.base.BaseActivity;
import org.mifos.selfserviceapp.ui.enums.AccountType;
import org.mifos.selfserviceapp.ui.enums.ChargeType;
import org.mifos.selfserviceapp.ui.fragments.base.BaseFragment;
import org.mifos.selfserviceapp.ui.views.HomeView;
import org.mifos.selfserviceapp.utils.CircularImageView;
import org.mifos.selfserviceapp.utils.Constants;
import org.mifos.selfserviceapp.utils.MaterialDialog;
import org.mifos.selfserviceapp.utils.TextDrawable;
import org.mifos.selfserviceapp.utils.Toaster;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by michaelsosnick on 1/1/17.
 */

public class HomeFragment extends BaseFragment implements HomeView,
        SwipeRefreshLayout.OnRefreshListener {

    public static final String LOG_TAG = HomeFragment.class.getSimpleName();

    @BindView(R.id.iv_user_image)
    ImageView ivUserImage;

    @BindView(R.id.iv_circular_user_image)
    CircularImageView ivCircularUserImage;

    @BindView(R.id.tv_user_name)
    TextView tvUserName;

    @BindView(R.id.swipe_home_container)
    SwipeRefreshLayout slHomeContainer;

    @Inject
    HomePresenter presenter;

    @Inject
    PreferencesHelper preferencesHelper;

    View rootView;

    private Bitmap userProfileBitmap;
    private long clientId;

    public static HomeFragment newInstance() {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_home_ui, container, false);
        ((HomeActivity) getActivity()).getActivityComponent().inject(this);
        ButterKnife.bind(this, rootView);
        clientId = preferencesHelper.getClientId();

        presenter.attachView(this);

        slHomeContainer.setColorSchemeResources(R.color.blue_light, R.color.green_light, R
                .color.orange_light, R.color.red_light);
        slHomeContainer.setOnRefreshListener(this);

        if (savedInstanceState == null) {
            loadClientData();
        }

        setToolbarTitle(getString(R.string.home));
        showUserImageTextDrawable();

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(Constants.USER_PROFILE, userProfileBitmap);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            showUserImage((Bitmap) savedInstanceState.getParcelable(Constants.USER_PROFILE));
            showUserDetails(preferencesHelper.getUserName());
        }
    }

    @Override
    public void onRefresh() {
        loadClientData();
    }

    private void loadClientData() {
        if (!preferencesHelper.getUserName().isEmpty()) {
            tvUserName.setText(getString(R.string.hello_client, preferencesHelper.getUserName()));
            hideProgress();
        } else {
            presenter.getUserDetails();
        }
        presenter.getUserImage();
    }

    /**
     * Opens {@link ClientAccountsFragment} according to the {@code accountType} provided
     *
     * @param accountType Enum of {@link AccountType}
     */
    public void openAccount(AccountType accountType) {
        ((BaseActivity) getActivity()).replaceFragment(
                ClientAccountsFragment.newInstance(accountType), true, R.id.container);
    }

    /**
     * Fetches Client details and display clientName
     *
     * @param userName of the client
     */
    @Override
    public void showUserDetails(String userName) {
        tvUserName.setText(getString(R.string.hello_client, userName));
    }

    @Override
    public void showUserImageTextDrawable() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextDrawable drawable = TextDrawable.builder()
                        .beginConfig()
                        .toUpperCase()
                        .endConfig()
                        .buildRound(preferencesHelper.getUserName().substring(0, 1),
                                ContextCompat.getColor(getActivity(), R.color.primary_dark));
                ivUserImage.setImageDrawable(drawable);
                ivCircularUserImage.setVisibility(View.GONE);
            }
        });
    }

    /**
     * Provides with Client image fetched from server
     *
     * @param bitmap Client Image
     */
    @Override
    public void showUserImage(final Bitmap bitmap) {
        if (bitmap != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    userProfileBitmap = bitmap;
                    ivCircularUserImage.setImageBitmap(bitmap);
                    ivUserImage.setVisibility(View.GONE);
                }
            });
        }
    }

    @OnClick(R.id.iv_user_image)
    public void userImageClicked() {
        startActivity(new Intent(getActivity(), UserProfileActivity.class));
    }

    /**
     * Calls {@code openAccount()} for opening {@link ClientAccountsFragment}
     */
    @OnClick(R.id.ll_accounts)
    public void accountsClicked() {
        openAccount(AccountType.SAVINGS);
        ((HomeActivity) getActivity()).setNavigationViewSelectedItem(R.id.item_accounts);
    }

    /**
     * Shows a dialog with options: Normal Transfer and Third Party Transfer
     */
    @OnClick(R.id.ll_transfer)
    public void transferClicked() {
        String[] transferTypes = {getString(R.string.transfer), getString(R.string.
                third_party_transfer)};
        new MaterialDialog.Builder().init(getActivity())
                .setTitle(R.string.choose_transfer_type)
                .setItems(transferTypes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            ((HomeActivity) getActivity()).replaceFragment(
                                    SavingsMakeTransferFragment.newInstance(1, ""), true,
                                    R.id.container);
                        } else {
                            ((HomeActivity) getActivity()).replaceFragment(
                                    ThirdPartyTransferFragment.newInstance(), true, R.id.container);
                        }
                    }
                })
                .createMaterialDialog()
                .show();
    }

    /**
     * Opens {@link ClientChargeFragment} to display all Charges associated with client's account
     */
    @OnClick(R.id.ll_charges)
    public void chargesClicked() {
        ((HomeActivity) getActivity()).replaceFragment(ClientChargeFragment.newInstance(clientId,
                ChargeType.CLIENT), true, R.id.container);
    }

    /**
     * Opens {@link LoanApplicationFragment} to apply for a loan
     */
    @OnClick(R.id.ll_apply_for_loan)
    public void applyForLoan() {
        startActivity(new Intent(getActivity(), LoanApplicationActivity.class));
    }

    /**
     * Opens {@link BeneficiaryListFragment} which contains list of Beneficiaries associated with
     * Client's account
     */
    @OnClick(R.id.ll_beneficiaries)
    public void beneficiaries() {
        ((HomeActivity) getActivity()).replaceFragment(BeneficiaryListFragment.
                newInstance(), true, R.id.container);
    }

    @OnClick(R.id.ll_surveys)
    public void surveys() {

    }

    @OnClick(R.id.ll_recent_transactions)
    public void showRecentTransactions() {
        ((HomeActivity) getActivity()).replaceFragment(RecentTransactionsFragment.newInstance(),
                true, R.id.container);
    }

    /**
     * It is called whenever any error occurs while executing a request
     *
     * @param errorMessage Error message that tells the user about the problem.
     */
    @Override
    public void showError(String errorMessage) {
        Toaster.show(rootView, errorMessage);
    }

    @Override
    public void showUserImageNotFound() {
        showUserImageTextDrawable();
    }

    /**
     * Shows {@link SwipeRefreshLayout}
     */
    @Override
    public void showProgress() {
        slHomeContainer.setRefreshing(true);
    }

    /**
     * Hides {@link SwipeRefreshLayout}
     */
    @Override
    public void hideProgress() {
        slHomeContainer.setRefreshing(false);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        presenter.detachView();
    }
}
