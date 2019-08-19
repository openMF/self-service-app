package org.mifos.mobile.viewModels

import androidx.lifecycle.ViewModel
import com.warrenstrange.googleauth.GoogleAuthenticator
import org.mifos.mobile.data.TwoFactorAuthRepository
import org.mifos.mobile.utils.GoogleAuthUtil

class EnableTwoFactorAuthViewModel(private val twoFactorAuthRepository: TwoFactorAuthRepository) : ViewModel() {

    init{
        GoogleAuthUtil.init()
    }

    private val gAuth = GoogleAuthenticator()
    private var generatedKey: String  = ""

    fun verifyCode(code :Int):Boolean{
        return gAuth.authorize(generatedKey, code)
    }

    fun getGenerateSharedKey(): String {
        if(generatedKey.isEmpty())
            generatedKey = gAuth.createCredentials().key
        return generatedKey
    }

    fun saveSharedKey() {
        twoFactorAuthRepository.setSharedKey(generatedKey)
    }
}
