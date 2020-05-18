package org.mifos.mobile.presenters;

import android.content.Context;

import org.mifos.mobile.R;
import org.mifos.mobile.api.DataManager;
import org.mifos.mobile.injection.ActivityContext;
import org.mifos.mobile.models.Charge;
import org.mifos.mobile.presenters.base.BasePresenter;
import org.mifos.mobile.ui.views.ChargeDetailsView;
import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;



public class ChargeDetailsPresenter extends BasePresenter<ChargeDetailsView> {

    private final DataManager dataManager;
    private CompositeDisposable compositeDisposable;

    /**
     * Initialises the ClientDetailsPresenter by automatically injecting an instance of
     * {@link DataManager} and {@link Context}.
     *
     * @param dataManager DataManager class that provides access to the data
     *                    via the API.
     * @param context     Context of the view attached to the presenter. In this case
     *                    it is that of an {@link androidx.appcompat.app.AppCompatActivity}
     */

    @Inject
    public ChargeDetailsPresenter(DataManager dataManager, @ActivityContext Context context) {
        super(context);
        this.dataManager = dataManager;
        compositeDisposable = new CompositeDisposable();
    }

    @Override
    public void attachView(ChargeDetailsView mvpView) {
        super.attachView(mvpView);
    }

    @Override
    public void detachView() {
        super.detachView();
        compositeDisposable.clear();
    }

    public void loadClientChargeDetails(int chargeId) {
        checkViewAttached();
        getMvpView().showProgress();
        compositeDisposable.add(dataManager.getClientChargeDetails(chargeId)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribeWith(new DisposableObserver<Charge>() {
                    @Override
                    public void onComplete() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        getMvpView().hideProgress();
                        getMvpView().showErrorFetchingChargeDetails(
                                context.getString(R.string.charge_detail));
                    }

                    @Override
                    public void onNext(Charge clientCharge) {
                        getMvpView().hideProgress();
                        if (clientCharge != null) {
                            getMvpView().hideProgress();
                            getMvpView().showChargeDetails(clientCharge);
                        }
                    }
                })
        );
    }


    public void loadLoanChargeDetails(long loanId, int chargeId) {
        checkViewAttached();
        getMvpView().showProgress();
        compositeDisposable.add(dataManager.getLoanChargeDetails(loanId, chargeId)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribeWith(new DisposableObserver<Charge>() {
                    @Override
                    public void onComplete() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        getMvpView().hideProgress();
                        getMvpView().showErrorFetchingChargeDetails(
                                context.getString(R.string.charge_detail));
                    }

                    @Override
                    public void onNext(Charge loanCharge) {
                        getMvpView().hideProgress();
                        if (loanCharge != null) {
                            getMvpView().hideProgress();
                            getMvpView().showChargeDetails(loanCharge);
                        }
                    }
                })
        );
    }


    public void loadSavingsChargeDetails(long accountId, int chargeId) {
        checkViewAttached();
        getMvpView().showProgress();
        compositeDisposable.add(dataManager.getSavingsChargeDetails(accountId, chargeId)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribeWith(new DisposableObserver<Charge>() {
                    @Override
                    public void onComplete() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        getMvpView().hideProgress();
                        getMvpView().showErrorFetchingChargeDetails(
                                context.getString(R.string.charge_detail));
                    }

                    @Override
                    public void onNext(Charge savingsCharge) {
                        getMvpView().hideProgress();
                        if (savingsCharge != null) {
                            getMvpView().hideProgress();
                            getMvpView().showChargeDetails(savingsCharge);
                        }
                    }
                })
        );
    }

}