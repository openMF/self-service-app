package org.mifos.mobile.presenters;

import android.content.Context;

import org.mifos.mobile.R;
import org.mifos.mobile.api.DataManager;
import org.mifos.mobile.injection.ActivityContext;
import org.mifos.mobile.models.Charge;
import org.mifos.mobile.models.Page;
import org.mifos.mobile.presenters.base.BasePresenter;
import org.mifos.mobile.ui.views.ClientChargeView;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;


/**
 * @author Vishwajeet
 * @since 17/8/16.
 */
public class ClientChargePresenter extends BasePresenter<ClientChargeView> {

    private final DataManager dataManager;
    private CompositeDisposable compositeDisposable;

    /**
     * Initialises the ClientChargePresenter by automatically injecting an instance of
     * {@link DataManager} and {@link Context}.
     *
     * @param dataManager DataManager class that provides access to the data
     *                    via the API.
     * @param context     Context of the view attached to the presenter. In this case
     *                    it is that of an {@link androidx.appcompat.app.AppCompatActivity}
     */

    @Inject
    public ClientChargePresenter(DataManager dataManager, @ActivityContext Context context) {
        super(context);
        this.dataManager = dataManager;
        compositeDisposable = new CompositeDisposable();
    }

    @Override
    public void attachView(ClientChargeView mvpView) {
        super.attachView(mvpView);
    }

    @Override
    public void detachView() {
        super.detachView();
        compositeDisposable.clear();
    }

    /**
     * Used to load ClientCharges as a {@link Page} of {@link Charge} from server and notifies
     * the view to display it. And in case of any error during fetching the required details it
     * notifies the view.
     */
    public void loadClientCharges(long clientId) {
        checkViewAttached();
        getMvpView().showProgress();
        compositeDisposable.add(dataManager.getClientCharges(clientId)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribeWith(new DisposableObserver<Page<Charge>>() {
                    @Override
                    public void onComplete() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        getMvpView().hideProgress();
                        getMvpView().showErrorFetchingClientCharges(
                                context.getString(R.string.client_charges));
                    }

                    @Override
                    public void onNext(Page<Charge> chargePage) {
                        getMvpView().hideProgress();
                        if (chargePage != null) {
                            getMvpView().showClientCharges(chargePage.getPageItems());
                        }
                    }
                })
        );
    }

    /**
     * Used to load LoanAccountCharges as a {@link List} of {@link Charge} from server and notifies
     * the view to display it. And in case of any error during fetching the required details it
     * notifies the view.
     */
    public void loadLoanAccountCharges(long loanId) {
        checkViewAttached();
        getMvpView().showProgress();
        compositeDisposable.add(dataManager.getLoanCharges(loanId)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribeWith(new DisposableObserver<List<Charge>>() {
                    @Override
                    public void onComplete() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        getMvpView().hideProgress();
                        getMvpView().showErrorFetchingClientCharges(
                                context.getString(R.string.client_charges));
                    }

                    @Override
                    public void onNext(List<Charge> chargeList) {
                        getMvpView().hideProgress();
                        getMvpView().showClientCharges(chargeList);
                    }
                })
        );
    }

    /**
     * Used to load SavingsAccountCharges as a {@link List} of {@link Charge} from server and
     * notifies the view to display it. And in case of any error during fetching the
     * required details it notifies the view.
     */
    public void loadSavingsAccountCharges(long savingsId) {
        checkViewAttached();
        getMvpView().showProgress();
        compositeDisposable.add(dataManager.getSavingsCharges(savingsId)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribeWith(new DisposableObserver<List<Charge>>() {
                    @Override
                    public void onComplete() { }

                    @Override
                    public void onError(Throwable e) {
                        getMvpView().hideProgress();
                        getMvpView().showErrorFetchingClientCharges(
                                context.getString(R.string.client_charges));
                    }

                    @Override
                    public void onNext(List<Charge> chargeList) {
                        getMvpView().hideProgress();
                        getMvpView().showClientCharges(chargeList);
                    }
                })
        );
    }

    /**
     * Used to load ClientCharges as a {@link Page} of {@link Charge} from the local sql database
     * and notifies the view to display it. And in case of any error during getting the
     * required details it notifies the view.
     */
    public void loadClientLocalCharges() {
        checkViewAttached();
        compositeDisposable.add(dataManager.getClientLocalCharges()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribeWith(new DisposableObserver<Page<Charge>>() {
                    @Override
                    public void onComplete() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        getMvpView().showErrorFetchingClientCharges(
                                context.getString(R.string.client_charges));
                    }

                    @Override
                    public void onNext(Page<Charge> chargePage) {
                        getMvpView().showClientCharges(chargePage.getPageItems());
                    }
                })
        );
    }
}
