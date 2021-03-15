package org.mifos.mobile.models.accounts.savings

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.mifos.mobile.models.client.DepositType
import org.mifos.mobile.utils.DateHelper
import java.util.*

/**
 * @author Vishwajeet
 * @since 22/06/16
 */

@Parcelize
data class SavingsWithAssociations(

        var id: Long? = null,

        var accountNo: String? = null,

        var depositType: DepositType? = null,

        var externalId: String? = null,

        var clientId: Int? = null,

        var clientName: String? = null,

        var savingsProductId: Int? = null,

        var savingsProductName: String? = null,

        var fieldOfficerId: Int? = null,

        var status: Status? = null,

        var timeline: TimeLine? = null,

        var currency: Currency? = null,

        internal var nominalAnnualInterestRate: Double? = null,

        var minRequiredOpeningBalance: Double? = null,

        var lockinPeriodFrequency: Double? = null,

        var withdrawalFeeForTransfers: Boolean? = null,

        var allowOverdraft: Boolean? = null,

        var enforceMinRequiredBalance: Boolean? = null,

        var withHoldTax: Boolean? = null,

        var lastActiveTransactionDate: List<Int>? = null,

        var dormancyTrackingActive: Boolean? = null,

        var summary: Summary? = null,

        var transactions: List<Transactions> = ArrayList()

) : Parcelable {

    fun isRecurring(): Boolean {
        return this.depositType != null && (this.depositType?.isRecurring() == true)
    }

    fun setNominalAnnualInterestRate(nominalAnnualInterestRate: Double?) {
        this.nominalAnnualInterestRate = nominalAnnualInterestRate
    }

    fun getNominalAnnualInterestRate(): Double {
        return nominalAnnualInterestRate!!
    }

    fun setNominalAnnualInterestRate(nominalAnnualInterestRate: Double) {
        this.nominalAnnualInterestRate = nominalAnnualInterestRate
    }

    fun getLastTransaction() : Transactions? {
        lastActiveTransactionDate?.let {
            for (transaction in transactions) {
                if (DateHelper.equals(transaction.date, it)) {
                    return transaction
                }
            }
        }
        return null
    }

}