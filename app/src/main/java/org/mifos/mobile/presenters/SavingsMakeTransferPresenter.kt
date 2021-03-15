package org.mifos.mobile.presenters

import android.content.Context

import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers

import org.mifos.mobile.R
import org.mifos.mobile.api.DataManager
import org.mifos.mobile.injection.ApplicationContext
import org.mifos.mobile.models.accounts.savings.SavingsWithAssociations
import org.mifos.mobile.models.payload.AccountDetail
import org.mifos.mobile.models.templates.account.AccountOption
import org.mifos.mobile.models.templates.account.AccountOptionsTemplate
import org.mifos.mobile.presenters.base.BasePresenter
import org.mifos.mobile.ui.views.SavingsMakeTransferMvpView
import org.mifos.mobile.utils.Constants

import java.util.*
import javax.inject.Inject

/**
 * Created by Rajan Maurya on 10/03/17.
 */
class SavingsMakeTransferPresenter @Inject constructor(
        val dataManager: DataManager?,
        @ApplicationContext context: Context?
) : BasePresenter<SavingsMakeTransferMvpView?>(context) {
    private var compositeDisposables: CompositeDisposable = CompositeDisposable()

    override fun detachView() {
        super.detachView()
        compositeDisposables.clear()
    }

    /**
     * Fetches [AccountOptionsTemplate] from server and notifies the view to display it. And
     * in case of any error during fetching the required details it notifies the view.
     */
    fun loanAccountTransferTemplate() {
        checkViewAttached()
        mvpView?.showProgress()
        dataManager?.accountTransferTemplate
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribeOn(Schedulers.io())
                ?.subscribeWith(object : DisposableObserver<AccountOptionsTemplate?>() {
                    override fun onComplete() {}
                    override fun onError(e: Throwable) {
                        mvpView?.hideProgress()
                        mvpView?.showError(context?.getString(
                                R.string.error_fetching_account_transfer_template))
                    }

                    override fun onNext(accountOptionsTemplate: AccountOptionsTemplate) {
                        mvpView?.hideProgress()
                        mvpView?.showSavingsAccountTemplate(accountOptionsTemplate)
                    }
                })?.let {
                    compositeDisposables.add(it
                    )
                }
    }

    /**
     * Retrieving [List] of `accountNo` from [List] of [AccountOption]
     *
     * @param accountOptions [List] of [AccountOption]
     * @return Returns [List] containing `accountNo`
     */
    fun getAccountNumbers(
            accountOptions: List<AccountOption>?,
            isTypePayFrom: Boolean
    ): List<AccountDetail> {
        val accountNumber: MutableList<AccountDetail> = ArrayList()
        Observable.fromIterable(accountOptions)
                .filter { (_, _, accountType) -> !(accountType?.code == context?.getString(R.string.account_type_loan) && isTypePayFrom) }
                .flatMap { (_, accountNo, accountType) ->
                    Observable.just(AccountDetail(accountNo!!,
                            accountType?.value!!))
                }
                .subscribe { accountDetail -> accountNumber.add(accountDetail) }
        return accountNumber
    }

    /**
     * Searches for a [AccountOption] with provided `accountId` from [List] of
     * [AccountOption] and returns it.
     *
     * @param accountOptions [List] of [AccountOption]
     * @param accountId      AccountId which needs to searched in [List] of [                       ]
     * @return Returns [AccountOption] which has accountId same as the provided
     * `accountId` in function parameter.
     */
    fun searchAccount(accountOptions: List<AccountOption>?, accountId: Long?): AccountOption {
        val accountOption = arrayOf(AccountOption())
        Observable.fromIterable(accountOptions)
                .filter { (accountId1) -> accountId == accountId1?.toLong() }
                .subscribe { account -> accountOption[0] = account }
        return accountOption[0]
    }

    fun getBalanceForAccountID(accountId: Long?) {
        checkViewAttached()
        mvpView?.showProgress()
        dataManager?.getSavingsWithAssociations(accountId,
        Constants.TRANSACTIONS)
        ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribeOn(Schedulers.io())
                ?.subscribeWith(object : DisposableObserver<SavingsWithAssociations?>() {
                    override fun onComplete() {}
                    override fun onError(e: Throwable) {
                        mvpView?.hideProgress()
                        mvpView?.showError(
                                context?.getString(R.string.error_saving_account_details_loading))
                    }

                    override fun onNext(savingAccount: SavingsWithAssociations) {
                        mvpView?.hideProgress()
                        mvpView?.updateAccountBalance(savingAccount.summary?.accountBalance!!)
                    }
                })?.let {
                    compositeDisposables.add(it
                    )
                }
    }
}