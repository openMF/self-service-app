package org.mifos.mobile.ui.views

import org.mifos.mobile.models.templates.account.AccountOptionsTemplate
import org.mifos.mobile.ui.views.base.MVPView

/**
 * Created by Rajan Maurya on 10/03/17.
 */
interface SavingsMakeTransferMvpView : MVPView {
    fun showUserInterface()
    fun showSavingsAccountTemplate(accountOptionsTemplate: AccountOptionsTemplate?)
    fun showToaster(message: String?)
    fun showError(message: String?)
    fun showProgressDialog()
    fun hideProgressDialog()
    fun updateAccountBalance(accountBalance: Double)
}