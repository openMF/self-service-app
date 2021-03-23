package org.mifos.mobile.api

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.functions.Function

import okhttp3.MediaType
import okhttp3.ResponseBody

import org.mifos.mobile.FakeRemoteDataSource
import org.mifos.mobile.api.local.DatabaseHelper
import org.mifos.mobile.api.local.PreferencesHelper
import org.mifos.mobile.models.*
import org.mifos.mobile.models.accounts.loan.LoanAccount
import org.mifos.mobile.models.accounts.loan.LoanWithAssociations
import org.mifos.mobile.models.accounts.loan.LoanWithdraw
import org.mifos.mobile.models.accounts.savings.SavingsAccountApplicationPayload
import org.mifos.mobile.models.accounts.savings.SavingsAccountUpdatePayload
import org.mifos.mobile.models.accounts.savings.SavingsAccountWithdrawPayload
import org.mifos.mobile.models.accounts.savings.SavingsWithAssociations
import org.mifos.mobile.models.beneficiary.Beneficiary
import org.mifos.mobile.models.beneficiary.BeneficiaryPayload
import org.mifos.mobile.models.beneficiary.BeneficiaryUpdatePayload
import org.mifos.mobile.models.client.Client
import org.mifos.mobile.models.client.ClientAccounts
import org.mifos.mobile.models.guarantor.GuarantorApplicationPayload
import org.mifos.mobile.models.guarantor.GuarantorPayload
import org.mifos.mobile.models.guarantor.GuarantorTemplatePayload
import org.mifos.mobile.models.notification.MifosNotification
import org.mifos.mobile.models.notification.NotificationRegisterPayload
import org.mifos.mobile.models.notification.NotificationUserDetail
import org.mifos.mobile.models.payload.LoansPayload
import org.mifos.mobile.models.payload.LoginPayload
import org.mifos.mobile.models.payload.TransferPayload
import org.mifos.mobile.models.register.RegisterPayload
import org.mifos.mobile.models.register.UserVerify
import org.mifos.mobile.models.templates.account.AccountOptionsTemplate
import org.mifos.mobile.models.templates.beneficiary.BeneficiaryTemplate
import org.mifos.mobile.models.templates.loans.LoanTemplate
import org.mifos.mobile.models.templates.savings.SavingsAccountTemplate

import javax.inject.Inject
import javax.inject.Singleton

/**
 * @author Vishwajeet
 * @since 13/6/16.
 */
@Singleton
class DataManager @Inject constructor(
        val preferencesHelper: PreferencesHelper, private val baseApiManager: BaseApiManager,
        private val databaseHelper: DatabaseHelper
) {
    var clientId: Long? = preferencesHelper.clientId
    fun login(loginPayload: LoginPayload?): Observable<User?>? {
        return baseApiManager.authenticationApi?.authenticate(loginPayload)
    }

    val clients: Observable<Page<Client?>?>?
        get() = baseApiManager.clientsApi?.clients
    val currentClient: Observable<Client?>?
        get() = baseApiManager.clientsApi?.getClientForId(clientId)
    val clientImage: Observable<ResponseBody?>?
        get() = baseApiManager.clientsApi?.getClientImage(clientId)
    val clientAccounts: Observable<ClientAccounts?>?
        get() = baseApiManager.clientsApi?.getClientAccounts(clientId)

    fun getAccounts(accountType: String?): Observable<ClientAccounts?>? {
        return baseApiManager.clientsApi?.getAccounts(clientId, accountType)
    }

    fun getRecentTransactions(offset: Int, limit: Int): Observable<Page<Transaction?>?>? {
        return baseApiManager.recentTransactionsApi
                ?.getRecentTransactionsList(clientId, offset, limit)
    }

    fun getClientCharges(clientId: Long): Observable<Page<Charge?>?>? {
        return baseApiManager.clientChargeApi?.getClientChargeList(clientId)
                ?.concatMap { chargePage -> databaseHelper.syncCharges(chargePage) }
    }

    fun getLoanCharges(loanId: Long): Observable<List<Charge?>?>? {
        return baseApiManager.clientChargeApi?.getLoanAccountChargeList(loanId)
    }

    fun getSavingsCharges(savingsId: Long): Observable<List<Charge?>?>? {
        return baseApiManager.clientChargeApi?.getSavingsAccountChargeList(savingsId)
    }

    fun getSavingsWithAssociations(
            accountId: Long?,
            associationType: String?
    ): Observable<SavingsWithAssociations?>? {
        return baseApiManager
                .savingAccountsListApi?.getSavingsWithAssociations(accountId, associationType)
    }

    val accountTransferTemplate: Observable<AccountOptionsTemplate?>?
        get() = baseApiManager.savingAccountsListApi?.accountTransferTemplate

    fun makeTransfer(transferPayload: TransferPayload?): Observable<ResponseBody?>? {
        return baseApiManager.savingAccountsListApi?.makeTransfer(transferPayload)
    }

    fun getSavingAccountApplicationTemplate(client: Long?): Observable<SavingsAccountTemplate?>? {
        return baseApiManager.savingAccountsListApi
                ?.getSavingsAccountApplicationTemplate(client)
    }

    fun submitSavingAccountApplication(
            payload: SavingsAccountApplicationPayload?
    ): Observable<ResponseBody?>? {
        return baseApiManager.savingAccountsListApi?.submitSavingAccountApplication(payload)
    }

    fun updateSavingsAccount(
            accountId: Long?, payload: SavingsAccountUpdatePayload?
    ): Observable<ResponseBody?>? {
        return baseApiManager.savingAccountsListApi
                ?.updateSavingsAccountUpdate(accountId, payload)
    }

    fun submitWithdrawSavingsAccount(
            accountId: String?, payload: SavingsAccountWithdrawPayload?
    ): Observable<ResponseBody?>? {
        return baseApiManager.savingAccountsListApi
                ?.submitWithdrawSavingsAccount(accountId, payload)
                ?.onErrorResumeNext(Function<Throwable?, ObservableSource<out ResponseBody>> {
                    Observable.just(ResponseBody.create(MediaType.parse("text/parse"),
                            "Saving Account Withdrawn Successfully"))
                })
    }

    fun getLoanAccountDetails(loanId: Long): Observable<LoanAccount?>? {
        return baseApiManager.loanAccountsListApi?.getLoanAccountsDetail(loanId)
    }

    fun getLoanWithAssociations(
            associationType: String?,
            loanId: Long?
    ): Observable<LoanWithAssociations?>? {
        return baseApiManager.loanAccountsListApi
                ?.getLoanWithAssociations(loanId, associationType)
    }

    val loanTemplate: Observable<LoanTemplate?>?
        get() = baseApiManager.loanAccountsListApi?.getLoanTemplate(clientId)

    fun getLoanTemplateByProduct(productId: Int?): Observable<LoanTemplate?>? {
        return baseApiManager.loanAccountsListApi
                ?.getLoanTemplateByProduct(clientId, productId)
    }

    fun createLoansAccount(loansPayload: LoansPayload?): Observable<ResponseBody?>? {
        return baseApiManager.loanAccountsListApi?.createLoansAccount(loansPayload)
    }

    fun updateLoanAccount(loanId: Long, loansPayload: LoansPayload?): Observable<ResponseBody?>? {
        return baseApiManager.loanAccountsListApi?.updateLoanAccount(loanId, loansPayload)
    }

    fun withdrawLoanAccount(loanId: Long?, loanWithdraw: LoanWithdraw?): Observable<ResponseBody?>? {
        return baseApiManager.loanAccountsListApi?.withdrawLoanAccount(loanId, loanWithdraw)
    }

    val beneficiaryList: Observable<List<Beneficiary?>?>?
        get() = baseApiManager.beneficiaryApi?.beneficiaryList
    val beneficiaryTemplate: Observable<BeneficiaryTemplate?>?
        get() = baseApiManager.beneficiaryApi?.beneficiaryTemplate

    fun createBeneficiary(beneficiaryPayload: BeneficiaryPayload?): Observable<ResponseBody?>? {
        return baseApiManager.beneficiaryApi?.createBeneficiary(beneficiaryPayload)
    }

    fun updateBeneficiary(
            beneficiaryId: Long?,
            payload: BeneficiaryUpdatePayload?
    ): Observable<ResponseBody?>? {
        return baseApiManager.beneficiaryApi?.updateBeneficiary(beneficiaryId, payload)
    }

    fun deleteBeneficiary(beneficiaryId: Long?): Observable<ResponseBody?>? {
        return baseApiManager.beneficiaryApi?.deleteBeneficiary(beneficiaryId)
    }

    val thirdPartyTransferTemplate: Observable<AccountOptionsTemplate?>?
        get() = baseApiManager.thirdPartyTransferApi?.accountTransferTemplate

    fun makeThirdPartyTransfer(transferPayload: TransferPayload?): Observable<ResponseBody?>? {
        return baseApiManager.thirdPartyTransferApi?.makeTransfer(transferPayload)
    }

    fun registerUser(registerPayload: RegisterPayload?): Observable<ResponseBody?>? {
        return baseApiManager.registrationApi?.registerUser(registerPayload)
    }

    fun verifyUser(userVerify: UserVerify?): Observable<ResponseBody?>? {
        return baseApiManager.registrationApi?.verifyUser(userVerify)
    }

    val clientLocalCharges: Observable<Page<Charge?>?>?
        get() = databaseHelper.clientCharges
    val notifications: Observable<List<MifosNotification?>?>?
        get() = databaseHelper.notifications
    val unreadNotificationsCount: Observable<Int>
        get() = databaseHelper.unreadNotificationsCount

    fun registerNotification(payload: NotificationRegisterPayload?): Observable<ResponseBody?>? {
        return baseApiManager.notificationApi?.registerNotification(payload)
    }

    fun updateRegisterNotification(id: Long, payload: NotificationRegisterPayload?): Observable<ResponseBody?>? {
        return baseApiManager.notificationApi?.updateRegisterNotification(id, payload)
    }

    fun getUserNotificationId(id: Long): Observable<NotificationUserDetail?>? {
        return baseApiManager.notificationApi?.getUserNotificationId(id)
    }

    fun updateAccountPassword(payload: UpdatePasswordPayload?): Observable<ResponseBody?>? {
        return baseApiManager.userDetailsService?.updateAccountPassword(payload)
    }

    fun getGuarantorTemplate(loanId: Long?): Observable<GuarantorTemplatePayload?>? {
        return baseApiManager.guarantorApi?.getGuarantorTemplate(loanId)
                ?.onErrorResumeNext(Function<Throwable?, ObservableSource<out GuarantorTemplatePayload>> { Observable.just(FakeRemoteDataSource.guarantorTemplatePayload) })
    }

    fun getGuarantorList(loanId: Long): Observable<List<GuarantorPayload?>?>? {
        return baseApiManager.guarantorApi?.getGuarantorList(loanId)
                ?.onErrorResumeNext(Function<Throwable?, ObservableSource<out List<GuarantorPayload>>> { Observable.just(FakeRemoteDataSource.guarantorsList) })
    }

    fun createGuarantor(
            loanId: Long?,
            payload: GuarantorApplicationPayload?
    ): Observable<ResponseBody?>? {
        return baseApiManager.guarantorApi?.createGuarantor(loanId, payload)
                ?.onErrorResumeNext(Function<Throwable?, ObservableSource<out ResponseBody>> {
                    val responseBody = ResponseBody.create(MediaType
                            .parse("text/plain"), "Guarantor Added Successfully")
                    Observable.just(responseBody)
                })
    }

    fun updateGuarantor(
            payload: GuarantorApplicationPayload?,
            loanId: Long?, guarantorId: Long?
    ): Observable<ResponseBody?>? {
        return baseApiManager.guarantorApi?.updateGuarantor(payload, loanId, guarantorId)
                ?.onErrorResumeNext(Function<Throwable?, ObservableSource<out ResponseBody>> {
                    Observable.just(ResponseBody.create(MediaType
                            .parse("plain/text"), "Guarantor Updated Successfully"))
                })
    }

    fun deleteGuarantor(loanId: Long?, guarantorId: Long?): Observable<ResponseBody?>? {
        return baseApiManager.guarantorApi?.deleteGuarantor(loanId, guarantorId)
                ?.onErrorResumeNext(Function<Throwable?, ObservableSource<out ResponseBody>> {
                    Observable.just(ResponseBody.create(MediaType
                            .parse("plain/text"), "Guarantor Deleted Successfully"))
                })
    }

}