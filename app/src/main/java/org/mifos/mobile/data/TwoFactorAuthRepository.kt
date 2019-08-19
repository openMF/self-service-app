package org.mifos.mobile.data

import org.mifos.mobile.api.local.PreferencesHelper
import org.mifos.mobile.injection.PerActivity
import javax.inject.Inject

@PerActivity
class TwoFactorAuthRepository @Inject constructor() {

    @Inject
    internal lateinit var preferencesHelper: PreferencesHelper

    fun getSharedKey():String{
        return preferencesHelper.sharedKey
}

    fun setSharedKey(sharedKey: String) {
        preferencesHelper.sharedKey = sharedKey
        //TODO:: Update google_authenticator_key to server
    }
}