package org.mifos.mobile.api;

import org.mifos.mobile.FakeRemoteDataSource;
import org.mifos.mobile.api.local.DatabaseHelper;
import org.mifos.mobile.api.local.PreferencesHelper;
import org.mifos.mobile.models.Charge;
import org.mifos.mobile.models.client.Client;
import org.mifos.mobile.models.guarantor.GuarantorApplicationPayload;
import org.mifos.mobile.models.guarantor.GuarantorPayload;
import org.mifos.mobile.models.guarantor.GuarantorTemplatePayload;
import org.mifos.mobile.models.UpdatePasswordPayload;
import org.mifos.mobile.models.accounts.savings.SavingsAccountApplicationPayload;
import org.mifos.mobile.models.accounts.savings.SavingsAccountUpdatePayload;
import org.mifos.mobile.models.accounts.savings.SavingsAccountWithdrawPayload;
import org.mifos.mobile.models.notification.MifosNotification;
import org.mifos.mobile.models.notification.NotificationRegisterPayload;
import org.mifos.mobile.models.Page;
import org.mifos.mobile.models.Transaction;
import org.mifos.mobile.models.User;
import org.mifos.mobile.models.accounts.loan.LoanAccount;
import org.mifos.mobile.models.accounts.loan.LoanWithAssociations;
import org.mifos.mobile.models.accounts.loan.LoanWithdraw;
import org.mifos.mobile.models.accounts.savings.SavingsWithAssociations;
import org.mifos.mobile.models.beneficiary.Beneficiary;
import org.mifos.mobile.models.beneficiary.BeneficiaryPayload;
import org.mifos.mobile.models.beneficiary.BeneficiaryUpdatePayload;
import org.mifos.mobile.models.client.ClientAccounts;
import org.mifos.mobile.models.notification.NotificationUserDetail;
import org.mifos.mobile.models.payload.LoansPayload;
import org.mifos.mobile.models.payload.TransferPayload;
import org.mifos.mobile.models.register.RegisterPayload;
import org.mifos.mobile.models.register.UserVerify;
import org.mifos.mobile.models.templates.account.AccountOptionsTemplate;
import org.mifos.mobile.models.templates.beneficiary.BeneficiaryTemplate;
import org.mifos.mobile.models.templates.loans.LoanTemplate;
import org.mifos.mobile.models.templates.savings.SavingsAccountTemplate;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.functions.Function;
import okhttp3.MediaType;
import okhttp3.ResponseBody;


/**
 * @author Vishwajeet
 * @since 13/6/16.
 */
@Singleton
public class DataManager {

    private final PreferencesHelper preferencesHelper;
    private final BaseApiManager baseApiManager;
    private final DatabaseHelper databaseHelper;
    private long clientId;

    @Inject
    public DataManager(PreferencesHelper preferencesHelper, BaseApiManager baseApiManager,
            DatabaseHelper databaseHelper) {
        this.preferencesHelper = preferencesHelper;
        this.baseApiManager = baseApiManager;
        this.databaseHelper = databaseHelper;
        clientId = this.preferencesHelper.getClientId();
    }

    public Observable<User> login(String username, String password) {
        return baseApiManager.getAuthenticationApi().authenticate(username, password);
    }

    public Observable<Page<Client>> getClients() {
        return baseApiManager.getClientsApi().getClients();
    }

    public Observable<Client> getCurrentClient() {
        return baseApiManager.getClientsApi().getClientForId(clientId);
    }

    public Observable<ResponseBody> getClientImage() {
        return baseApiManager.getClientsApi().getClientImage(clientId);
    }

    public Observable<ClientAccounts> getClientAccounts() {
        return baseApiManager.getClientsApi().getClientAccounts(clientId);
    }

    public Observable<ClientAccounts> getAccounts(String accountType) {
        return baseApiManager.getClientsApi().getAccounts(clientId, accountType);
    }

    public Observable<Page<Transaction>> getRecentTransactions(int offset, int limit) {
        return baseApiManager.getRecentTransactionsApi()
                .getRecentTransactionsList(clientId, offset, limit);
    }

    public Observable<Page<Charge>> getClientCharges(long clientId) {
        return baseApiManager.getClientChargeApi().getClientChargeList(clientId)
                .concatMap(new Function<Page<Charge>, ObservableSource<? extends Page<Charge>>>() {
                    @Override
                    public Observable<? extends Page<Charge>> apply(Page<Charge> chargePage) {
                        return databaseHelper.syncCharges(chargePage);
                    }
                });
    }

    public Observable<Charge> getClientChargeDetails(int chargeId) {
        return baseApiManager.getClientChargeApi().getClientChargeDetails(clientId, chargeId);
    }

    public Observable<List<Charge>> getLoanCharges(long loanId) {
        return baseApiManager.getClientChargeApi().getLoanAccountChargeList(loanId);
    }

    public Observable<Charge> getLoanChargeDetails(long loanId, int chargeId) {
        return baseApiManager.getClientChargeApi().getLoanChargeDetails(loanId, chargeId);
    }

    public Observable<List<Charge>> getSavingsCharges(long savingsId) {
        return baseApiManager.getClientChargeApi().getSavingsAccountChargeList(savingsId);
    }

    public Observable<Charge> getSavingsChargeDetails(long accountId, int chargeId) {
        return baseApiManager.getClientChargeApi().getSavingsChargeDetails(accountId, chargeId);
    }

    public Observable<SavingsWithAssociations> getSavingsWithAssociations(long accountId,
            String associationType) {
        return baseApiManager
                .getSavingAccountsListApi().getSavingsWithAssociations(accountId, associationType);
    }

    public Observable<AccountOptionsTemplate> getAccountTransferTemplate() {
        return baseApiManager.getSavingAccountsListApi().getAccountTransferTemplate();
    }

    public Observable<ResponseBody> makeTransfer(TransferPayload transferPayload) {
        return baseApiManager.getSavingAccountsListApi().makeTransfer(transferPayload);
    }

    public Observable<SavingsAccountTemplate> getSavingAccountApplicationTemplate(long client) {
        return baseApiManager.getSavingAccountsListApi()
                .getSavingsAccountApplicationTemplate(client);
    }

    public Observable<ResponseBody> submitSavingAccountApplication(
            SavingsAccountApplicationPayload payload) {
        return baseApiManager.getSavingAccountsListApi().submitSavingAccountApplication(payload);
    }

    public Observable<ResponseBody> updateSavingsAccount(
            String accountId, SavingsAccountUpdatePayload payload) {
        return baseApiManager.getSavingAccountsListApi()
                .updateSavingsAccountUpdate(accountId, payload);
    }

    public Observable<ResponseBody> submitWithdrawSavingsAccount(
            String accountId, SavingsAccountWithdrawPayload payload) {
        return baseApiManager.getSavingAccountsListApi()
                .submitWithdrawSavingsAccount(accountId, payload)
                .onErrorResumeNext(new Function<Throwable, ObservableSource<?
                        extends ResponseBody>>() {
                    @Override
                    public ObservableSource<? extends
                            ResponseBody> apply(Throwable throwable) throws Exception {
                        return Observable.just(ResponseBody.create(MediaType.parse("text/parse"),
                                "Saving Account Withdrawn Successfully"));
                    }
                });
    }

    public Observable<LoanAccount> getLoanAccountDetails(long loanId) {
        return baseApiManager.getLoanAccountsListApi().getLoanAccountsDetail(loanId);
    }

    public Observable<LoanWithAssociations> getLoanWithAssociations(String associationType,
            long loanId) {
        return baseApiManager.getLoanAccountsListApi()
                .getLoanWithAssociations(loanId, associationType);
    }

    public Observable<LoanTemplate> getLoanTemplate() {
        return baseApiManager.getLoanAccountsListApi().getLoanTemplate(clientId);
    }

    public Observable<LoanTemplate> getLoanTemplateByProduct(Integer productId) {
        return baseApiManager.getLoanAccountsListApi()
                .getLoanTemplateByProduct(clientId, productId);
    }

    public Observable<ResponseBody> createLoansAccount(LoansPayload loansPayload) {
        return baseApiManager.getLoanAccountsListApi().createLoansAccount(loansPayload);
    }

    public Observable<ResponseBody> updateLoanAccount(long loanId, LoansPayload loansPayload) {
        return baseApiManager.getLoanAccountsListApi().updateLoanAccount(loanId, loansPayload);
    }

    public Observable<ResponseBody> withdrawLoanAccount(long loanId, LoanWithdraw loanWithdraw) {
        return baseApiManager.getLoanAccountsListApi().withdrawLoanAccount(loanId, loanWithdraw);
    }

    public Observable<List<Beneficiary>> getBeneficiaryList() {
        return baseApiManager.getBeneficiaryApi().getBeneficiaryList();
    }

    public Observable<BeneficiaryTemplate> getBeneficiaryTemplate() {
        return baseApiManager.getBeneficiaryApi().getBeneficiaryTemplate();
    }

    public Observable<ResponseBody> createBeneficiary(BeneficiaryPayload beneficiaryPayload) {
        return baseApiManager.getBeneficiaryApi().createBeneficiary(beneficiaryPayload);
    }

    public Observable<ResponseBody> updateBeneficiary(long beneficiaryId,
                                                      BeneficiaryUpdatePayload payload) {
        return baseApiManager.getBeneficiaryApi().updateBeneficiary(beneficiaryId, payload);
    }

    public Observable<ResponseBody> deleteBeneficiary(long beneficiaryId) {
        return baseApiManager.getBeneficiaryApi().deleteBeneficiary(beneficiaryId);
    }

    public Observable<AccountOptionsTemplate> getThirdPartyTransferTemplate() {
        return baseApiManager.getThirdPartyTransferApi().getAccountTransferTemplate();
    }

    public Observable<ResponseBody> makeThirdPartyTransfer(TransferPayload transferPayload) {
        return baseApiManager.getThirdPartyTransferApi().makeTransfer(transferPayload);
    }

    public Observable<ResponseBody> registerUser(RegisterPayload registerPayload) {
        return baseApiManager.getRegistrationApi().registerUser(registerPayload);
    }

    public Observable<ResponseBody> verifyUser(UserVerify userVerify) {
        return baseApiManager.getRegistrationApi().verifyUser(userVerify);
    }

    public PreferencesHelper getPreferencesHelper() {
        return preferencesHelper;
    }

    public Observable<Page<Charge>> getClientLocalCharges() {
        return databaseHelper.getClientCharges();
    }

    public Observable<List<MifosNotification>> getNotifications() {
        return databaseHelper.getNotifications();
    }

    public Observable<Integer> getUnreadNotificationsCount() {
        return databaseHelper.getUnreadNotificationsCount();
    }

    public Observable<ResponseBody> registerNotification(NotificationRegisterPayload payload) {
        return baseApiManager.getNotificationApi().registerNotification(payload);
    }

    public Observable<ResponseBody> updateRegisterNotification(long id, NotificationRegisterPayload
                                                                       payload) {
        return baseApiManager.getNotificationApi().updateRegisterNotification(id, payload);
    }

    public Observable<NotificationUserDetail> getUserNotificationId(long id) {
        return baseApiManager.getNotificationApi().getUserNotificationId(id);
    }

    public Observable<ResponseBody> updateAccountPassword(UpdatePasswordPayload payload) {
        return baseApiManager.getUserDetailsService().updateAccountPassword(payload);
    }

    public long getClientId() {
        return clientId;
    }

    public void setClientId(long clientId) {
        this.clientId = clientId;
    }

    public Observable<GuarantorTemplatePayload> getGuarantorTemplate(long loanId) {
        return baseApiManager.getGuarantorApi().getGuarantorTemplate(loanId)
                .onErrorResumeNext(new Function<Throwable,
                        ObservableSource<? extends GuarantorTemplatePayload>>() {
                    @Override
                    public ObservableSource<? extends
                            GuarantorTemplatePayload> apply(Throwable throwable) throws Exception {
                        return Observable.just(FakeRemoteDataSource.getGuarantorTemplatePayload());
                    }
                });
    }

    public Observable<List<GuarantorPayload>> getGuarantorList(long loanId) {
        return baseApiManager.getGuarantorApi().getGuarantorList(loanId)
                .onErrorResumeNext(new Function<Throwable,
                        ObservableSource<? extends List<GuarantorPayload>>>() {
                    @Override
                    public ObservableSource<?
                            extends List<GuarantorPayload>> apply(Throwable throwable) throws
                            Exception {
                        return Observable.just(FakeRemoteDataSource.getGuarantorsList());
                    }
                });
    }

    public Observable<ResponseBody> createGuarantor(long loanId,
                                                    GuarantorApplicationPayload payload) {
        return baseApiManager.getGuarantorApi().createGuarantor(loanId, payload)
                .onErrorResumeNext(new Function<Throwable, ObservableSource<?
                        extends ResponseBody>>() {
                    @Override
                    public ObservableSource<? extends ResponseBody> apply(Throwable throwable)
                            throws Exception {
                        ResponseBody responseBody = ResponseBody.create(MediaType
                                .parse("text/plain"), "Guarantor Added Successfully");
                        return Observable.just(responseBody);
                    }
                });
    }

    public Observable<ResponseBody> updateGuarantor(GuarantorApplicationPayload payload,
                                                    long loanId, long guarantorId) {
        return baseApiManager.getGuarantorApi().updateGuarantor(payload, loanId, guarantorId)
                .onErrorResumeNext(new Function<Throwable, ObservableSource<?
                        extends ResponseBody>>() {
                    @Override
                    public ObservableSource<? extends ResponseBody> apply(Throwable throwable)
                            throws Exception {
                        return Observable.just(ResponseBody.create(MediaType
                                .parse("plain/text"), "Guarantor Updated Successfully"));
                    }
                });
    }

    public Observable<ResponseBody> deleteGuarantor(long loanId, long guarantorId) {
        return baseApiManager.getGuarantorApi().deleteGuarantor(loanId, guarantorId)
                .onErrorResumeNext(new Function<Throwable, ObservableSource<
                        ? extends ResponseBody>>() {
                    @Override
                    public ObservableSource<? extends ResponseBody> apply(Throwable throwable)
                            throws Exception {
                        return Observable.just(ResponseBody.create(MediaType
                                .parse("plain/text"), "Guarantor Deleted Successfully"));
                    }
                });
    }

}
