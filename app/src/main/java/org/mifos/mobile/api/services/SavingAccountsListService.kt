package org.mifos.mobile.api.services

import io.reactivex.Observable

import okhttp3.ResponseBody

import org.mifos.mobile.api.ApiEndPoints
import org.mifos.mobile.models.accounts.savings.SavingsAccountApplicationPayload
import org.mifos.mobile.models.accounts.savings.SavingsAccountUpdatePayload
import org.mifos.mobile.models.accounts.savings.SavingsAccountWithdrawPayload
import org.mifos.mobile.models.accounts.savings.SavingsWithAssociations
import org.mifos.mobile.models.payload.TransferPayload
import org.mifos.mobile.models.templates.account.AccountOptionsTemplate
import org.mifos.mobile.models.templates.savings.SavingsAccountTemplate

import retrofit2.http.*

/**
 * @author Vishwajeet
 * @since 21/6/16.
 */
interface SavingAccountsListService {
    @GET(ApiEndPoints.SAVINGS_ACCOUNTS + "/{accountId}")
    fun getSavingsWithAssociations(
            @Path("accountId") accountId: Long?,
            @Query("associations") associationType: String?
    ): Observable<SavingsWithAssociations?>?

    @get:GET(ApiEndPoints.ACCOUNT_TRANSFER + "/template")
    val accountTransferTemplate: Observable<AccountOptionsTemplate?>?

    @POST(ApiEndPoints.ACCOUNT_TRANSFER)
    fun makeTransfer(@Body transferPayload: TransferPayload?): Observable<ResponseBody?>?

    @GET(ApiEndPoints.SAVINGS_ACCOUNTS + "/template")
    fun getSavingsAccountApplicationTemplate(
            @Query("clientId") clientId: Long?
    ): Observable<SavingsAccountTemplate?>?

    @POST(ApiEndPoints.SAVINGS_ACCOUNTS)
    fun submitSavingAccountApplication(
            @Body payload: SavingsAccountApplicationPayload?
    ): Observable<ResponseBody?>?

    @PUT(ApiEndPoints.SAVINGS_ACCOUNTS + "/{accountsId}")
    fun updateSavingsAccountUpdate(
            @Path("accountsId") accountsId: Long?, @Body payload: SavingsAccountUpdatePayload?
    ): Observable<ResponseBody?>?

    @POST(ApiEndPoints.SAVINGS_ACCOUNTS + "/{savingsId}?command=withdrawnByApplicant")
    fun submitWithdrawSavingsAccount(
            @Path("savingsId") savingsId: String?, @Body payload: SavingsAccountWithdrawPayload?
    ): Observable<ResponseBody?>?
}