package org.mifos.mobile.models.beneficiary

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * Created by dilpreet on 16/6/17.
 */

@Parcelize
data class BeneficiaryPayload(
        internal var locale: String = "en_GB",

        var name: String? = null,

        var accountNumber: String? = null,

        var accountType: Int? = 0,

        var transferLimit: Int? = 0,

        var officeName: String? = null
) : Parcelable