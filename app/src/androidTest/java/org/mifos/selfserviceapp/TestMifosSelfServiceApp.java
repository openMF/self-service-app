package org.mifos.selfserviceapp;

import org.mifos.selfserviceapp.injection.component.ApplicationComponent;
import org.mifos.selfserviceapp.injection.component.DaggerTestApplicationComponent;
import org.mifos.selfserviceapp.injection.component.TestApplicationComponent;
import org.mifos.selfserviceapp.injection.module.TestApplicationModule;

/**
 * Created by dilpreet on 21/7/17.
 */

public class TestMifosSelfServiceApp extends MifosSelfServiceApp {

    private TestApplicationComponent testApplicationComponent;
    @Override
    public void onCreate() {
        super.onCreate();
        testApplicationComponent = DaggerTestApplicationComponent.builder()
                .testApplicationModule(new TestApplicationModule(this))
                .build();
    }

    @Override
    public ApplicationComponent component() {
        return testApplicationComponent;
    }
}
