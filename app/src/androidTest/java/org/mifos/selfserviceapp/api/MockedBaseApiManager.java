package org.mifos.selfserviceapp.api;

import org.mifos.selfserviceapp.api.services.AuthenticationService;
import org.mifos.selfserviceapp.api.services.BeneficiaryService;
import org.mifos.selfserviceapp.api.services.ClientChargeService;
import org.mifos.selfserviceapp.api.services.ClientService;
import org.mifos.selfserviceapp.api.services.LoanAccountsListService;
import org.mifos.selfserviceapp.api.services.MockedClientService;
import org.mifos.selfserviceapp.api.services.RecentTransactionsService;
import org.mifos.selfserviceapp.api.services.SavingAccountsListService;
import org.mifos.selfserviceapp.api.services.ThirdPartyTransferService;

/**
 * Created by dilpreet on 21/7/17.
 */

public class MockedBaseApiManager extends BaseApiManager {

    @Override
    public AuthenticationService getAuthenticationApi() {
        return super.getAuthenticationApi();
    }

    @Override
    public ClientService getClientsApi() {
        return new MockedClientService();
    }

    @Override
    public SavingAccountsListService getSavingAccountsListApi() {
        return super.getSavingAccountsListApi();
    }

    @Override
    public LoanAccountsListService getLoanAccountsListApi() {
        return super.getLoanAccountsListApi();
    }

    @Override
    public RecentTransactionsService getRecentTransactionsApi() {
        return super.getRecentTransactionsApi();
    }

    @Override
    public ClientChargeService getClientChargeApi() {
        return super.getClientChargeApi();
    }

    @Override
    public BeneficiaryService getBeneficiaryApi() {
        return super.getBeneficiaryApi();
    }

    @Override
    public ThirdPartyTransferService getThirdPartyTransferApi() {
        return super.getThirdPartyTransferApi();
    }
}
