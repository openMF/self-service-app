package org.mifos.mobile.ui.views;

import org.mifos.mobile.models.Charge;
import org.mifos.mobile.ui.views.base.MVPView;


public interface ChargeDetailsView extends MVPView {

    void showErrorFetchingChargeDetails(String message);

    void showChargeDetails(Charge charge);
}