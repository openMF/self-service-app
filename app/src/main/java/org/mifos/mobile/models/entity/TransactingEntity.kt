package org.mifos.mobile.models.entity

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class TransactingEntity(
        @SerializedName("partyIdInfo") val partyIdInfo: PartyIdInfo?): Parcelable