package org.mifos.mobile.presenters

import android.content.Context

import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers

import okhttp3.ResponseBody

import org.mifos.mobile.api.DataManager
import org.mifos.mobile.injection.ApplicationContext
import org.mifos.mobile.models.accounts.savings.SavingsAccountApplicationPayload
import org.mifos.mobile.models.accounts.savings.SavingsAccountUpdatePayload
import org.mifos.mobile.models.templates.savings.SavingsAccountTemplate
import org.mifos.mobile.presenters.base.BasePresenter
import org.mifos.mobile.ui.enums.SavingsAccountState
import org.mifos.mobile.ui.views.SavingsAccountApplicationView

import javax.inject.Inject

/*
* Created by saksham on 30/June/2018
*/
class SavingsAccountApplicationPresenter @Inject constructor(
        private val dataManager: DataManager?,
        @ApplicationContext context: Context?
) : BasePresenter<SavingsAccountApplicationView?>(context) {
    private val compositeDisposable: CompositeDisposable = CompositeDisposable()

    override fun detachView() {
        super.detachView()
        compositeDisposable.clear()
    }

    fun loadSavingsAccountApplicationTemplate(
            clientId: Long?,
            state: SavingsAccountState?
    ) {
        checkViewAttached()
        mvpView?.showProgress()
        dataManager?.getSavingAccountApplicationTemplate(clientId)
                ?.subscribeOn(Schedulers.io())
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribeWith(object : DisposableObserver<SavingsAccountTemplate?>() {
                    override fun onNext(template: SavingsAccountTemplate) {
                        mvpView?.hideProgress()
                        if (state === SavingsAccountState.CREATE) {
                            mvpView?.showUserInterfaceSavingAccountApplication(template)
                        } else {
                            mvpView?.showUserInterfaceSavingAccountUpdate(template)
                        }
                    }

                    override fun onError(e: Throwable) {
                        mvpView?.hideProgress()
                        mvpView?.showError(e.message)
                    }

                    override fun onComplete() {}
                })?.let { compositeDisposable.add(it) }
    }

    fun submitSavingsAccountApplication(payload: SavingsAccountApplicationPayload?) {
        checkViewAttached()
        mvpView?.showProgress()
        dataManager?.submitSavingAccountApplication(payload)
                ?.subscribeOn(Schedulers.io())
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribeWith(object : DisposableObserver<ResponseBody?>() {
                    override fun onNext(responseBody: ResponseBody) {
                        mvpView?.hideProgress()
                    }

                    override fun onError(e: Throwable) {
                        mvpView?.hideProgress()
                        mvpView?.showError(e.message)
                    }

                    override fun onComplete() {
                        mvpView?.showSavingsAccountApplicationSuccessfully()
                    }
                })?.let { compositeDisposable.add(it) }
    }

    fun updateSavingsAccount(accountId: Long?, payload: SavingsAccountUpdatePayload?) {
        checkViewAttached()
        mvpView?.showProgress()
        dataManager?.updateSavingsAccount(accountId, payload)
                ?.subscribeOn(Schedulers.newThread())
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribeWith(object : DisposableObserver<ResponseBody?>() {
                    override fun onNext(responseBody: ResponseBody) {
                        mvpView?.hideProgress()
                    }

                    override fun onError(e: Throwable) {
                        mvpView?.hideProgress()
                        mvpView?.showError(e.message)
                    }

                    override fun onComplete() {
                        mvpView?.showSavingsAccountUpdateSuccessfully()
                    }
                })?.let { compositeDisposable.add(it) }
    }

}