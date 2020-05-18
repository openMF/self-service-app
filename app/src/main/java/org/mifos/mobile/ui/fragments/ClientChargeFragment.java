package org.mifos.mobile.ui.fragments;

import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.github.therajanmaurya.sweeterror.SweetUIErrorHandler;
import com.google.android.material.snackbar.Snackbar;

import org.mifos.mobile.R;
import org.mifos.mobile.models.Charge;
import org.mifos.mobile.presenters.ClientChargePresenter;
import org.mifos.mobile.ui.activities.base.BaseActivity;
import org.mifos.mobile.ui.adapters.ClientChargeAdapter;
import org.mifos.mobile.ui.enums.ChargeType;
import org.mifos.mobile.ui.fragments.base.BaseFragment;
import org.mifos.mobile.ui.views.ClientChargeView;
import org.mifos.mobile.utils.Constants;
import org.mifos.mobile.utils.Network;
import org.mifos.mobile.utils.RecyclerItemClickListener;
import org.mifos.mobile.utils.Toaster;

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
 * @author Vishwajeet
 * @since 17/8/16.
 */

public class ClientChargeFragment extends BaseFragment implements
        RecyclerItemClickListener.OnItemClickListener, ClientChargeView {

    @BindView(R.id.rv_client_charge)
    RecyclerView rvClientCharge;

    @BindView(R.id.swipe_charge_container)
    SwipeRefreshLayout swipeChargeContainer;

    @BindView(R.id.layout_error)
    View layoutError;

    @Inject
    ClientChargePresenter clientChargePresenter;

    @Inject
    ClientChargeAdapter clientChargeAdapter;

    private long id;
    private ChargeType chargeType;
    private View rootView;
    private LinearLayoutManager layoutManager;
    private List<Charge> clientChargeList = new ArrayList<>();
    private SweetUIErrorHandler sweetUIErrorHandler;

    public static ClientChargeFragment newInstance(long clientId, ChargeType chargeType) {
        ClientChargeFragment clientChargeFragment = new ClientChargeFragment();
        Bundle args = new Bundle();
        args.putLong(Constants.CLIENT_ID, clientId);
        args.putSerializable(Constants.CHARGE_TYPE, chargeType);
        clientChargeFragment.setArguments(args);
        return clientChargeFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((BaseActivity) getActivity()).getActivityComponent().inject(this);
        if (getArguments() != null) {
            id = getArguments().getLong(Constants.CLIENT_ID);
            chargeType = (ChargeType) getArguments().getSerializable(Constants.CHARGE_TYPE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_client_charge, container, false);
        ButterKnife.bind(this, rootView);

        clientChargePresenter.attachView(this);
        setToolbarTitle(getString(R.string.charges));

        sweetUIErrorHandler = new SweetUIErrorHandler(getActivity(), rootView);

        layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        rvClientCharge.setLayoutManager(layoutManager);
        rvClientCharge.addOnItemTouchListener(new RecyclerItemClickListener(getActivity(), this));

        swipeChargeContainer.setColorSchemeResources(R.color.blue_light, R.color.green_light, R
                .color.orange_light, R.color.red_light);
        swipeChargeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadCharges();
            }
        });
        if (savedInstanceState == null) {
            loadCharges();
        }
        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(Constants.CHARGES, new ArrayList<Parcelable>(
                clientChargeList));
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            List<Charge> charges = savedInstanceState.getParcelableArrayList(Constants.CHARGES);
            showClientCharges(charges);
        }
    }

    /**
     * Fetches Charges for {@code id} according to {@code chargeType} provided.
     */
    private void loadCharges() {
        if (layoutError.getVisibility() == View.VISIBLE) {
            sweetUIErrorHandler.hideSweetErrorLayoutUI(rvClientCharge, layoutError);
        }

        if (chargeType == ChargeType.CLIENT) {
            clientChargePresenter.loadClientCharges(id);
        } else if (chargeType == ChargeType.SAVINGS) {
            clientChargePresenter.loadSavingsAccountCharges(id);
        } else if (chargeType == ChargeType.LOAN) {
            clientChargePresenter.loadLoanAccountCharges(id);
        }
    }

    /**
     * It is called whenever any error occurs while executing a request. If not connected to
     * internet then it shows display a message to user to connect to internet other it just
     * displays the {@code message} in a {@link Snackbar}
     *
     * @param message Error message that tells the user about the problem.
     */
    @Override
    public void showErrorFetchingClientCharges(String message) {
        if (!Network.isConnected(getActivity())) {
            sweetUIErrorHandler.showSweetNoInternetUI(rvClientCharge, layoutError);
        } else {
            sweetUIErrorHandler.showSweetErrorUI(message, rvClientCharge, layoutError);
            Toaster.show(rootView, message);
        }
    }

    /**
     * Tries to fetch charges again.
     */
    @OnClick(R.id.btn_try_again)
    public void retryClicked() {
        if (Network.isConnected(getContext())) {
            sweetUIErrorHandler.hideSweetErrorLayoutUI(rvClientCharge, layoutError);
            loadCharges();
        } else {
            Toast.makeText(getContext(), getString(R.string.internet_not_connected),
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Receives {@code clientChargeList} from server and calls {@code inflateClientChargeList()} to
     * update the {@code clientChargeAdapter} adapter.
     *
     * @param clientChargeList {@link List} of {@link Charge}
     */
    @Override
    public void showClientCharges(List<Charge> clientChargeList) {
        this.clientChargeList = clientChargeList;
        inflateClientChargeList();
        if (swipeChargeContainer.isRefreshing()) {
            swipeChargeContainer.setRefreshing(false);
        }
    }

    /**
     * Updates {@code clientChargeAdapter} with updated {@code clientChargeList} if
     * {@code clientChargeList} size if greater than 0 else shows the error layout
     */
    private void inflateClientChargeList() {
        if (clientChargeList.size() > 0) {
            clientChargeAdapter.setClientChargeList(clientChargeList);
            rvClientCharge.setAdapter(clientChargeAdapter);
        } else {
            sweetUIErrorHandler.showSweetEmptyUI(getString(R.string.charges), R.drawable.ic_charges,
                    rvClientCharge, layoutError);
        }
    }

    @Override
    public void showProgress() {
        swipeChargeContainer.setRefreshing(true);
    }

    @Override
    public void hideProgress() {
        swipeChargeContainer.setRefreshing(false);
    }

    @Override
    public void onItemClick(View childView, int position) { }

    @Override
    public void onItemLongPress(View childView, int position) { }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        clientChargePresenter.detachView();
    }

}
