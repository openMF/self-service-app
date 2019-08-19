package org.mifos.mobile.models

import java.util.ArrayList

/**
 * @author Vishwajeet
 * @since 12/06/16
 */

data class User (

    var userId: Long = 0,
    var isAuthenticated: Boolean = false,
    var username: String? = null,
    var base64EncodedAuthenticationKey: String? = null,
    var permissions: List<String> = ArrayList(),
    var googleAutenticatorKey: String = ""
    )
