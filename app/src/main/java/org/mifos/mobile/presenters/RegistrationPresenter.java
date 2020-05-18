package org.mifos.mobile.presenters;

import android.content.Context;

import org.mifos.mobile.api.DataManager;
import org.mifos.mobile.injection.ApplicationContext;
import org.mifos.mobile.models.register.RegisterPayload;
import org.mifos.mobile.presenters.base.BasePresenter;
import org.mifos.mobile.ui.views.RegistrationView;
import org.mifos.mobile.utils.MFErrorParser;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;


/**
 * Created by dilpreet on 31/7/17.
 */

public class RegistrationPresenter extends BasePresenter<RegistrationView> {

    private DataManager dataManager;
    private CompositeDisposable compositeDisposables;

    /**
     * Initialises the RecentTransactionsPresenter by automatically injecting an instance of
     * {@link DataManager} and {@link Context}.
     *
     * @param dataManager DataManager class that provides access to the data
     *                    via the API.
     * @param context     Context of the view attached to the presenter. In this case
     *                    it is that of an {@link androidx.appcompat.app.AppCompatActivity}
     */
    @Inject
    public RegistrationPresenter(DataManager dataManager, @ApplicationContext Context context) {
        super(context);
        this.dataManager = dataManager;
        compositeDisposables = new CompositeDisposable();
    }

    @Override
    public void attachView(RegistrationView mvpView) {
        super.attachView(mvpView);
    }

    @Override
    public void detachView() {
        super.detachView();
        compositeDisposables.clear();
    }

    /**
     * Used to register instance of the user if details entered meet the required standards and
     * notify the view on completion. Also notifies the view in case of any errors
     *
     * @param registerPayload Instance of {@link RegisterPayload} that contains the details of
     *                        client that has to be registered
     */
    public void registerUser(RegisterPayload registerPayload) {
        checkViewAttached();
        getMvpView().showProgress();
        compositeDisposables.add(dataManager.registerUser(registerPayload)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribeWith(new DisposableObserver<ResponseBody>() {
                    @Override
                    public void onComplete() { }

                    @Override
                    public void onError(Throwable e) {
                        getMvpView().hideProgress();
                        getMvpView().showError(MFErrorParser.errorMessage(e));
                    }

                    @Override
                    public void onNext(ResponseBody responseBody) {
                        getMvpView().hideProgress();
                        getMvpView().showRegisteredSuccessfully();
                    }
                }));
    }
}
