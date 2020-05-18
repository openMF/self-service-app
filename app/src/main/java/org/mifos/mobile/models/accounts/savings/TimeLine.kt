package org.mifos.mobile.models.accounts.savings

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import java.util.*

/**
 * Created by Rajan Maurya on 05/03/17.
 */

data class TimeLine(
        @SerializedName("submittedOnDate")
        var submittedOnDate: List<Int> = ArrayList(),

        @SerializedName("submittedByUsername")
        var submittedByUsername: String,

        @SerializedName("submittedByFirstname")
        var submittedByFirstname: String,

        @SerializedName("submittedByLastname")
        var submittedByLastname: String,

        @SerializedName("approvedOnDate")
        var approvedOnDate: List<Int> = ArrayList(),

        @SerializedName("approvedByUsername")
        var approvedByUsername: String? = null,

        @SerializedName("approvedByFirstname")
        var approvedByFirstname: String? = null,

        @SerializedName("approvedByLastname")
        var approvedByLastname: String? = null,

        @SerializedName("activatedOnDate")
        var activatedOnDate: List<Int>,

        @SerializedName("activatedByUsername")
        var activatedByUsername: String? = null,

        @SerializedName("activatedByFirstname")
        var activatedByFirstname: String? = null,

        @SerializedName("activatedByLastname")
        var activatedByLastname: String? = null,

        @SerializedName("closedOnDate")
        var closedOnDate: List<Int>

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
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
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
        parcel.writeList(activatedOnDate)
        parcel.writeString(activatedByUsername)
        parcel.writeString(activatedByFirstname)
        parcel.writeString(activatedByLastname)
        parcel.writeList(closedOnDate)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object {

        @JvmField
        final val CREATOR: Parcelable.Creator<TimeLine> = object : Parcelable.Creator<TimeLine> {
            override fun createFromParcel(parcel: Parcel): TimeLine {
                return TimeLine(parcel)
            }

            override fun newArray(size: Int): Array<TimeLine?> {
                return arrayOfNulls(size)
            }
        }
    }
}
