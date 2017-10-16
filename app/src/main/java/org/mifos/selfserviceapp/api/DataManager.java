package org.mifos.selfserviceapp.api;

import org.mifos.selfserviceapp.api.local.DatabaseHelper;
import org.mifos.selfserviceapp.api.local.PreferencesHelper;
import org.mifos.selfserviceapp.models.Charge;
import org.mifos.selfserviceapp.models.Page;
import org.mifos.selfserviceapp.models.Transaction;
import org.mifos.selfserviceapp.models.User;
import org.mifos.selfserviceapp.models.accounts.loan.LoanAccount;
import org.mifos.selfserviceapp.models.accounts.loan.LoanWithAssociations;
import org.mifos.selfserviceapp.models.accounts.loan.LoanWithdraw;
import org.mifos.selfserviceapp.models.accounts.savings.SavingsWithAssociations;
import org.mifos.selfserviceapp.models.beneficary.Beneficiary;
import org.mifos.selfserviceapp.models.beneficary.BeneficiaryPayload;
import org.mifos.selfserviceapp.models.beneficary.BeneficiaryUpdatePayload;
import org.mifos.selfserviceapp.models.client.Client;
import org.mifos.selfserviceapp.models.client.ClientAccounts;
import org.mifos.selfserviceapp.models.payload.LoansPayload;
import org.mifos.selfserviceapp.models.payload.TransferPayload;
import org.mifos.selfserviceapp.models.register.RegisterPayload;
import org.mifos.selfserviceapp.models.register.UserVerify;
import org.mifos.selfserviceapp.models.templates.account.AccountOptionsTemplate;
import org.mifos.selfserviceapp.models.templates.beneficiary.BeneficiaryTemplate;
import org.mifos.selfserviceapp.models.templates.loans.LoanTemplate;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import okhttp3.ResponseBody;
import rx.Observable;
import rx.functions.Func1;

/**
 * @author Vishwajeet
 * @since 13/6/16.
 */
@Singleton
public class DataManager {

    private final PreferencesHelper preferencesHelper;
    private final BaseApiManager baseApiManager;
    private final DatabaseHelper databaseHelper;
    protected long clientId;

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
                .concatMap(new Func1<Page<Charge>, Observable<? extends Page<Charge>>>() {
                    @Override
                    public Observable<? extends Page<Charge>> call(Page<Charge> chargePage) {
                        return databaseHelper.syncCharges(chargePage);
                    }
                });
    }

    public Observable<List<Charge>> getLoanCharges(long loanId) {
        return baseApiManager.getClientChargeApi().getLoanAccountChargeList(loanId);
    }

    public Observable<List<Charge>> getSavingsCharges(long savingsId) {
        return baseApiManager.getClientChargeApi().getSavingsAccountChargeList(savingsId);
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
    public long getClientId() {
        return clientId;
    }

    public void setClientId(long clientId) {
        this.clientId = clientId;
    }

}
