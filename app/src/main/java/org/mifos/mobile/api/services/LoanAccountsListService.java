package org.mifos.mobile.api.services;

import org.mifos.mobile.api.ApiEndPoints;
import org.mifos.mobile.models.Transaction;
import org.mifos.mobile.models.accounts.loan.LoanAccount;
import org.mifos.mobile.models.accounts.loan.LoanWithAssociations;
import org.mifos.mobile.models.accounts.loan.LoanWithdraw;
import org.mifos.mobile.models.payload.LoansPayload;
import org.mifos.mobile.models.templates.loans.LoanTemplate;

import io.reactivex.Observable;
import okhttp3.ResponseBody;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

import static org.mifos.mobile.utils.Constants.LOAN_ID;

/**
 * @author Vishwajeet
 * @since 23/6/16.
 */
public interface LoanAccountsListService {

    @GET(ApiEndPoints.LOANS + "/{loanId}/")
    Observable<LoanAccount> getLoanAccountsDetail(@Path(LOAN_ID) long loanId);

    @GET(ApiEndPoints.LOANS + "/{loanId}")
    Observable<LoanWithAssociations> getLoanWithAssociations(
            @Path(LOAN_ID) long loanId,
            @Query("associations") String associationType);

    @GET(ApiEndPoints.LOANS + "/template?templateType=individual")
    Observable<LoanTemplate> getLoanTemplate(@Query("clientId") long clientId);

    @GET(ApiEndPoints.LOANS + "/template?templateType=individual")
    Observable<LoanTemplate> getLoanTemplateByProduct(
            @Query("clientId") long clientId,
            @Query("productId") Integer productId);

    @POST(ApiEndPoints.LOANS)
    Observable<ResponseBody> createLoansAccount(@Body LoansPayload loansPayload);

    @PUT(ApiEndPoints.LOANS + "/{loanId}/")
    Observable<ResponseBody> updateLoanAccount(@Path(LOAN_ID) long loanId,
                                              @Body LoansPayload loansPayload);

    @POST(ApiEndPoints.LOANS + "/{loanId}?command=withdrawnByApplicant")
    Observable<ResponseBody> withdrawLoanAccount(@Path(LOAN_ID) long loanId,
                                                 @Body LoanWithdraw loanWithdraw);

    @GET(ApiEndPoints.LOANS + "/{loanId}/transactions/{transactionId}")
    Observable<Transaction> getLoanAccountTransaction(
            @Path(LOAN_ID) long loanId,
            @Path("transactionId") long transactionId);
}
