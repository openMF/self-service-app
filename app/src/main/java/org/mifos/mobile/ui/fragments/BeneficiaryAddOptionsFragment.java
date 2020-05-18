package org.mifos.mobile.ui.fragments;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.mifos.mobile.R;
import org.mifos.mobile.ui.activities.base.BaseActivity;
import org.mifos.mobile.ui.enums.BeneficiaryState;
import org.mifos.mobile.ui.enums.RequestAccessType;
import org.mifos.mobile.ui.fragments.base.BaseFragment;
import org.mifos.mobile.utils.CheckSelfPermissionAndRequest;
import org.mifos.mobile.utils.Constants;
import org.mifos.mobile.utils.Toaster;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by dilpreet on 5/7/17.
 */

public class BeneficiaryAddOptionsFragment extends BaseFragment {

    private View rootView;
    private boolean external_storage_read_status = false;
    private boolean external_storage_write_status = false;

    public static BeneficiaryAddOptionsFragment newInstance() {
        BeneficiaryAddOptionsFragment fragment = new BeneficiaryAddOptionsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_beneficiary_add_options, container, false);
        setToolbarTitle(getString(R.string.add_beneficiary));
        ((BaseActivity) getActivity()).getActivityComponent().inject(this);
        ButterKnife.bind(this, rootView);

        return rootView;
    }

    /**
     * Opens {@link BeneficiaryApplicationFragment} with {@link BeneficiaryState} as
     * {@code BeneficiaryState.CREATE_MANUAL}
     */
    @OnClick(R.id.ll_add_beneficiary_manually)
    public void addManually() {
        ((BaseActivity) getActivity()).replaceFragment(BeneficiaryApplicationFragment.
                newInstance(BeneficiaryState.CREATE_MANUAL, null), true, R.id.container);
    }

    /**
     * It first checks CAMERA runtime permission and if it returns true then it opens
     * {@link QrCodeReaderFragment} , if it returns false then ask for permissions.
     */
    @OnClick(R.id.ll_add_beneficiary_qrcode)
    public void addUsingQrCode() {

        if (CheckSelfPermissionAndRequest.checkSelfPermission(getActivity(),
                Manifest.permission.CAMERA)) {
            ((BaseActivity) getActivity()).replaceFragment(QrCodeReaderFragment.
                    newInstance(), true, R.id.container);
        } else {
            requestPermission(RequestAccessType.CAMERA);
        }
    }

    /**
     * It first checks Storage Read and Write Permission then if both of them are true then it opens
     * Intent to all gallery app
     */
    @OnClick(R.id.ll_upload_beneficiary_qrcode)
    public void addByImportingQrCode() {

        //request permission for writing external storage
        accessReadWriteAccess();
        if (external_storage_write_status && external_storage_read_status) {
            Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
            getIntent.setType("image/*");
            Intent pickIntent = new Intent(Intent.ACTION_PICK,
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            pickIntent.setType("image/*");
            Intent chooserIntent = Intent.createChooser(getIntent, "Select Image");
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{pickIntent});
            startActivityForResult(chooserIntent, Constants.GALLERY_QR_PICK);
        }

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.GALLERY_QR_PICK && data != null) {
            getActivity().getSupportFragmentManager().popBackStack();
            ((BaseActivity) getActivity()).replaceFragment(QrCodeImportFragment
                    .newInstance(data.getData()), true, R.id.container);
        }
    }


    /**
     * Checks for permissions needed to complete the action, if not granted then requests for
     * the same
     */
    public void accessReadWriteAccess() {
        if (CheckSelfPermissionAndRequest.checkSelfPermission(getActivity(),
                Manifest.permission.READ_EXTERNAL_STORAGE)) {
            external_storage_read_status = true;
        } else {
            requestPermission(RequestAccessType.EXTERNAL_STORAGE_READ);
        }
        if (CheckSelfPermissionAndRequest.checkSelfPermission(getActivity(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            external_storage_write_status = true;
        } else {
            requestPermission(RequestAccessType.EXTERNAL_STORAGE_WRITE);
        }
    }

    /**
     * Uses {@link CheckSelfPermissionAndRequest} to check for runtime permissions
     */
    private void requestPermission(RequestAccessType requestAccessType) {

        switch (requestAccessType) {
            case CAMERA: {
                CheckSelfPermissionAndRequest.requestPermission(
                        (BaseActivity) getActivity(),
                        Manifest.permission.CAMERA,
                        Constants.PERMISSIONS_REQUEST_CAMERA,
                        getResources().getString(
                                R.string.dialog_message_camera_permission_denied_prompt),
                        getResources().getString(R
                                .string.dialog_message_camera_permission_never_ask_again),
                        Constants.PERMISSIONS_CAMERA_STATUS);
            }
            break;
            case EXTERNAL_STORAGE_READ: {
                CheckSelfPermissionAndRequest.requestPermission((BaseActivity) getActivity(),
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Constants.PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE,
                        getResources().getString(R
                                .string.dialog_message_storage_permission_denied_prompt),
                        getResources()
                                .getString(R.string
                                        .dialog_message_read_storage_permission_never_ask_again),
                        Constants.PERMISSIONS_STORAGE_STATUS);

            }
            break;
            case EXTERNAL_STORAGE_WRITE: {
                CheckSelfPermissionAndRequest.requestPermission((BaseActivity) getActivity(),
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Constants.PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE,
                        getResources().getString(R
                                .string.dialog_message_storage_permission_denied_prompt),
                        getResources().getString(R
                                .string.dialog_message_write_storage_permission_never_ask_again),
                        Constants.PERMISSIONS_STORAGE_STATUS);
            }

        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        switch (requestCode) {
            case Constants.PERMISSIONS_REQUEST_CAMERA: {

                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    ((BaseActivity) getActivity()).replaceFragment(QrCodeReaderFragment.
                            newInstance(), true, R.id.container);

                } else {

                    Toaster.show(rootView, getResources()
                            .getString(R.string.permission_denied_camera));
                }
            }
            break;
            case Constants.PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE: {

                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    external_storage_read_status = true;
                } else {
                    Toaster.show(rootView, getResources()
                            .getString(R.string.permission_denied_storage));
                }

            }
            break;
            case Constants.PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    external_storage_write_status = true;
                } else {
                    Toaster.show(rootView, getResources()
                            .getString(R.string.permission_denied_storage));
                }
            }

        }
    }
}