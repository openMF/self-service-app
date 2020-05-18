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
import org.mifos.mobile.presenters.base.BasePresenter;
import org.mifos.mobile.ui.views.HomeView;
import org.mifos.mobile.utils.ImageUtil;

import java.io.IOException;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;

/**
 * Created by dilpreet on 19/6/17.
 */

public class HomePresenter extends BasePresenter<HomeView> {

    private DataManager dataManager;
    private CompositeDisposable compositeDisposable;
    @Inject
    PreferencesHelper preferencesHelper;

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
    public HomePresenter(DataManager dataManager, @ApplicationContext Context context) {
        super(context);
        this.dataManager = dataManager;
        compositeDisposable = new CompositeDisposable();
    }

    @Override
    public void attachView(HomeView mvpView) {
        super.attachView(mvpView);
    }

    @Override
    public void detachView() {
        super.detachView();
        compositeDisposable.clear();
    }

    /**
     * Fetches Details about Client from the server as {@link Client} and notifies the view to
     * display the details. And in case of any error during fetching the required details it
     * notifies the view.
     */
    public void getUserDetails() {
        checkViewAttached();
        compositeDisposable.add(dataManager.getCurrentClient()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribeWith(new DisposableObserver<Client>() {
                    @Override
                    public void onComplete() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        getMvpView().showError(context.getString(R.string.error_fetching_client));
                        getMvpView().hideProgress();
                    }

                    @Override
                    public void onNext(Client client) {
                        getMvpView().hideProgress();
                        if (client != null) {
                            preferencesHelper.setOfficeName(client.getOfficeName());
                            preferencesHelper.setClientName(client.getDisplayName());
                            getMvpView().showUserDetails(preferencesHelper.getClientName());
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
        compositeDisposable.add(dataManager.getClientImage()
                .observeOn(Schedulers.newThread())
                .subscribeOn(Schedulers.io())
                .subscribeWith(new DisposableObserver<ResponseBody>() {
                    @Override
                    public void onComplete() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        getMvpView().showUserImageNotFound();
                    }

                    @Override
                    public void onNext(ResponseBody response) {
                        try {

                            final String encodedString = response.string();

                            final String pureBase64Encoded =
                                    encodedString.substring(encodedString.indexOf(',') + 1);

                            final byte[] decodedBytes =
                                    Base64.decode(pureBase64Encoded, Base64.DEFAULT);

                            Bitmap decodedBitmap = ImageUtil.getInstance().
                                    compressImage(decodedBytes, 256, 256);
                            getMvpView().showUserImage(decodedBitmap);
                        } catch (IOException e) {
                            Log.d("userimage", e.toString());
                        }
                    }
                })
        );
    }

    /**
     * Used to get count of unread Notifications from the local sql database and on completion
     * notifies the view
     */
    public void getUnreadNotificationsCount() {
        compositeDisposable.add(dataManager.getUnreadNotificationsCount()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.computation())
                .subscribeWith(new DisposableObserver<Integer>() {
                    @Override
                    public void onComplete() { }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(Integer integer) {
                        getMvpView().showNotificationCount(integer);
                    }
                }));
    }
}
