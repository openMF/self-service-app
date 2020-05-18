package org.mifos.mobile.injection.component;

import org.mifos.mobile.injection.PerActivity;
import org.mifos.mobile.injection.module.ActivityModule;
import org.mifos.mobile.ui.activities.HomeActivity;
import org.mifos.mobile.ui.activities.SplashActivity;
import org.mifos.mobile.ui.fragments.AccountOverviewFragment;
import org.mifos.mobile.ui.fragments.AddGuarantorFragment;
import org.mifos.mobile.ui.fragments.BeneficiaryAddOptionsFragment;
import org.mifos.mobile.ui.activities.PassCodeActivity;
import org.mifos.mobile.ui.fragments.BeneficiaryApplicationFragment;
import org.mifos.mobile.ui.fragments.BeneficiaryDetailFragment;
import org.mifos.mobile.ui.fragments.BeneficiaryListFragment;
import org.mifos.mobile.ui.fragments.ChargeDetailsFragment;
import org.mifos.mobile.ui.fragments.GuarantorDetailFragment;
import org.mifos.mobile.ui.fragments.GuarantorListFragment;
import org.mifos.mobile.ui.fragments.HelpFragment;
import org.mifos.mobile.ui.fragments.HomeOldFragment;
import org.mifos.mobile.ui.fragments.LoanAccountTransactionFragment;
import org.mifos.mobile.ui.fragments.LoanAccountWithdrawFragment;
import org.mifos.mobile.ui.fragments.LoanAccountsDetailFragment;
import org.mifos.mobile.ui.activities.LoginActivity;
import org.mifos.mobile.ui.fragments.LoanApplicationFragment;
import org.mifos.mobile.ui.fragments.NotificationFragment;
import org.mifos.mobile.ui.fragments.QrCodeImportFragment;
import org.mifos.mobile.ui.fragments.RegistrationFragment;
import org.mifos.mobile.ui.fragments.RegistrationVerificationFragment;
import org.mifos.mobile.ui.fragments.ReviewLoanApplicationFragment;
import org.mifos.mobile.ui.fragments.SavingAccountsDetailFragment;
import org.mifos.mobile.ui.fragments.AccountsFragment;
import org.mifos.mobile.ui.fragments.HomeFragment;
import org.mifos.mobile.ui.fragments.ClientAccountsFragment;
import org.mifos.mobile.ui.fragments.ClientChargeFragment;
import org.mifos.mobile.ui.fragments.LoanAccountSummaryFragment;
import org.mifos.mobile.ui.fragments.LoanRepaymentScheduleFragment;
import org.mifos.mobile.ui.fragments.RecentTransactionsFragment;
import org.mifos.mobile.ui.fragments.SavingAccountsTransactionFragment;
import org.mifos.mobile.ui.fragments.SavingsAccountApplicationFragment;
import org.mifos.mobile.ui.fragments.SavingsAccountWithdrawFragment;
import org.mifos.mobile.ui.fragments.SavingsMakeTransferFragment;
import org.mifos.mobile.ui.fragments.ThirdPartyTransferFragment;
import org.mifos.mobile.ui.fragments.TransferProcessFragment;
import org.mifos.mobile.ui.fragments.UpdatePasswordFragment;
import org.mifos.mobile.ui.fragments.UserProfileFragment;

import dagger.Component;

/**
 * @author ishan
 * @since 08/07/16
 */
@PerActivity
@Component(dependencies = ApplicationComponent.class, modules = ActivityModule.class)
public interface ActivityComponent {

    void inject(LoginActivity loginActivity);

    void inject(HomeActivity homeActivity);

    void inject(PassCodeActivity passCodeActivity);

    void inject(HomeFragment homeFragment);

    void inject(ClientAccountsFragment clientAccountsFragment);

    void inject(RecentTransactionsFragment recentTransactionsFragment);

    void inject(ClientChargeFragment clientChargeFragment);

    void inject(SavingAccountsDetailFragment savingAccountsDetailActivity);

    void inject(LoanAccountsDetailFragment loanAccountsDetailActivity);

    void inject(AccountsFragment accountsFragment);

    void inject(LoanAccountSummaryFragment loanAccountSummaryFragment);

    void inject(LoanAccountTransactionFragment loanAccountTransactionFragment);

    void inject(LoanRepaymentScheduleFragment loanRepaymentScheduleFragment);

    void inject(LoanApplicationFragment loanApplicationFragment);

    void inject(LoanAccountWithdrawFragment loanAccountWithdrawFragment);

    void inject(SavingAccountsTransactionFragment savingAccountsTransactionFragment);

    void inject(SavingsMakeTransferFragment savingsMakeTransferFragment);

    void inject(BeneficiaryAddOptionsFragment beneficiaryAddOptionsFragment);

    void inject(BeneficiaryListFragment beneficiaryListFragment);

    void inject(BeneficiaryApplicationFragment beneficiaryApplicationFragment);

    void inject(BeneficiaryDetailFragment beneficiaryDetailFragment);

    void inject(ThirdPartyTransferFragment thirdPartyTransferFragment);

    void inject(TransferProcessFragment transferProcessFragment);

    void inject(UserProfileFragment userProfileFragment);

    void inject(HelpFragment helpFragment);

    void inject(RegistrationFragment registrationFragment);

    void inject(RegistrationVerificationFragment registrationVerificationFragment);

    void inject(AccountOverviewFragment accountOverviewFragment);

    void inject(HomeOldFragment homeOldFragment);

    void inject(NotificationFragment notificationFragment);
    
    void inject(QrCodeImportFragment qrCodeImportFragment);

    void inject(SplashActivity splashActivity);

    void inject(AddGuarantorFragment addGuarantorFragment);

    void inject(GuarantorListFragment guarantorListFragment);

    void inject(GuarantorDetailFragment guarantorDetailFragment);

    void inject(UpdatePasswordFragment updatePasswordFragment);

    void inject(SavingsAccountApplicationFragment savingsAccountApplicationFragment);

    void inject(SavingsAccountWithdrawFragment savingsAccountWithdrawFragment);

    void inject(ReviewLoanApplicationFragment reviewLoanApplicationFragment);

    void inject(ChargeDetailsFragment chargeDetailsFragment);
}
