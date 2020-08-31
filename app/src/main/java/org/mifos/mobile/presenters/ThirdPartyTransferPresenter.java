package org.mifos.mobile.presenters;

import android.content.Context;

import org.mifos.mobile.R;
import org.mifos.mobile.api.DataManager;
import org.mifos.mobile.injection.ApplicationContext;
import org.mifos.mobile.models.AccountOptionAndBeneficiary;
import org.mifos.mobile.models.beneficiary.Beneficiary;
import org.mifos.mobile.models.beneficiary.BeneficiaryDetail;
import org.mifos.mobile.models.payload.AccountDetail;
import org.mifos.mobile.models.templates.account.AccountOption;
import org.mifos.mobile.models.templates.account.AccountOptionsTemplate;
import org.mifos.mobile.presenters.base.BasePresenter;
import org.mifos.mobile.ui.views.ThirdPartyTransferView;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;


/**
 * Created by dilpreet on 21/6/17.
 */

public class ThirdPartyTransferPresenter extends BasePresenter<ThirdPartyTransferView> {

    private DataManager dataManager;
    private CompositeDisposable compositeDisposable;

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
    public ThirdPartyTransferPresenter(DataManager dataManager,
                                       @ApplicationContext Context context) {
        super(context);
        this.dataManager = dataManager;
        compositeDisposable = new CompositeDisposable();
    }

    @Override
    public void attachView(ThirdPartyTransferView mvpView) {
        super.attachView(mvpView);
    }

    @Override
    public void detachView() {
        super.detachView();
        compositeDisposable.clear();
    }

    /**
     * Fetches {@link AccountOptionsTemplate} and {@link List} of {@link Beneficiary} from server
     * and notifies the view to display them. And in case of any error during fetching the required
     * details it notifies the view.
     */
    public void loadTransferTemplate() {

        checkViewAttached();
        getMvpView().showProgress();

        compositeDisposable.add(Observable.zip(dataManager.getThirdPartyTransferTemplate(),
                dataManager.getBeneficiaryList(),
                new BiFunction<AccountOptionsTemplate, List<Beneficiary>,
                        AccountOptionAndBeneficiary>() {
                    @Override
                    public AccountOptionAndBeneficiary apply(AccountOptionsTemplate
                                                                     accountOptionsTemplate,
                                                             List<Beneficiary> beneficiaries) {
                        return new AccountOptionAndBeneficiary(accountOptionsTemplate,
                                beneficiaries);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribeWith(new DisposableObserver<AccountOptionAndBeneficiary>() {
                    @Override
                    public void onComplete() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        getMvpView().hideProgress();
                        getMvpView().showError(context.getString(
                                R.string.error_fetching_third_party_transfer_template));
                    }

                    @Override
                    public void onNext(AccountOptionAndBeneficiary accountOptionAndBeneficiary) {
                        getMvpView().hideProgress();
                        getMvpView().showThirdPartyTransferTemplate(accountOptionAndBeneficiary.
                                getAccountOptionsTemplate());
                        getMvpView().showBeneficiaryList(accountOptionAndBeneficiary.
                                getBeneficiaryList());
                    }
                }));
    }

    /**
     * Retrieving {@link List} of {@code accountNumbers} from {@link List} of {@link AccountOption}
     *
     * @param accountOptions {@link List} of {@link AccountOption}
     * @return Returns {@link List} containing {@code accountNumbers}
     */
    public List<AccountDetail> getAccountNumbersFromAccountOptions(List<AccountOption>
                                                                           accountOptions) {
        final List<AccountDetail> accountNumber = new ArrayList<>();
        Observable.fromIterable(accountOptions)
                .filter(new Predicate<AccountOption>() {
                    @Override
                    public boolean test(AccountOption accountOption) throws Exception {
                        return !accountOption.getAccountType().getCode().equals(context.
                                getString(R.string.account_type_loan));
                    }
                })
                .flatMap(new Function<AccountOption, Observable<AccountDetail>>() {
                    @Override
                    public Observable<AccountDetail> apply(AccountOption accountOption) {
                        return Observable.just(new AccountDetail(accountOption.getAccountNo(),
                                accountOption.getAccountType().getValue()));
                    }
                })
                .subscribe(new Consumer<AccountDetail>() {
                    @Override
                    public void accept(AccountDetail accountDetail) throws Exception {
                        accountNumber.add(accountDetail);

                    }
                });
        return accountNumber;
    }

    /**
     * Retrieving {@link List} of {@code accountNumbers} from {@link List} of {@link Beneficiary}
     *
     * @param beneficiaries {@link List} of {@link Beneficiary}
     * @return Returns {@link List} containing {@code accountNumbers}
     */
    public List<BeneficiaryDetail> getAccountNumbersFromBeneficiaries(final List<Beneficiary>
                                                                              beneficiaries) {
        final List<BeneficiaryDetail> accountNumbers = new ArrayList<>();
        Observable.fromIterable(beneficiaries)
                .flatMap(new Function<Beneficiary, Observable<BeneficiaryDetail>>() {
                    @Override
                    public Observable<BeneficiaryDetail> apply(Beneficiary beneficiary) {
                        return Observable.just(new BeneficiaryDetail(beneficiary.getAccountNumber(),
                                beneficiary.getName(), beneficiary.getTransferLimit().toString()));
                    }
                })
                .subscribe(new Consumer<BeneficiaryDetail>() {
                    @Override
                    public void accept(BeneficiaryDetail beneficiaryDetail) throws Exception {
                        accountNumbers.add(beneficiaryDetail);

                    }
                });
        return accountNumbers;
    }

    /**
     * Searches for a {@link AccountOption} with provided {@code accountNo} from {@link List} of
     * {@link AccountOption} and returns it.
     *
     * @param accountOptions {@link List} of {@link AccountOption}
     * @param accountNo      Account Number which needs to searched in {@link List} of
     *                       {@link AccountOption}
     * @return Returns {@link AccountOption} which has Account Number same as the provided
     * {@code accountNo} in function parameter.
     */
    public AccountOption searchAccount(List<AccountOption> accountOptions, final String accountNo) {
        final AccountOption[] account = {new AccountOption()};
        Observable.fromIterable(accountOptions)
                .filter(new Predicate<AccountOption>() {
                    @Override
                    public boolean test(AccountOption accountOption) {
                        return accountOption.getAccountNo().equals(accountNo);
                    }
                })
                .subscribe(new Consumer<AccountOption>() {
                    @Override
                    public void accept(AccountOption accountOption) throws Exception {
                        account[0] = accountOption;
                    }
                });
        return account[0];
    }
}