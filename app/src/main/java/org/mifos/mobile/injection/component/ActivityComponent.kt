package org.mifos.mobile.injection.component

import android.accounts.Account
import dagger.Component

import org.mifos.mobile.injection.PerActivity
import org.mifos.mobile.injection.module.ActivityModule
import org.mifos.mobile.ui.activities.HomeActivity
import org.mifos.mobile.ui.activities.LoginActivity
import org.mifos.mobile.ui.activities.PassCodeActivity
import org.mifos.mobile.ui.activities.SplashActivity
import org.mifos.mobile.ui.fragments.*

/**
 * @author ishan
 * @since 08/07/16
 */
@PerActivity
@Component(dependencies = [ApplicationComponent::class], modules = [ActivityModule::class])
interface ActivityComponent {
    fun inject(loginActivity: LoginActivity?)
    fun inject(homeActivity: HomeActivity?)
    fun inject(passCodeActivity: PassCodeActivity?)
    fun inject(homeFragment: HomeFragment?)
    fun inject(clientAccountsFragment: ClientAccountsFragment?)
    fun inject(recentTransactionsFragment: RecentTransactionsFragment?)
    fun inject(clientChargeFragment: ClientChargeFragment?)
    fun inject(savingAccountsDetailActivity: SavingAccountsDetailFragment?)
    fun inject(loanAccountsDetailActivity: LoanAccountsDetailFragment?)
    fun inject(accountsFragment: AccountsFragment?)
    fun inject(loanAccountSummaryFragment: LoanAccountSummaryFragment?)
    fun inject(loanAccountTransactionFragment: LoanAccountTransactionFragment?)
    fun inject(loanRepaymentScheduleFragment: LoanRepaymentScheduleFragment?)
    fun inject(loanApplicationFragment: LoanApplicationFragment?)
    fun inject(loanAccountWithdrawFragment: LoanAccountWithdrawFragment?)
    fun inject(savingAccountsTransactionFragment: SavingAccountsTransactionFragment?)
    fun inject(savingsMakeTransferFragment: SavingsMakeTransferFragment?)
    fun inject(beneficiaryAddOptionsFragment: BeneficiaryAddOptionsFragment?)
    fun inject(beneficiaryListFragment: BeneficiaryListFragment?)
    fun inject(beneficiaryApplicationFragment: BeneficiaryApplicationFragment?)
    fun inject(beneficiaryDetailFragment: BeneficiaryDetailFragment?)
    fun inject(thirdPartyTransferFragment: ThirdPartyTransferFragment?)
    fun inject(transferProcessFragment: TransferProcessFragment?)
    fun inject(userProfileFragment: UserProfileFragment?)
    fun inject(helpFragment: HelpFragment?)
    fun inject(registrationFragment: RegistrationFragment?)
    fun inject(registrationVerificationFragment: RegistrationVerificationFragment?)
    fun inject(accountOverviewFragment: AccountOverviewFragment?)
    fun inject(homeOldFragment: HomeOldFragment?)
    fun inject(notificationFragment: NotificationFragment?)
    fun inject(qrCodeImportFragment: QrCodeImportFragment?)
    fun inject(splashActivity: SplashActivity?)
    fun inject(addGuarantorFragment: AddGuarantorFragment?)
    fun inject(guarantorListFragment: GuarantorListFragment?)
    fun inject(guarantorDetailFragment: GuarantorDetailFragment?)
    fun inject(updatePasswordFragment: UpdatePasswordFragment?)
    fun inject(savingsAccountApplicationFragment: SavingsAccountApplicationFragment?)
    fun inject(savingsAccountWithdrawFragment: SavingsAccountWithdrawFragment?)
    fun inject(reviewLoanApplicationFragment: ReviewLoanApplicationFragment?)
    fun inject(account : Account)
}