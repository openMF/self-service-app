package org.mifos.mobile.presenters;

import android.content.Context;

import org.mifos.mobile.R;
import org.mifos.mobile.api.DataManager;
import org.mifos.mobile.injection.ApplicationContext;
import org.mifos.mobile.models.CheckboxStatus;
import org.mifos.mobile.models.accounts.loan.LoanAccount;
import org.mifos.mobile.models.accounts.savings.SavingAccount;
import org.mifos.mobile.models.accounts.share.ShareAccount;
import org.mifos.mobile.models.client.ClientAccounts;
import org.mifos.mobile.presenters.base.BasePresenter;
import org.mifos.mobile.ui.views.AccountsView;
import org.mifos.mobile.utils.Constants;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Predicate;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;


/**
 * Created by Rajan Maurya on 23/10/16.
 */

public class AccountsPresenter extends BasePresenter<AccountsView> {

    private final DataManager dataManager;
    private CompositeDisposable compositeDisposable;

    /**
     * Initialises the AccountsPresenter by automatically injecting an instance of
     * {@link Context} and {@link DataManager} .
     *
     * @param context     Context of the view attached to the presenter.
     * @param dataManager DataManager class that provides access to the data
     *                    via the API.
     */
    @Inject
    public AccountsPresenter(@ApplicationContext Context context, DataManager dataManager) {
        super(context);
        this.dataManager = dataManager;
        compositeDisposable = new CompositeDisposable();
    }

    @Override
    public void attachView(AccountsView mvpView) {
        super.attachView(mvpView);
    }

    @Override
    public void detachView() {
        super.detachView();
        compositeDisposable.clear();
    }

    /**
     * Loads savings, loan and share accounts associated with the Client from the server
     * and notifies the view to display it. And in case of any error during fetching the required
     * details it notifies the view.
     */
    public void loadClientAccounts() {
        checkViewAttached();
        getMvpView().showProgress();
        compositeDisposable.add(dataManager.getClientAccounts()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribeWith(new DisposableObserver<ClientAccounts>() {
                    @Override
                    public void onComplete() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        getMvpView().hideProgress();
                        getMvpView().showError(context.getString(R.string.error_fetching_accounts));
                    }

                    @Override
                    public void onNext(ClientAccounts clientAccounts) {
                        getMvpView().hideProgress();
                        getMvpView().showSavingsAccounts(clientAccounts.getSavingsAccounts());
                        getMvpView().showLoanAccounts(clientAccounts.getLoanAccounts());
                        getMvpView().showShareAccounts(clientAccounts.getShareAccounts());
                    }
                })
        );
    }

    /**
     * Loads savings, loan or share account depending upon {@code accountType} provided from the
     * server and notifies the view to display it.And in case of any error during fetching the
     * required details it notifies the view.
     * @param accountType Type of account for which we need to fetch details
     */
    public void loadAccounts(final String accountType, final boolean wantRecurring) {
        checkViewAttached();
        getMvpView().showProgress();
        compositeDisposable.add(dataManager.getAccounts(accountType)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribeWith(new DisposableObserver<ClientAccounts>() {
                    @Override
                    public void onComplete() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        getMvpView().hideProgress();
                        getMvpView().showError(context.getString(R.string.error_fetching_accounts));
                    }

                    @Override
                    public void onNext(ClientAccounts clientAccounts) {
                        getMvpView().hideProgress();
                        switch (accountType) {
                            case Constants.SAVINGS_ACCOUNTS:
                                if (wantRecurring) {
                                    getMvpView().showSavingsAccounts(
                                            clientAccounts.recurringSavingsAccounts());
                                } else {
                                    getMvpView().showSavingsAccounts(
                                            clientAccounts.nonRecurringSavingsAccounts()
                                    );
                                }
                                break;
                            case Constants.LOAN_ACCOUNTS:
                                getMvpView().showLoanAccounts(clientAccounts.getLoanAccounts());
                                break;
                            case Constants.SHARE_ACCOUNTS:
                                getMvpView().showShareAccounts(clientAccounts.getShareAccounts());
                                break;
                        }
                    }
                })
        );
    }

    /**
     * Filters {@link List} of {@link SavingAccount}
     * @param accounts {@link List} of {@link SavingAccount}
     * @param input {@link String} which is used for filtering
     * @return Returns {@link List} of filtered {@link SavingAccount} according to the {@code input}
     * provided.
     */
    public List<SavingAccount> searchInSavingsList(List<SavingAccount> accounts,
                                            final String input) {
        return Observable.fromIterable(accounts)
                .filter(new Predicate<SavingAccount>() {
                    @Override
                    public boolean test(SavingAccount savingAccount) throws Exception {
                        return (savingAccount.getProductName().toLowerCase().contains(input.
                                toLowerCase()) ||
                                savingAccount.getAccountNo().toLowerCase().contains(input.
                                        toLowerCase()));
                    }
                    }).toList().blockingGet();
    }

    /**
     * Filters {@link List} of {@link LoanAccount}
     * @param accounts {@link List} of {@link LoanAccount}
     * @param input {@link String} which is used for filtering
     * @return Returns {@link List} of filtered {@link LoanAccount} according to the {@code input}
     * provided.
     */
    public List<LoanAccount> searchInLoanList(List<LoanAccount> accounts,
                                                   final String input) {
        return Observable.fromIterable(accounts)
                .filter(new Predicate<LoanAccount>() {
                    @Override
                    public boolean test(LoanAccount loanAccount) throws Exception {
                        return (loanAccount.getProductName().toLowerCase().contains(input.
                                toLowerCase()) ||
                                loanAccount.getAccountNo().toLowerCase().contains(input.
                                        toLowerCase()));
                    }
                }).toList().blockingGet();
    }

    /**
     * Filters {@link List} of {@link ShareAccount}
     * @param accounts {@link List} of {@link ShareAccount}
     * @param input {@link String} which is used for filtering
     * @return Returns {@link List} of filtered {@link ShareAccount} according to the {@code input}
     * provided.
     */
    public List<ShareAccount> searchInSharesList(List<ShareAccount> accounts,
                                                   final String input) {
        return Observable.fromIterable(accounts)
                .filter(new Predicate<ShareAccount>() {
                    @Override
                    public boolean test(ShareAccount shareAccount) throws Exception {
                        return (shareAccount.getProductName().toLowerCase().contains(input.
                                toLowerCase()) ||
                                shareAccount.getAccountNo().toLowerCase().contains(input.
                                        toLowerCase()));
                    }
                }).toList().blockingGet();
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
     * Filters {@link List} of {@link SavingAccount} according to {@link CheckboxStatus}
     * @param accounts {@link List} of filtered {@link SavingAccount}
     * @param status Used for filtering the {@link List}
     * @return Returns {@link List} of filtered {@link SavingAccount} according to the
     * {@code status} provided.
     */
    public List<SavingAccount> getFilteredSavingsAccount(List<SavingAccount> accounts,
                                                         final CheckboxStatus status) {
        return Observable.fromIterable(accounts)
                .filter(new Predicate<SavingAccount>() {
                    @Override
                    public boolean test(SavingAccount account) {
                        if (status.getStatus().compareTo(context.getString(R.string.
                                        active)) == 0 && account.getStatus().getActive()) {
                            return true;
                        } else if (status.getStatus().compareTo(context.getString(R.string.
                                approved)) == 0 && account.getStatus().getApproved()) {
                            return true;
                        } else if (status.getStatus().compareTo(context.getString(R.string.
                                approval_pending)) == 0 && account.getStatus().
                                getSubmittedAndPendingApproval()) {
                            return true;
                        } else if (status.getStatus().compareTo(context.getString(R.string.
                                matured)) == 0 && account.getStatus().getMatured()) {
                            return true;
                        } else if (status.getStatus().compareTo(context.getString(R.string.
                                closed)) == 0 && account.getStatus().getClosed()) {
                            return true;
                        }
                        return false;
                    }
                }).toList().blockingGet();
    }

    /**
     * Filters {@link List} of {@link LoanAccount} according to {@link CheckboxStatus}
     * @param accounts {@link List} of filtered {@link LoanAccount}
     * @param status Used for filtering the {@link List}
     * @return Returns {@link List} of filtered {@link LoanAccount} according to the
     * {@code status} provided.
     */
    public List<LoanAccount> getFilteredLoanAccount(List<LoanAccount> accounts,
                                                         final CheckboxStatus status) {
        return Observable.fromIterable(accounts)
                .filter(new Predicate<LoanAccount>() {
                    @Override
                    public boolean test(LoanAccount account) {
                        if (status.getStatus().compareTo(context.getString(R.string.in_arrears))
                                == 0 && account.getInArrears()) {
                            return true;
                        } else if (status.getStatus().compareTo(context.getString(R.string.
                                active)) == 0 && account.getStatus().getActive()) {
                            return true;
                        } else if (status.getStatus().compareTo(context.getString(R.string.
                                waiting_for_disburse)) == 0 && account.getStatus().
                                getWaitingForDisbursal()) {
                            return true;
                        } else if (status.getStatus().compareTo(context.getString(R.string.
                                approval_pending)) == 0 && account.getStatus().
                                getPendingApproval()) {
                            return true;
                        } else if (status.getStatus().compareTo(context.getString(R.string.
                                overpaid)) == 0 && account.getStatus().getOverpaid()) {
                            return true;
                        } else if (status.getStatus().compareTo(context.getString(R.string.
                                closed)) == 0 && account.getStatus().getClosed()) {
                            return true;
                        } else if (status.getStatus().compareTo(context.getString(R.string.
                                withdrawn)) == 0 && account.getStatus().isLoanTypeWithdrawn()) {
                            return true;
                        }
                        return false;
                    }
                }).toList().blockingGet();
    }

    /**
     * Filters {@link List} of {@link ShareAccount} according to {@link CheckboxStatus}
     * @param accounts {@link List} of filtered {@link ShareAccount}
     * @param status Used for filtering the {@link List}
     * @return Returns {@link List} of filtered {@link ShareAccount} according to the
     * {@code status} provided.
     */
    public List<ShareAccount> getFilteredShareAccount(List<ShareAccount> accounts,
                                                        final CheckboxStatus status) {
        return Observable.fromIterable(accounts)
                .filter(new Predicate<ShareAccount>() {
                    @Override
                    public boolean test(ShareAccount account) {
                        if (status.getStatus().compareTo(context.getString(R.string.active)) == 0 &&
                                account.getStatus().getActive()) {
                            return true;
                        } else if (status.getStatus().compareTo(context.getString(R.string.
                                approved)) == 0 && account.getStatus().getApproved()) {
                            return true;
                        } else if (status.getStatus().compareTo(context.getString(R.string.
                                approval_pending)) == 0 && account.getStatus().
                                getSubmittedAndPendingApproval()) {
                            return true;
                        } else if (status.getStatus().compareTo(context.getString(R.string.
                                closed)) == 0 && account.getStatus().getClosed()) {
                            return true;
                        }
                        return false;
                    }
                }).toList().blockingGet();
    }

}
