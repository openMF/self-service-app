package org.mifos.mobile.presenters;

import android.content.Context;

import org.mifos.mobile.R;
import org.mifos.mobile.api.DataManager;
import org.mifos.mobile.injection.ApplicationContext;
import org.mifos.mobile.models.CheckboxStatus;
import org.mifos.mobile.models.accounts.savings.SavingsWithAssociations;
import org.mifos.mobile.models.accounts.savings.Transactions;
import org.mifos.mobile.presenters.base.BasePresenter;
import org.mifos.mobile.ui.views.SavingAccountsTransactionView;
import org.mifos.mobile.utils.Constants;
import org.mifos.mobile.utils.DateHelper;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Predicate;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;


/**
 * Created by dilpreet on 6/3/17.
 */


public class SavingAccountsTransactionPresenter extends
        BasePresenter<SavingAccountsTransactionView> {

    private final DataManager dataManager;
    private CompositeDisposable compositeDisposables;

    /**
     * Initialises the SavingAccountsDetailPresenter by automatically injecting an instance of
     * {@link DataManager} and {@link Context}.
     *
     * @param dataManager DataManager class that provides access to the data
     *                    via the API.
     * @param context     Context of the view attached to the presenter. In this case
     *                    it is that of an {@link androidx.appcompat.app.AppCompatActivity}
     */
    @Inject
    public SavingAccountsTransactionPresenter(DataManager dataManager,
            @ApplicationContext Context context) {
        super(context);
        this.dataManager = dataManager;
        compositeDisposables = new CompositeDisposable();
    }

    @Override
    public void attachView(SavingAccountsTransactionView mvpView) {
        super.attachView(mvpView);
    }

    @Override
    public void detachView() {
        super.detachView();
        compositeDisposables.clear();
    }

    /**
     * Filters {@link List} of {@link CheckboxStatus}
     * @param statusModelList {@link List} of {@link CheckboxStatus}
     * @return Returns {@link List} of {@link CheckboxStatus} which have
     * {@code checkboxStatus.isChecked()} as true.
     */
    public List<CheckboxStatus> getCheckedStatus(List<CheckboxStatus> statusModelList) {
        return Observable.fromIterable(statusModelList)
                .filter(new Predicate<CheckboxStatus>() {

                    @Override
                    public boolean test(CheckboxStatus checkboxStatus) throws Exception {
                        return checkboxStatus.isChecked();
                    }
                }).toList().blockingGet();
    }


    /**
     * Load details of a particular saving account from the server and notify the view
     * to display it. Notify the view, in case there is any error in fetching
     * the details from server.
     *
     * @param accountId Id of Savings Account
     */
    public void loadSavingsWithAssociations(long accountId) {
        checkViewAttached();
        getMvpView().showProgress();
        compositeDisposables.add(dataManager.getSavingsWithAssociations(accountId,
                Constants.TRANSACTIONS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribeWith(new DisposableObserver<SavingsWithAssociations>() {
                    @Override
                    public void onComplete() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        getMvpView().hideProgress();
                        getMvpView().showErrorFetchingSavingAccountsDetail(
                                context.getString(R.string.saving_account_details));
                    }

                    @Override
                    public void onNext(SavingsWithAssociations savingAccount) {
                        getMvpView().hideProgress();
                        getMvpView().showSavingAccountsDetail(savingAccount);
                    }
                })
        );
    }

    /**
     * Used for filtering {@link List} of {@link Transactions} according to {@code startDate} and
     * {@code lastDate}
     *
     * @param savingAccountsTransactionList {@link List} of {@link Transactions}
     * @param startDate                     Starting date for filtering
     * @param lastDate                      Last date for filtering
     */
    public void filterTransactionList(List<Transactions> savingAccountsTransactionList,
            final long startDate, final long lastDate) {
        List<Transactions> list = Observable.fromIterable(savingAccountsTransactionList)
                .filter(new Predicate<Transactions>() {
                    @Override
                    public boolean test(Transactions transactions) {
                        return startDate <= DateHelper.getDateAsLongFromList(transactions.getDate())
                                && lastDate >= DateHelper.
                                getDateAsLongFromList(transactions.getDate());
                    }
                })
                .toList().blockingGet();

        getMvpView().showFilteredList(list);
    }

    /**
     * Filters {@link List} of {@link Transactions} according to {@link CheckboxStatus}
     * @param savingAccountsTransactionList {@link List} of filtered {@link Transactions}
     * @param status Used for filtering the {@link List}
     * @return Returns {@link List} of filtered {@link Transactions} according to the
     * {@code status} provided.
     */
    public  List<Transactions> filterTranactionListbyType(
            List<Transactions> savingAccountsTransactionList, final CheckboxStatus status) {

        return Observable.fromIterable(savingAccountsTransactionList)
                .filter(new Predicate<Transactions>() {
                    @Override
                    public boolean test(Transactions transactions) throws Exception {
                        if (status.getStatus().compareTo(context.getString(R.string.deposit)) == 0
                                && transactions.getTransactionType().getDeposit()) {
                            return true;
                        } else if (status.getStatus().compareTo(
                                context.getString(R.string.dividend_payout)) == 0 &&
                                transactions.getTransactionType().getDividendPayout()) {
                            return true;
                        } else if (status.getStatus().compareTo(
                                context.getString(R.string.withdrawal)) == 0 &&
                                transactions.getTransactionType().getWithdrawal()) {
                            return true;
                        } else if (status.getStatus().compareTo(
                                context.getString(R.string.interest_posting)) == 0 &&
                                transactions.getTransactionType().getInterestPosting()) {
                            return true;
                        } else if (status.getStatus().compareTo(
                                context.getString(R.string.fee_deduction)) == 0 &&
                                transactions.getTransactionType().getFeeDeduction()) {
                            return true;
                        } else if (status.getStatus().compareTo(
                                context.getString(R.string.withdrawal_transfer)) == 0 &&
                                transactions.getTransactionType().getApproveTransfer()) {
                            return true;
                        } else if (status.getStatus().compareTo(context.getString(
                                R.string.rejected_transfer)) == 0 && transactions
                                .getTransactionType().getRejectTransfer()) {
                            return true;
                        } else if (status.getStatus().compareTo(context.getString(
                                R.string.overdraft_fee)) == 0 &&
                                transactions.getTransactionType().getOverdraftFee()) {
                            return true;
                        }
                        return false;
                    }
                }).toList().blockingGet();
    }

}
