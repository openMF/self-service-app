package org.mifos.mobile.presenters;

import android.content.Context;

import org.mifos.mobile.R;
import org.mifos.mobile.api.DataManager;
import org.mifos.mobile.injection.ActivityContext;
import org.mifos.mobile.models.notification.MifosNotification;
import org.mifos.mobile.presenters.base.BasePresenter;
import org.mifos.mobile.ui.views.NotificationView;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by dilpreet on 14/9/17.
 */

public class NotificationPresenter extends BasePresenter<NotificationView> {

    private DataManager manager;
    private CompositeDisposable compositeDisposable;

    /**
     * Initialises the LoginPresenter by automatically injecting an instance of
     * {@link DataManager} and {@link Context}.
     *
     * @param manager DataManager class that provides access to the data
     *                via the API.
     * @param context Context of the view attached to the presenter. In this case
     *                it is that of an {@link androidx.appcompat.app.AppCompatActivity}
     */
    @Inject
    public NotificationPresenter(DataManager manager, @ActivityContext Context context) {
        super(context);
        this.manager = manager;
        compositeDisposable = new CompositeDisposable();
    }

    @Override
    public void attachView(NotificationView mvpView) {
        super.attachView(mvpView);
    }

    @Override
    public void detachView() {
        super.detachView();
        compositeDisposable.clear();
    }

    /**
     * Used to load {@link List} of {@link MifosNotification} from the local sql database and
     * notify the view to display it. Notify the view, in case there is any errors
     */
    public void loadNotifications() {

        checkViewAttached();
        getMvpView().showProgress();
        compositeDisposable.add(manager.getNotifications()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribeWith(new DisposableObserver<List<MifosNotification>>() {
                    @Override
                    public void onComplete() { }

                    @Override
                    public void onError(Throwable e) {
                        getMvpView().hideProgress();
                        getMvpView().showError(context
                                .getString(R.string.notification));
                    }

                    @Override
                    public void onNext(List<MifosNotification> notificationModels) {
                        getMvpView().hideProgress();
                        getMvpView().showNotifications(notificationModels);
                    }
                }));
    }
}
