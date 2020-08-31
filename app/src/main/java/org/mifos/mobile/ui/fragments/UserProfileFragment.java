package org.mifos.mobile.ui.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.therajanmaurya.sweeterror.SweetUIErrorHandler;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.mifos.mobile.R;
import org.mifos.mobile.api.local.PreferencesHelper;
import org.mifos.mobile.models.client.Client;
import org.mifos.mobile.models.client.Group;
import org.mifos.mobile.presenters.UserDetailsPresenter;
import org.mifos.mobile.ui.activities.EditUserDetailActivity;
import org.mifos.mobile.ui.activities.base.BaseActivity;
import org.mifos.mobile.ui.fragments.base.BaseFragment;
import org.mifos.mobile.ui.views.UserDetailsView;
import org.mifos.mobile.utils.Constants;
import org.mifos.mobile.utils.DateHelper;
import org.mifos.mobile.utils.TextDrawable;
import org.mifos.mobile.utils.Toaster;

import java.util.List;

import javax.inject.Inject;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by dilpreet on 10/7/17.
 */

public class UserProfileFragment extends BaseFragment implements UserDetailsView {

    @BindView(R.id.iv_profile)
    ImageView ivProfile;

    @BindView(R.id.iv_text_drawable)
    ImageView ivTextDrawable;

    @BindView(R.id.app_bar_layout)
    AppBarLayout appBarLayout;

    @BindView(R.id.collapsing_toolbar)
    CollapsingToolbarLayout collapsingToolbarLayout;

    @BindView(R.id.tv_user_name)
    TextView tvUsername;

    @BindView(R.id.tv_account_number)
    TextView tvAccountNumber;

    @BindView(R.id.tv_activation_date)
    TextView tvActivationDate;

    @BindView(R.id.tv_office_name)
    TextView tvOfficeName;

    @BindView(R.id.tv_client_type)
    TextView tvClientType;

    @BindView(R.id.tv_phone_number)
    TextView tvPhoneNumber;

    @BindView(R.id.tv_dob)
    TextView tvDOB;

    @BindView(R.id.tv_gender)
    TextView tvGender;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.layout_error)
    View layoutError;

    @BindView(R.id.ll_user_profile)
    LinearLayout llUserProfile;

    @BindView(R.id.fab_edit)
    FloatingActionButton fabEdit;

    @Inject
    UserDetailsPresenter presenter;

    @Inject
    PreferencesHelper preferencesHelper;

    private View rootView;
    private Bitmap userBitmap;
    private Client client;
    private SweetUIErrorHandler sweetUIErrorHandler;

    public static UserProfileFragment newInstance() {
        UserProfileFragment fragment = new UserProfileFragment();
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_user_profile, container, false);
        ((BaseActivity) getActivity()).getActivityComponent().inject(this);
        ButterKnife.bind(this, rootView);
        presenter.attachView(this);

        ((BaseActivity) getActivity()).setSupportActionBar(toolbar);
        ((BaseActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        collapsingToolbarLayout.setCollapsedTitleTextColor(ContextCompat.getColor(getActivity(),
                R.color.white));
        collapsingToolbarLayout.setExpandedTitleColor(ContextCompat.getColor(getActivity(),
                R.color.white));

        sweetUIErrorHandler = new SweetUIErrorHandler(getActivity(), rootView);
        if (savedInstanceState == null) {
            presenter.getUserDetails();
            presenter.getUserImage();
        }
        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(Constants.USER_DETAILS, client);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            client = savedInstanceState.getParcelable(Constants.USER_DETAILS);
            presenter.setUserProfile(preferencesHelper.getUserProfileImage());
            showUserDetails(client);
        }
    }

    /**
     * Sets client basic details which are fetched from server
     *
     * @param client instance of {@link Client} which contains information about client
     */
    @Override
    public void showUserDetails(Client client) {
        this.client = client;
        tvUsername.setText(nullFieldCheck(getString(R.string.username), client.getDisplayName()));
        tvAccountNumber.setText(nullFieldCheck(getString(R.string.account_number),
                client.getAccountNo()));
        tvActivationDate.setText(nullFieldCheck(getString(R.string.activation_date),
                DateHelper.getDateAsString(client.getActivationDate())));
        tvOfficeName.setText(nullFieldCheck(getString(R.string.office_name),
                client.getOfficeName()));
        tvClientType.setText(nullFieldCheck(getString(R.string.client_type),
                client.getClientType().getName()));
        tvPhoneNumber.setText(nullFieldCheck(getString(R.string.phone_number),
                client.getMobileNo()));
        if (client.getDobDate().size() != 3) {  // no data entry in database for the client
            tvDOB.setText(getString(R.string.no_dob_found));
        } else {
            tvDOB.setText(DateHelper.getDateAsString(client.getDobDate()));
        }
        tvGender.setText(nullFieldCheck(getString(R.string.gender), client.getGender().getName()));
    }

    private String nullFieldCheck(String field, String value) {
        if (value == null) {
            return getString(R.string.no) + getString(R.string.blank) + field +
                    getString(R.string.blank) + getString(R.string.found);
        }
        return value;
    }

    /**
     * Generate a string for groups which the client is part of.
     *
     * @param groups {@link List} of {@link Group} which client is a part of.
     * @return Returns String of groups
     */
    private String getGroups(List<Group> groups) {
        if (groups.size() == 0) {
            return getString(
                    R.string.not_assigned_with_any_group); // no groups entry in database for the
            // client
        }


        StringBuilder builder = new StringBuilder();
        for (Group group : groups) {
            builder.append(getString(R.string.string_and_string, group.getName(), " | "));
        }
        return builder.toString().substring(0, builder.toString().length() - 2);
    }

    /**
     * Provides with client Image fetched from the server in {@code bitmap}
     *
     * @param bitmap User Image
     */
    @Override
    public void showUserImage(final Bitmap bitmap) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                userBitmap = bitmap;
                if (userBitmap == null) {
                    final TextDrawable textDrawable = TextDrawable.builder()
                            .beginConfig()
                            .toUpperCase()
                            .endConfig()
                            .buildRound(preferencesHelper
                                            .getClientName()
                                            .substring(0, 1),
                                    ContextCompat.getColor(getContext(), R.color.primary_dark));
                    ivProfile.setVisibility(View.GONE);
                    ivTextDrawable.setVisibility(View.VISIBLE);
                    ivTextDrawable.setImageDrawable(textDrawable);
                } else {
                    ivTextDrawable.setVisibility(View.GONE);
                    ivProfile.setVisibility(View.VISIBLE);
                    ivProfile.setImageBitmap(bitmap);
                }
            }
        });
    }

    @OnClick(R.id.btn_change_password)
    void changePassword() {
        startActivity(new Intent(getContext(), EditUserDetailActivity.class));
    }

    /**
     * It is called whenever any error occurs while executing a request
     *
     * @param message Error message that tells the user about the problem.
     */
    @Override
    public void showError(String message) {
        Toaster.show(rootView, message);
        sweetUIErrorHandler.showSweetCustomErrorUI(getString(R.string.error_fetching_user_profile),
                R.drawable.ic_assignment_turned_in_black_24dp, appBarLayout,
                layoutError);
        fabEdit.setVisibility(View.GONE);
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
