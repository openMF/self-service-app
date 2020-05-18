/*
 * This project is licensed under the open source MPL V2.
 * See https://github.com/openMF/android-client/blob/master/LICENSE.md
 */

package org.mifos.mobile.models.accounts.loan

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

data class Timeline(
        @SerializedName("submittedOnDate")
        var submittedOnDate: List<Int>,

        @SerializedName("submittedByUsername")
        var submittedByUsername: String,

        @SerializedName("submittedByFirstname")
        var submittedByFirstname: String,

        @SerializedName("submittedByLastname")
        var submittedByLastname: String,

        @SerializedName("approvedOnDate")
        var approvedOnDate: List<Int>,

        @SerializedName("approvedByUsername")
        var approvedByUsername: String? = null,

        @SerializedName("approvedByFirstname")
        var approvedByFirstname: String? = null,

        @SerializedName("approvedByLastname")
        var approvedByLastname: String? = null,

        @SerializedName("expectedDisbursementDate")
        var expectedDisbursementDate: List<Int>,

        @SerializedName("actualDisbursementDate")
        var actualDisbursementDate: List<Int>,

        @SerializedName("disbursedByUsername")
        var disbursedByUsername: String? = null,

        @SerializedName("disbursedByFirstname")
        var disbursedByFirstname: String? = null,

        @SerializedName("disbursedByLastname")
        var disbursedByLastname: String? = null,

        @SerializedName("closedOnDate")
        var closedOnDate: List<Int>,

        @SerializedName("expectedMaturityDate")
        var expectedMaturityDate: List<Int>,

        @SerializedName("withdrawnOnDate")
        var withdrawnOnDate: List<Int>

) : Parcelable {
    constructor(parcel: Parcel) : this(
            arrayListOf<Int>().apply {
                parcel.readArrayList(Int::class.java.classLoader)
            },
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            arrayListOf<Int>().apply {
                parcel.readArrayList(Int::class.java.classLoader)
            },
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            arrayListOf<Int>().apply {
                parcel.readArrayList(Int::class.java.classLoader)
            },
            arrayListOf<Int>().apply {
                parcel.readArrayList(Int::class.java.classLoader)
            },
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            arrayListOf<Int>().apply {
                parcel.readArrayList(Int::class.java.classLoader)
            },
            arrayListOf<Int>().apply {
                parcel.readArrayList(Int::class.java.classLoader)
            },
            arrayListOf<Int>().apply {
                parcel.readArrayList(Int::class.java.classLoader)
            }) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeList(submittedOnDate)
        parcel.writeString(submittedByUsername)
        parcel.writeString(submittedByFirstname)
        parcel.writeString(submittedByLastname)
        parcel.writeList(approvedOnDate)
        parcel.writeString(approvedByUsername)
        parcel.writeString(approvedByFirstname)
        parcel.writeString(approvedByLastname)
        parcel.writeList(expectedDisbursementDate)
        parcel.writeList(actualDisbursementDate)
        parcel.writeString(disbursedByUsername)
        parcel.writeString(disbursedByFirstname)
        parcel.writeString(disbursedByLastname)
        parcel.writeList(closedOnDate)
        parcel.writeList(expectedMaturityDate)
        parcel.writeList(withdrawnOnDate)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object {

        @JvmField
        final var CREATOR: Parcelable.Creator<Timeline> = object : Parcelable.Creator<Timeline> {
            override fun createFromParcel(parcel: Parcel): Timeline {
                return Timeline(parcel)
            }

            override fun newArray(size: Int): Array<Timeline?> {
                return arrayOfNulls(size)
            }
        }
    }
}