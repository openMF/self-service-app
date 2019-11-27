package org.mifos.mobile.presenters;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Base64;
import android.util.Log;

import org.mifos.mobile.R;
import org.mifos.mobile.api.DataManager;
import org.mifos.mobile.api.local.PreferencesHelper;
import org.mifos.mobile.injection.ApplicationContext;
import org.mifos.mobile.models.client.Client;
import org.mifos.mobile.models.notification.NotificationRegisterPayload;
import org.mifos.mobile.models.notification.NotificationUserDetail;
import org.mifos.mobile.presenters.base.BasePresenter;
import org.mifos.mobile.ui.views.UserDetailsView;
import org.mifos.mobile.utils.ImageUtil;

import java.io.IOException;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;
import retrofit2.HttpException;

/**
 * Created by naman on 07/04/17.
 */

public class UserDetailsPresenter extends BasePresenter<UserDetailsView> {

    private final DataManager dataManager;
    private CompositeDisposable compositeDisposables;
    private PreferencesHelper preferencesHelper;

    /**
     * Initialises the LoginPresenter by automatically injecting an instance of
     * {@link DataManager} and {@link Context}.
     *
     * @param dataManager DataManager class that provides access to the data
     *                    via the API.
     * @param context     Context of the view attached to the presenter. In this case
     *                    it is that of an {@link androidx.appcompat.app.AppCompatActivity}
     */
    @Inject
    public UserDetailsPresenter(@ApplicationContext Context context, DataManager dataManager,
                                PreferencesHelper preferencesHelper) {
        super(context);
        this.dataManager = dataManager;
        compositeDisposables = new CompositeDisposable();
        this.preferencesHelper = preferencesHelper;
    }

    @Override
    public void attachView(UserDetailsView mvpView) {
        super.attachView(mvpView);
    }

    @Override
    public void detachView() {
        super.detachView();
        compositeDisposables.clear();
    }

    /**
     * Fetches Details about Client from the server as {@link Client} and notifies the view to
     * display the details. And in case of any error during fetching the required details it
     * notifies the view.
     */
    public void getUserDetails() {
        checkViewAttached();
        getMvpView().showProgress();
        compositeDisposables.add(dataManager.getCurrentClient()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribeWith(new DisposableObserver<Client>() {
                    @Override
                    public void onComplete() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        getMvpView().hideProgress();
                        getMvpView().showError(context.getString(R.string.error_fetching_client));
                    }

                    @Override
                    public void onNext(Client client) {
                        getMvpView().hideProgress();
                        if (client != null) {
                            getMvpView().showUserDetails(client);
                        } else {
                            getMvpView().showError(context
                                    .getString(R.string.error_client_not_found));
                        }
                    }
                })
        );
    }

    /**
     * Fetches Client image from the server in {@link Base64} format which is then decoded into a
     * {@link Bitmap} after which the view notified to display it.
     */
    public void getUserImage() {
        checkViewAttached();
        setUserProfile(preferencesHelper.getUserProfileImage());

        compositeDisposables.add(dataManager.getClientImage()
                .observeOn(Schedulers.newThread())
                .subscribeOn(Schedulers.io())
                .subscribeWith(new DisposableObserver<ResponseBody>() {
                    @Override
                    public void onComplete() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        getMvpView().showUserImage(null);
                    }

                    @Override
                    public void onNext(ResponseBody response) {
                        try {

                            final String encodedString = response.string();

                            //removing 'data:image/jpg;base64' from the response
                            //the response is of the form of 'data:image/jpg;base64, .....'
                            final String pureBase64Encoded =
                                    encodedString.substring(encodedString.indexOf(',') + 1);
                            preferencesHelper.setUserProfileImage(pureBase64Encoded);
                            setUserProfile(pureBase64Encoded);
                        } catch (IOException e) {
                            Log.e("userimage", e.getMessage());
                        }
                    }
                })
        );
    }

    public void setUserProfile(String image) {
        if (image == null) {
            return;
        }
        try {
            final byte[] decodedBytes = Base64.decode(image, Base64.DEFAULT);
            Bitmap decodedBitmap = ImageUtil.getInstance().compressImage(decodedBytes);
            getMvpView().showUserImage(decodedBitmap);
        } catch (Exception e) {
            Log.e(UserDetailsPresenter.class.getSimpleName(), e.toString());
        }
    }

    public void registerNotification(final String token) {
        checkViewAttached();
        final NotificationRegisterPayload payload = new
                NotificationRegisterPayload(preferencesHelper.getClientId(), token);
        compositeDisposables.add(dataManager.registerNotification(payload)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribeWith(new DisposableObserver<ResponseBody>() {
                    @Override
                    public void onComplete() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(UserDetailsPresenter.class.getSimpleName(), e.toString());
                        if (e instanceof HttpException && ((HttpException) e).code() == 500) {
                            getUserNotificationId(payload, token);
                        }
                    }

                    @Override
                    public void onNext(ResponseBody responseBody) {
                        preferencesHelper.setSentTokenToServer(true);
                        preferencesHelper.saveGcmToken(token);
                    }
                }));
    }

    private void getUserNotificationId(final NotificationRegisterPayload payload, final String
            token) {
        checkViewAttached();
        compositeDisposables.add(dataManager.getUserNotificationId(preferencesHelper.getClientId())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribeWith(new DisposableObserver<NotificationUserDetail>() {
                    @Override
                    public void onComplete() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(UserDetailsPresenter.class.getSimpleName(), e.toString());
                    }

                    @Override
                    public void onNext(NotificationUserDetail userDetail) {
                        updateRegistrationNotification(userDetail.getId(), payload, token);
                    }
                }));
    }

    private void updateRegistrationNotification(long id, NotificationRegisterPayload payload,
            final String token) {
        checkViewAttached();
        compositeDisposables.add(dataManager.updateRegisterNotification(id, payload)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribeWith(new DisposableObserver<ResponseBody>() {
                    @Override
                    public void onComplete() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(UserDetailsPresenter.class.getSimpleName(), e.toString());
                    }

                    @Override
                    public void onNext(ResponseBody responseBody) {
                        preferencesHelper.setSentTokenToServer(true);
                        preferencesHelper.saveGcmToken(token);
                    }
                }));
    }
}
