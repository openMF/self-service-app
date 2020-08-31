package org.mifos.mobile.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.zxing.Result;

import org.mifos.mobile.R;
import org.mifos.mobile.models.beneficiary.Beneficiary;
import org.mifos.mobile.ui.activities.base.BaseActivity;
import org.mifos.mobile.ui.enums.BeneficiaryState;
import org.mifos.mobile.ui.fragments.base.BaseFragment;

import androidx.annotation.Nullable;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.dm7.barcodescanner.zxing.ZXingScannerView;

/**
 * Created by dilpreet on 6/7/17.
 */

public class QrCodeReaderFragment extends BaseFragment implements ZXingScannerView.ResultHandler {

    @BindView(R.id.view_scanner)
    ZXingScannerView mScannerView;

    @BindView(R.id.btn_flash)
    ImageButton btnFlash;


    private View rootView;
    private boolean flashOn = false;

    public static QrCodeReaderFragment newInstance() {
        QrCodeReaderFragment fragment = new QrCodeReaderFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_scan_qr_code, container, false);
        setToolbarTitle(getString(R.string.add_beneficiary));
        ((BaseActivity) getActivity()).getActivityComponent().inject(this);
        ButterKnife.bind(this, rootView);
        mScannerView.setAutoFocus(true);
        return rootView;
    }

    /**
     * Sets the {@link me.dm7.barcodescanner.zxing.ZXingScannerView.ResultHandler} callback and
     * opens Camera
     */
    @Override
    public void onResume() {
        super.onResume();
        mScannerView.setResultHandler(this);
        mScannerView.startCamera();
    }

    @OnClick(R.id.btn_flash)
    public void turnOnFlash() {
        if (flashOn) {
            flashOn = false;
            btnFlash.setImageDrawable(getResources().getDrawable(R.drawable.ic_flash_on));
            mScannerView.setFlash(false);
        } else {
            flashOn = true;
            btnFlash.setImageDrawable(getResources().getDrawable(R.drawable.ic_flash_off));
            mScannerView.setFlash(true);
        }
    }


    /**
     * Closes the Camera
     */
    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();
    }

    /**
     * Callback for {@link ZXingScannerView} which retrieves data from QRCode
     *
     * @param result Contains data scanned from QRCode
     */
    @Override
    public void handleResult(Result result) {
        Gson gson = new Gson();
        try {
            Beneficiary beneficiary = gson.fromJson(result.getText(), Beneficiary.class);
            getActivity().getSupportFragmentManager().popBackStack();
            ((BaseActivity) getActivity()).replaceFragment(BeneficiaryApplicationFragment.
                    newInstance(BeneficiaryState.CREATE_QR, beneficiary), true, R.id.container);
        } catch (JsonSyntaxException e) {
            Toast.makeText(getActivity(), getString(R.string.invalid_qr),
                    Toast.LENGTH_SHORT).show();
            mScannerView.resumeCameraPreview(this);
        }
    }
}
