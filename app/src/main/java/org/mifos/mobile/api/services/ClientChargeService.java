package org.mifos.mobile.api.services;

import org.mifos.mobile.api.ApiEndPoints;
import org.mifos.mobile.models.Charge;
import org.mifos.mobile.models.Page;

import java.util.List;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * @author Vishwajeet
 * @since 17/8/16.
 */
public interface ClientChargeService {
    @GET(ApiEndPoints.CLIENTS + "/{clientId}/charges")
    Observable<Page<Charge>> getClientChargeList(@Path("clientId") long clientId);

    @GET(ApiEndPoints.CLIENTS + "/{clientId}/charges/{clientChargeId}")
    Observable<Charge> getClientChargeDetails(
            @Path("clientId") long clientId,
            @Path("clientChargeId") int clientChargeId);

    @GET(ApiEndPoints.LOANS + "/{loanId}/charges")
    Observable<List<Charge>> getLoanAccountChargeList(@Path("loanId") long loanId);

    @GET(ApiEndPoints.LOANS + "/{loanId}/charges/{chargeId}")
    Observable<Charge> getLoanChargeDetails(
            @Path("loanId") long loanId,
            @Path("chargeId") int chargeId);


    @GET(ApiEndPoints.SAVINGS_ACCOUNTS + "/{savingsId}/charges")
    Observable<List<Charge>> getSavingsAccountChargeList(@Path("savingsId") long savingsId);

    @GET(ApiEndPoints.SAVINGS_ACCOUNTS + "/{accountId}/charges/{savingsAccountChargeId}")
    Observable<Charge> getSavingsChargeDetails(
            @Path("accountId") long accountId,
            @Path("savingsAccountChargeId") int savingsAccountChargeId);
}
