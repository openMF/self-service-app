package org.mifos.mobile.ui

import org.mifos.mobile.models.accounts.loan.LoanAccount
import org.mifos.mobile.models.client.DepositType
import org.mifos.mobile.ui.views.base.MVPView

interface AccountsContract {
    interface View: MVPView {
        fun showLoanAccounts(loanAccount: List<LoanAccount>)
        fun showDepositAccounts (depositType: DepositType)
        fun showEmptyAccounts(feature: String)
        fun showError(message: String)

    }
    interface Presenter {
        fun loadLoanAccounts()
        fun loadDepositAccounts()
    }

}