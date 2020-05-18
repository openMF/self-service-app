package org.mifos.mobile.api.services;

import org.mifos.mobile.api.ApiEndPoints;
import org.mifos.mobile.models.Page;
import org.mifos.mobile.models.Transaction;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * @author Vishwajeet
 * @since 10/08/2016
 */
public interface RecentTransactionsService {
    @GET(ApiEndPoints.CLIENTS + "/{clientId}/transactions")
    Observable<Page<Transaction>> getRecentTransactionsList(
            @Path("clientId") long clientId,
            @Query("offset") int offset,
            @Query("limit") int limit);

    @GET(ApiEndPoints.LOANS + "/{clientId}/transaction/{transactionId}")
    Observable<Transaction> getClientTransaction(
            @Path("clientId") long clientId,
            @Path("transactionId") long transactionId);

}