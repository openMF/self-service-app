package org.mifos.mobile.models.accounts.loan

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import org.mifos.mobile.models.accounts.Account

/**
 * @author Vishwajeet
 * @since 22/06/16.
 */
data class LoanAccount(
        @SerializedName("loanProductId")
        var loanProductId: Long = 0,

        @SerializedName("externalId")
        var externalId: String? = null,

        @SerializedName("numberOfRepayments")
        var numberOfRepayments: Long = 0,

        @SerializedName("accountNo")
        var accountNo: String? = null,

        @SerializedName("productName")
        var productName: String? = null,

        @SerializedName("productId")
        var productId: Int? = null,

        @SerializedName("loanProductName")
        var loanProductName: String? = null,

        @SerializedName("clientName")
        var clientName: String? = null,

        @SerializedName("loanProductDescription")
        var loanProductDescription: String? = null,

        @SerializedName("principal")
        var principal: Double = 0.toDouble(),

        @SerializedName("annualInterestRate")
        var annualInterestRate: Double = 0.toDouble(),

        @SerializedName("status")
        var status: Status? = null,

        @SerializedName("loanType")
        var loanType: LoanType? = null,

        @SerializedName("loanCycle")
        var loanCycle: Int? = null,

        @SerializedName("loanBalance")
        var loanBalance: Double = 0.toDouble(),

        @SerializedName("amountPaid")
        var amountPaid: Double = 0.toDouble(),

        @SerializedName("currency")
        var currency: Currency? = null,

        @SerializedName("inArrears")
        var inArrears: Boolean? = null,

        @SerializedName("summary")
        var summary: Summary? = null,

        @SerializedName("loanPurposeName")
        var loanPurposeName: String? = null,

        @SerializedName("timeline")
        var timeline: Timeline

) : Account(), Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readLong(),
            parcel.readString(),
            parcel.readLong(),
            parcel.readString(),
            parcel.readString(),
            parcel.readValue(Int::class.java.classLoader) as? Int,
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readDouble(),
            parcel.readDouble(),
            parcel.readParcelable(Status::class.java.classLoader),
            parcel.readParcelable(LoanType::class.java.classLoader),
            parcel.readValue(Int::class.java.classLoader) as? Int,
            parcel.readDouble(),
            parcel.readDouble(),
            parcel.readParcelable(Currency::class.java.classLoader),
            parcel.readValue(Boolean::class.java.classLoader) as? Boolean,
            parcel.readParcelable(Summary::class.java.classLoader),
            parcel.readString(),
            parcel.readParcelable(Timeline::class.java.classLoader)) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(loanProductId)
        parcel.writeString(externalId)
        parcel.writeLong(numberOfRepayments)
        parcel.writeString(accountNo)
        parcel.writeString(productName)
        parcel.writeValue(productId)
        parcel.writeString(loanProductName)
        parcel.writeString(clientName)
        parcel.writeString(loanProductDescription)
        parcel.writeDouble(principal)
        parcel.writeDouble(annualInterestRate)
        parcel.writeParcelable(status, flags)
        parcel.writeParcelable(loanType, flags)
        parcel.writeValue(loanCycle)
        parcel.writeDouble(loanBalance)
        parcel.writeDouble(amountPaid)
        parcel.writeParcelable(currency, flags)
        parcel.writeValue(inArrears)
        parcel.writeParcelable(summary, flags)
        parcel.writeString(loanPurposeName)
        parcel.writeParcelable(timeline, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object {

        @JvmField
        final val CREATOR: Parcelable.Creator<LoanAccount> = object : Parcelable.Creator<LoanAccount> {

            override fun createFromParcel(parcel: Parcel): LoanAccount {
                return LoanAccount(parcel)
            }

            override fun newArray(size: Int): Array<LoanAccount?> {
                return arrayOfNulls(size)
            }
        }
    }
}