package org.mifos.mobile.ui.fragments;

/*
~This project is licensed under the open source MPL V2.
~See https://github.com/openMF/self-service-app/blob/master/LICENSE.md
*/

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.oss.licenses.OssLicensesMenuActivity;

import org.mifos.mobile.R;
import org.mifos.mobile.ui.activities.PrivacyPolicyActivity;
import org.mifos.mobile.ui.fragments.base.BaseFragment;

import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class AboutUsFragment extends BaseFragment {

    private static final String GITHUB_REPO = "https://github.com/openMF/mifos-mobile";

    private Context mContext;

    @BindView(R.id.tv_app_version)
    TextView tvAppVersion;

    @BindView(R.id.tv_copy_right)
    TextView tvCopyRight;

    View rootView;

    public static AboutUsFragment newInstance() {
        AboutUsFragment fragment = new AboutUsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_about_us, container, false);
        ButterKnife.bind(this, rootView);
        setToolbarTitle(getString(R.string.about_us));

        tvCopyRight.setText(getString(R.string.copy_right_mifos, String.valueOf(Calendar.getInstance().get(Calendar.YEAR))));
        try {
            PackageInfo pInfo = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0);
            String version = pInfo.versionName;
            tvAppVersion.setText(version);
        } catch (PackageManager.NameNotFoundException e) {
            e.getMessage();
        }
        return rootView;
    }

    @OnClick(R.id.tv_licenses)
    void showOpenSourceLicenses() {
        startActivity(new Intent(getActivity(), OssLicensesMenuActivity.class));
    }

    @OnClick(R.id.tv_privacy_policy)
    void showPrivacyPolicy() {
        startActivity(new Intent(getActivity(), PrivacyPolicyActivity.class));
    }

    @OnClick(R.id.cv_fork)
    public void onForkClicked() {
        Intent viewIntent =
                new Intent(Intent.ACTION_VIEW, Uri.parse(GITHUB_REPO));
        startActivity(viewIntent);
    }
}
