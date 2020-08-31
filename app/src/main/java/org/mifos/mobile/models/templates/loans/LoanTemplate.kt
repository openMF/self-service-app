package org.mifos.mobile.models.templates.loans

import android.os.Parcelable

import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

import org.mifos.mobile.models.accounts.loan.AmortizationType
import org.mifos.mobile.models.accounts.loan.DaysInMonthType
import org.mifos.mobile.models.accounts.loan.DaysInYearType
import org.mifos.mobile.models.accounts.loan.InterestCalculationPeriodType
import org.mifos.mobile.models.accounts.loan.InterestRateFrequencyType
import org.mifos.mobile.models.accounts.loan.InterestType
import org.mifos.mobile.models.accounts.loan.RepaymentFrequencyType
import org.mifos.mobile.models.accounts.loan.TermPeriodFrequencyType

import java.util.ArrayList

/**
 * Created by Rajan Maurya on 15/07/16.
 */

@Parcelize
data class LoanTemplate(
        @SerializedName("clientId")
        var clientId: Int? = null,

        @SerializedName("clientAccountNo")
        var clientAccountNo: String? = null,

        @SerializedName("clientName")
        var clientName: String? = null,

        @SerializedName("clientOfficeId")
        var clientOfficeId: Int? = null,

        @SerializedName("loanProductId")
        var loanProductId: Int? = null,

        @SerializedName("loanProductName")
        var loanProductName: String? = null,

        @SerializedName("isLoanProductLinkedToFloatingRate")
        var loanProductLinkedToFloatingRate: Boolean? = null,

        @SerializedName("fundId")
        var fundId: Int? = null,

        @SerializedName("fundName")
        var fundName: String? = null,

        @SerializedName("currency")
        var currency: Currency? = null,

        @SerializedName("principal")
        var principal: Double? = null,

        @SerializedName("approvedPrincipal")
        var approvedPrincipal: Double? = null,

        @SerializedName("proposedPrincipal")
        var proposedPrincipal: Double? = null,

        @SerializedName("termFrequency")
        var termFrequency: Int? = null,

        @SerializedName("termPeriodFrequencyType")
        var termPeriodFrequencyType: TermPeriodFrequencyType? = null,

        @SerializedName("numberOfRepayments")
        var numberOfRepayments: Int? = null,

        @SerializedName("repaymentEvery")
        var repaymentEvery: Int? = null,

        @SerializedName("repaymentFrequencyType")
        var repaymentFrequencyType: RepaymentFrequencyType? = null,

        @SerializedName("interestRatePerPeriod")
        var interestRatePerPeriod: Double? = null,

        @SerializedName("interestRateFrequencyType")
        var interestRateFrequencyType: InterestRateFrequencyType? = null,

        @SerializedName("annualInterestRate")
        var annualInterestRate: Double? = null,

        @SerializedName("isFloatingInterestRate")
        var floatingInterestRate: Boolean? = null,

        @SerializedName("amortizationType")
        var amortizationType: AmortizationType? = null,

        @SerializedName("interestType")
        var interestType: InterestType? = null,

        @SerializedName("interestCalculationPeriodType")
        var interestCalculationPeriodType: InterestCalculationPeriodType? = null,

        @SerializedName("allowPartialPeriodInterestCalcualtion")
        var allowPartialPeriodInterestCalcualtion: Boolean? = null,

        @SerializedName("transactionProcessingStrategyId")
        var transactionProcessingStrategyId: Int? = null,

        @SerializedName("graceOnArrearsAgeing")
        var graceOnArrearsAgeing: Int? = null,

        @SerializedName("timeline")
        var timeline: Timeline? = null,

        @SerializedName("productOptions")
        var productOptions: List<ProductOptions> = ArrayList(),

        @SerializedName("loanOfficerOptions")
        var loanOfficerOptions: List<LoanOfficerOptions> = ArrayList(),

        @SerializedName("loanPurposeOptions")
        var loanPurposeOptions: List<LoanPurposeOptions> = ArrayList(),

        @SerializedName("fundOptions")
        var fundOptions: List<FundOptions> = ArrayList(),

        @SerializedName("termFrequencyTypeOptions")
        var termFrequencyTypeOptions: List<TermFrequencyTypeOptions> = ArrayList(),

        @SerializedName("repaymentFrequencyTypeOptions")
        var repaymentFrequencyTypeOptions: List<RepaymentFrequencyTypeOptions> = ArrayList(),

        @SerializedName("repaymentFrequencyNthDayTypeOptions")
        var repaymentFrequencyNthDayTypeOptions: List<RepaymentFrequencyNthDayTypeOptions> = ArrayList(),

        @SerializedName("repaymentFrequencyDaysOfWeekTypeOptions")
        var repaymentFrequencyDaysOfWeekTypeOptions: List<RepaymentFrequencyDaysOfWeekTypeOptions> = ArrayList(),

        @SerializedName("interestRateFrequencyTypeOptions")
        var interestRateFrequencyTypeOptions: List<InterestRateFrequencyTypeOptions> = ArrayList(),

        @SerializedName("amortizationTypeOptions")
        var amortizationTypeOptions: List<AmortizationTypeOptions> = ArrayList(),

        @SerializedName("interestTypeOptions")
        var interestTypeOptions: List<InterestTypeOptions> = ArrayList(),

        @SerializedName("interestCalculationPeriodTypeOptions")
        var interestCalculationPeriodTypeOptions: List<InterestCalculationPeriodType> = ArrayList(),

        @SerializedName("transactionProcessingStrategyOptions")
        var transactionProcessingStrategyOptions: List<TransactionProcessingStrategyOptions> = ArrayList(),

        @SerializedName("chargeOptions")
        var chargeOptions: List<ChargeOptions> = ArrayList(),

        @SerializedName("loanCollateralOptions")
        var loanCollateralOptions: List<LoanCollateralOptions> = ArrayList(),

        @SerializedName("multiDisburseLoan")
        var multiDisburseLoan: Boolean? = null,

        @SerializedName("canDefineInstallmentAmount")
        var canDefineInstallmentAmount: Boolean? = null,

        @SerializedName("canDisburse")
        var canDisburse: Boolean? = null,

        @SerializedName("product")
        var product: Product? = null,

        @SerializedName("daysInMonthType")
        var daysInMonthType: DaysInMonthType? = null,

        @SerializedName("daysInYearType")
        var daysInYearType: DaysInYearType? = null,

        @SerializedName("isInterestRecalculationEnabled")
        var interestRecalculationEnabled: Boolean? = null,

        @SerializedName("isVariableInstallmentsAllowed")
        var variableInstallmentsAllowed: Boolean? = null,

        @SerializedName("minimumGap")
        var minimumGap: Int? = null,

        @SerializedName("maximumGap")
        var maximumGap: Int? = null,

        @SerializedName("accountLinkingOptions")
        var accountLinkingOptions: List<AccountLinkingOptions> = ArrayList()

) : Parcelable {
    /**
     * Required to set default value to the Fund spinner
     *
     * @param fundId The value received from the Template for that particular loanProduct
     * @return Returns the index of the fundOption list where the specified fundId is located
     */
    fun getFundNameFromId(fundId: Int): Int {
        for (i in fundOptions.indices) {
            if (fundOptions[i].id == fundId) {
                return i
            }
        }
        return 0
    }
}
