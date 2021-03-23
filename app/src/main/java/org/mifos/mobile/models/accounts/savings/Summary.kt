package org.mifos.mobile.models.accounts.savings

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize


/**
 * Created by Rajan Maurya on 05/03/17.
 */

@Parcelize
data class Summary(

        var currency: Currency? = null,

        var totalDeposits: Double? = null,

        var totalWithdrawals: Double? = null,

        var totalInterestEarned: Double? = null,

        var totalInterestPosted: Double? = null,

        var accountBalance: Double? = null,

        var totalOverdraftInterestDerived: Double? = null,

        var interestNotPosted: Double? = null,

        var lastInterestCalculationDate: List<Int> = ArrayList()

) : Parcelable