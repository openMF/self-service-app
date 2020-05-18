package org.mifos.mobile.api.services;

import org.mifos.mobile.api.ApiEndPoints;
import org.mifos.mobile.models.accounts.savings.SavingsAccountApplicationPayload;
import org.mifos.mobile.models.accounts.savings.SavingsAccountUpdatePayload;
import org.mifos.mobile.models.accounts.savings.SavingsAccountWithdrawPayload;
import org.mifos.mobile.models.accounts.savings.SavingsWithAssociations;
import org.mifos.mobile.models.accounts.savings.Transactions;
import org.mifos.mobile.models.payload.TransferPayload;
import org.mifos.mobile.models.templates.account.AccountOptionsTemplate;
import org.mifos.mobile.models.templates.savings.SavingsAccountTemplate;

import io.reactivex.Observable;
import okhttp3.ResponseBody;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * @author Vishwajeet
 * @since 21/6/16.
 */
public interface SavingAccountsListService {

    @GET(ApiEndPoints.SAVINGS_ACCOUNTS + "/{accountId}")
    Observable<SavingsWithAssociations> getSavingsWithAssociations(
            @Path("accountId") long accountId,
            @Query("associations") String associationType);

    @GET(ApiEndPoints.ACCOUNT_TRANSFER + "/template")
    Observable<AccountOptionsTemplate> getAccountTransferTemplate();

    @POST(ApiEndPoints.ACCOUNT_TRANSFER)
    Observable<ResponseBody> makeTransfer(@Body TransferPayload transferPayload);

    @GET(ApiEndPoints.SAVINGS_ACCOUNTS + "/template")
    Observable<SavingsAccountTemplate> getSavingsAccountApplicationTemplate(
            @Query("clientId") long clientId);

    @POST(ApiEndPoints.SAVINGS_ACCOUNTS)
    Observable<ResponseBody> submitSavingAccountApplication(
            @Body SavingsAccountApplicationPayload payload);

    @PUT(ApiEndPoints.SAVINGS_ACCOUNTS + "/{accountsId}")
    Observable<ResponseBody> updateSavingsAccountUpdate(
            @Path("accountsId") String accountsId, @Body SavingsAccountUpdatePayload payload);

    @POST(ApiEndPoints.SAVINGS_ACCOUNTS + "/{savingsId}?command=withdrawnByApplicant")
    Observable<ResponseBody> submitWithdrawSavingsAccount(
            @Path("savingsId") String savingsId, @Body SavingsAccountWithdrawPayload payload);

    @GET(ApiEndPoints.SAVINGS_ACCOUNTS + "/{accountId}/transactions/{transactionId}")
    Observable<Transactions> getSavingsAccountTransaction(
            @Path("accountId") long accountId,
            @Path("transactionId") long transactionId);
}
