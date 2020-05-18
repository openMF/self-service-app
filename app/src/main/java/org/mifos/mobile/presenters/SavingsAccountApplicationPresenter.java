package org.mifos.mobile.presenters;

/*
 * Created by saksham on 30/June/2018
 */

import android.content.Context;

import org.mifos.mobile.api.DataManager;
import org.mifos.mobile.injection.ApplicationContext;
import org.mifos.mobile.models.accounts.savings.SavingsAccountApplicationPayload;
import org.mifos.mobile.models.accounts.savings.SavingsAccountUpdatePayload;
import org.mifos.mobile.models.templates.savings.SavingsAccountTemplate;
import org.mifos.mobile.presenters.base.BasePresenter;
import org.mifos.mobile.ui.enums.SavingsAccountState;
import org.mifos.mobile.ui.views.SavingsAccountApplicationView;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;

public class SavingsAccountApplicationPresenter
        extends BasePresenter<SavingsAccountApplicationView> {

    private CompositeDisposable compositeDisposable;
    private DataManager dataManager;

    /**
     * Initialises the SavingAccountApplicationPresenter by automatically injecting an instance of
     * {@link DataManager} and {@link Context}.
     *
     * @param dataManager DataManager class that provides access to the data
     *                    via the API.
     * @param context     Context of the view attached to the presenter. In this case
     *                    it is that of an {@link androidx.appcompat.app.AppCompatActivity}
     */
    @Inject
    public SavingsAccountApplicationPresenter(DataManager dataManager,
                                              @ApplicationContext Context context) {
        super(context);
        this.dataManager = dataManager;
        compositeDisposable = new CompositeDisposable();
    }

    @Override
    public void attachView(SavingsAccountApplicationView mvpView) {
        super.attachView(mvpView);
    }

    @Override
    public void detachView() {
        super.detachView();
        compositeDisposable.clear();
    }

    /**
     * Used to load {@link SavingsAccountTemplate} from the remote server according to the
     * clientId and SavingsAccountState provided. Notifies the view to display on success and also
     * in case of any errors that occurred
     *
     * @param clientId Specifies the unique id of the current client that can be used to uniquely
     *                 fetch data from the server
     * @param state Instance of {@link SavingsAccountState} that specifies whether to show new
     *              template or update template
     */
    public void loadSavingsAccountApplicationTemplate(long clientId,
                                                      final SavingsAccountState state) {
        checkViewAttached();
        getMvpView().showProgress();
        compositeDisposable.add(dataManager.getSavingAccountApplicationTemplate(clientId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<SavingsAccountTemplate>() {

                    @Override
                    public void onNext(SavingsAccountTemplate template) {
                        getMvpView().hideProgress();

                        if (state == SavingsAccountState.CREATE) {
                            getMvpView().showUserInterfaceSavingAccountApplication(template);
                        } else {
                            getMvpView().showUserInterfaceSavingAccountUpdate(template);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        getMvpView().hideProgress();
                        getMvpView().showError(e.getMessage());
                    }

                    @Override
                    public void onComplete() {

                    }
                }));
    }

    public void submitSavingsAccountApplication(SavingsAccountApplicationPayload payload) {
        checkViewAttached();
        getMvpView().showProgress();
        compositeDisposable.add(dataManager.submitSavingAccountApplication(payload)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<ResponseBody>() {
                    @Override
                    public void onNext(ResponseBody responseBody) {
                        getMvpView().hideProgress();
                    }

                    @Override
                    public void onError(Throwable e) {
                        getMvpView().hideProgress();
                        getMvpView().showError(e.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        getMvpView().showSavingsAccountApplicationSuccessfully();

                    }
                }));
    }

    public void updateSavingsAccount(String accountId, SavingsAccountUpdatePayload payload) {
        checkViewAttached();
        getMvpView().showProgress();
        compositeDisposable.add(dataManager.updateSavingsAccount(accountId, payload)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<ResponseBody>() {
                    @Override
                    public void onNext(ResponseBody responseBody) {
                        getMvpView().hideProgress();
                    }

                    @Override
                    public void onError(Throwable e) {
                        getMvpView().hideProgress();
                        getMvpView().showError(e.getMessage());

                    }

                    @Override
                    public void onComplete() {
                        getMvpView().showSavingsAccountUpdateSuccessfully();
                    }
                }));

    }
}
