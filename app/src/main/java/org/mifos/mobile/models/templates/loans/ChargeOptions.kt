package org.mifos.mobile.models.templates.loans

import android.os.Parcelable

import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

import org.mifos.mobile.models.ChargeCalculationType
import org.mifos.mobile.models.ChargeTimeType

/**
 * Created by Rajan Maurya on 16/07/16.
 */

@Parcelize
data class ChargeOptions(
        @SerializedName("id")
        var id: Int? = null,

        @SerializedName("name")
        var name: String? = null,

        @SerializedName("active")
        var active: Boolean? = null,

        @SerializedName("penalty")
        var penalty: Boolean? = null,

        @SerializedName("currency")
        var currency: Currency? = null,

        @SerializedName("amount")
        var amount: Double? = null,

        @SerializedName("chargeTimeType")
        var chargeTimeType: ChargeTimeType? = null,

        @SerializedName("chargeAppliesTo")
        var chargeAppliesTo: ChargeAppliesTo? = null,

        @SerializedName("chargeCalculationType")
        var chargeCalculationType: ChargeCalculationType? = null,

        @SerializedName("chargePaymentMode")
        var chargePaymentMode: ChargePaymentMode? = null,

        @SerializedName("taxGroup")
        var taxGroup: TaxGroup? = null

) : Parcelable