package org.mifos.mobile.presenters

import android.content.Context

import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Predicate
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers

import org.mifos.mobile.R
import org.mifos.mobile.api.DataManager
import org.mifos.mobile.injection.ApplicationContext
import org.mifos.mobile.models.CheckboxStatus
import org.mifos.mobile.models.accounts.savings.SavingsWithAssociations
import org.mifos.mobile.models.accounts.savings.Transactions
import org.mifos.mobile.presenters.base.BasePresenter
import org.mifos.mobile.ui.views.SavingAccountsTransactionView
import org.mifos.mobile.utils.Constants
import org.mifos.mobile.utils.DateHelper.getDateAsLongFromList

import javax.inject.Inject


/**
 * Created by dilpreet on 6/3/17.
 */
class SavingAccountsTransactionPresenter @Inject constructor(
        private val dataManager: DataManager?,
        @ApplicationContext context: Context?
) : BasePresenter<SavingAccountsTransactionView?>(context) {

    private val compositeDisposables: CompositeDisposable = CompositeDisposable()

    override fun detachView() {
        super.detachView()
        compositeDisposables.clear()
    }

    /**
     * Filters [List] of [CheckboxStatus]
     * @param statusModelList [List] of [CheckboxStatus]
     * @return Returns [List] of [CheckboxStatus] which have
     * `checkboxStatus.isChecked()` as true.
     */
    fun getCheckedStatus(statusModelList: List<CheckboxStatus?>?): List<CheckboxStatus?>? {
        return Observable.fromIterable(statusModelList)
                .filter { (_, _, isChecked) -> isChecked }.toList().blockingGet()
    }

    /**
     * Load details of a particular saving account from the server and notify the view
     * to display it. Notify the view, in case there is any error in fetching
     * the details from server.
     *
     * @param accountId Id of Savings Account
     */
    fun loadSavingsWithAssociations(accountId: Long) {
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
                        mvpView?.showErrorFetchingSavingAccountsDetail(
                                context?.getString(R.string.saving_account_details))
                    }

                    override fun onNext(savingAccount: SavingsWithAssociations) {
                        mvpView?.hideProgress()
                        mvpView?.showSavingAccountsDetail(savingAccount)
                    }
                })?.let {
                    compositeDisposables.add(it
                    )
                }
    }

    /**
     * Used for filtering [List] of [Transactions] according to `startDate` and
     * `lastDate`
     *
     * @param savingAccountsTransactionList [List] of [Transactions]
     * @param startDate                     Starting date for filtering
     * @param lastDate                      Last date for filtering
     */
    fun filterTransactionList(
            savingAccountsTransactionList: List<Transactions?>?,
            startDate: Long?, lastDate: Long?
    ) {
        val list = if (startDate != null && lastDate != null)
            Observable.fromIterable(savingAccountsTransactionList)
                    .filter { (_, _, _, _, date) ->
                        (getDateAsLongFromList(date) in startDate..lastDate)
                    }
                    .toList().blockingGet()
        else null
        mvpView?.showFilteredList(list)
    }

    /**
     * Filters [List] of [Transactions] according to [CheckboxStatus]
     * @param savingAccountsTransactionList [List] of filtered [Transactions]
     * @param status Used for filtering the [List]
     * @return Returns [List] of filtered [Transactions] according to the
     * `status` provided.
     */
    fun filterTranactionListbyType(
            savingAccountsTransactionList: List<Transactions?>?, status: CheckboxStatus?
    ): Collection<Transactions?>? {
        return Observable.fromIterable(savingAccountsTransactionList)
                .filter(Predicate { (_, transactionType) ->
                    if (context?.getString(R.string.deposit)?.let { status?.status?.compareTo(it) } == 0
                            && transactionType?.deposit!!) {
                        return@Predicate true
                    } else if (context?.getString(R.string.dividend_payout)?.let {
                                status?.status?.compareTo(
                                        it)
                            } == 0 &&
                            transactionType?.dividendPayout!!) {
                        return@Predicate true
                    } else if (context?.getString(R.string.withdrawal)?.let {
                                status?.status?.compareTo(
                                        it)
                            } == 0 &&
                            transactionType?.withdrawal!!) {
                        return@Predicate true
                    } else if (context?.getString(R.string.interest_posting)?.let {
                                status?.status?.compareTo(
                                        it)
                            } == 0 &&
                            transactionType?.interestPosting!!) {
                        return@Predicate true
                    } else if (context?.getString(R.string.fee_deduction)?.let {
                                status?.status?.compareTo(
                                        it)
                            } == 0 &&
                            transactionType?.feeDeduction!!) {
                        return@Predicate true
                    } else if (context?.getString(R.string.withdrawal_transfer)?.let {
                                status?.status?.compareTo(
                                        it)
                            } == 0 &&
                            transactionType?.approveTransfer!!) {
                        return@Predicate true
                    } else if (context?.getString(
                                    R.string.rejected_transfer)?.let { status?.status?.compareTo(it) } == 0 && transactionType?.rejectTransfer!!) {
                        return@Predicate true
                    } else if (context?.getString(
                                    R.string.overdraft_fee)?.let { status?.status?.compareTo(it) } == 0 &&
                            transactionType?.overdraftFee!!) {
                        return@Predicate true
                    }
                    false
                }).toList().blockingGet()
    }

    fun filterTransactionListByAmount(
            savingAccountsTransactionList: List<Transactions?>?,
            min: Float?, max: Float?
    ): List<Transactions?>? {
        return if (min != null && max != null) {
            val list = mutableListOf<Transactions>()
            for (t in savingAccountsTransactionList!!) if(t?.amount!! >= min && t.amount!! <= max) list.add(t)
            return list
        }else null
    }

}