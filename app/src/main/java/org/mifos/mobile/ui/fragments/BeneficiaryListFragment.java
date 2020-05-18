package org.mifos.mobile.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.github.therajanmaurya.sweeterror.SweetUIErrorHandler;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.mifos.mobile.R;
import org.mifos.mobile.models.beneficiary.Beneficiary;
import org.mifos.mobile.presenters.BeneficiaryListPresenter;
import org.mifos.mobile.ui.activities.AddBeneficiaryActivity;
import org.mifos.mobile.ui.activities.base.BaseActivity;
import org.mifos.mobile.ui.adapters.BeneficiaryListAdapter;
import org.mifos.mobile.ui.fragments.base.BaseFragment;
import org.mifos.mobile.ui.views.BeneficiariesView;
import org.mifos.mobile.utils.Constants;
import org.mifos.mobile.utils.DividerItemDecoration;
import org.mifos.mobile.utils.Network;
import org.mifos.mobile.utils.RecyclerItemClickListener;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by dilpreet on 14/6/17.
 */

public class BeneficiaryListFragment extends BaseFragment implements RecyclerItemClickListener.
        OnItemClickListener, SwipeRefreshLayout.OnRefreshListener, BeneficiariesView {

    @BindView(R.id.rv_beneficiaries)
    RecyclerView rvBeneficiaries;

    @BindView(R.id.swipe_container)
    SwipeRefreshLayout swipeRefreshLayout;

    @BindView(R.id.fab_add_beneficiary)
    FloatingActionButton fabAddBeneficiary;

    @BindView(R.id.layout_error)
    View layoutError;

    @Inject
    BeneficiaryListPresenter beneficiaryListPresenter;

    @Inject
    BeneficiaryListAdapter beneficiaryListAdapter;

    private View rootView;
    private List<Beneficiary> beneficiaryList;
    private SweetUIErrorHandler sweetUIErrorHandler;

    public static BeneficiaryListFragment newInstance() {
        BeneficiaryListFragment fragment = new BeneficiaryListFragment();
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_beneficiary_list, container, false);
        ((BaseActivity) getActivity()).getActivityComponent().inject(this);
        ButterKnife.bind(this, rootView);
        setToolbarTitle(getString(R.string.beneficiaries));
        sweetUIErrorHandler = new SweetUIErrorHandler(getActivity(), rootView);

        showUserInterface();

        beneficiaryListPresenter.attachView(this);
        if (savedInstanceState == null) {
            beneficiaryListPresenter.loadBeneficiaries();
        }

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        beneficiaryListPresenter.loadBeneficiaries();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (beneficiaryList != null) {
            outState.putParcelableArrayList(Constants.BENEFICIARY, new ArrayList<Parcelable>(
                    beneficiaryList));
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            List<Beneficiary> beneficiaries = savedInstanceState.getParcelableArrayList(Constants.
                    BENEFICIARY);
            showBeneficiaryList(beneficiaries);
        }
    }

    /**
     * Setup Initial User Interface
     */
    @Override
    public void showUserInterface() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rvBeneficiaries.setLayoutManager(layoutManager);
        rvBeneficiaries.setHasFixedSize(true);
        rvBeneficiaries.addItemDecoration(new DividerItemDecoration(getActivity(), layoutManager.
                getOrientation()));
        rvBeneficiaries.addOnItemTouchListener(new RecyclerItemClickListener(getActivity(), this));
        rvBeneficiaries.setAdapter(beneficiaryListAdapter);

        swipeRefreshLayout.setColorSchemeColors(getActivity()
                .getResources().getIntArray(R.array.swipeRefreshColors));
        swipeRefreshLayout.setOnRefreshListener(this);

        fabAddBeneficiary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), AddBeneficiaryActivity.class));
            }
        });
    }


    @OnClick(R.id.btn_try_again)
    public void retryClicked() {
        if (Network.isConnected(getContext())) {
            sweetUIErrorHandler.hideSweetErrorLayoutUI(rvBeneficiaries, layoutError);
            beneficiaryListPresenter.loadBeneficiaries();
        } else {
            Toast.makeText(getContext(), getString(R.string.internet_not_connected),
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Refreshes {@code beneficiaryList} by calling {@code loadBeneficiaries()}
     */
    @Override
    public void onRefresh() {
        if (layoutError.getVisibility() == View.VISIBLE) {
            sweetUIErrorHandler.hideSweetErrorLayoutUI(rvBeneficiaries, layoutError);
        }
        beneficiaryListPresenter.loadBeneficiaries();
    }

    /**
     * Shows {@link SwipeRefreshLayout}
     */
    @Override
    public void showProgress() {
        showSwipeRefreshLayout(true);
    }

    /**
     * Hides {@link SwipeRefreshLayout}
     */
    @Override
    public void hideProgress() {
        showSwipeRefreshLayout(false);
    }

    /**
     * It is called whenever any error occurs while executing a request
     *
     * @param msg Error message that tells the user about the problem.
     */
    @Override
    public void showError(String msg) {

        if (!Network.isConnected(getActivity())) {
            sweetUIErrorHandler.showSweetNoInternetUI(rvBeneficiaries, layoutError);
        } else {
            sweetUIErrorHandler.showSweetErrorUI(msg,
                    rvBeneficiaries, layoutError);
            Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Set the {@code beneficiaryList} fetched from server to {@code beneficiaryListAdapter}
     */
    @Override
    public void showBeneficiaryList(List<Beneficiary> beneficiaryList) {
        this.beneficiaryList = beneficiaryList;
        if (beneficiaryList.size() != 0) {
            beneficiaryListAdapter.setBeneficiaryList(beneficiaryList);
        } else {
            showEmptyBeneficiary();
        }
    }

    @Override
    public void onItemClick(View childView, int position) {
        ((BaseActivity) getActivity()).replaceFragment(BeneficiaryDetailFragment
                .newInstance(beneficiaryList.get(position)), true, R.id.container);
    }

    @Override
    public void onItemLongPress(View childView, int position) { }

    public void showSwipeRefreshLayout(final boolean show) {
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(show);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        beneficiaryListPresenter.detachView();
    }

    /**
     * Shows an error layout when this function is called.
     */
    public void showEmptyBeneficiary() {
        sweetUIErrorHandler.showSweetEmptyUI(getString(R.string.beneficiary),
                getString(R.string.beneficiary),
                R.drawable.ic_beneficiaries_48px, rvBeneficiaries, layoutError);
        rvBeneficiaries.setVisibility(View.GONE);
    }

}
