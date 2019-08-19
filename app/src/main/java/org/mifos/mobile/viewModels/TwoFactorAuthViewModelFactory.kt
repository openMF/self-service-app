package org.mifos.mobile.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.mifos.mobile.data.TwoFactorAuthRepository
import org.mifos.mobile.injection.PerActivity
import javax.inject.Inject

@PerActivity
class TwoFactorAuthViewModelFactory @Inject constructor(private val twoFactorAuthRepository : TwoFactorAuthRepository)
    : ViewModelProvider.NewInstanceFactory(){

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TwoFactorAuthViewModel::class.java)) {
            return TwoFactorAuthViewModel(twoFactorAuthRepository) as T
        } else if (modelClass.isAssignableFrom(EnableTwoFactorAuthViewModel::class.java)) {
            return EnableTwoFactorAuthViewModel(twoFactorAuthRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: " + modelClass.name)
    }
}