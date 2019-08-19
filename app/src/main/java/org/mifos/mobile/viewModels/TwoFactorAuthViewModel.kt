package org.mifos.mobile.viewModels

import androidx.lifecycle.ViewModel
import com.warrenstrange.googleauth.GoogleAuthenticator
import org.mifos.mobile.data.TwoFactorAuthRepository
import org.mifos.mobile.utils.GoogleAuthUtil

class TwoFactorAuthViewModel(twoFactorAuthRepository: TwoFactorAuthRepository) : ViewModel() {

    init{
        GoogleAuthUtil.init()
    }

    private val gAuth = GoogleAuthenticator()
    private val sharedKey = twoFactorAuthRepository.getSharedKey()

    fun verifyCode(code :Int):Boolean{
        return gAuth.authorize(sharedKey, code)
    }

}
