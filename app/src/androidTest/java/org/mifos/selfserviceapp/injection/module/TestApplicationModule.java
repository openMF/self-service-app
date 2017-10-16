package org.mifos.selfserviceapp.injection.module;

import android.app.Application;
import android.content.Context;

import org.mifos.selfserviceapp.api.BaseApiManager;
import org.mifos.selfserviceapp.api.MockedBaseApiManager;
import org.mifos.selfserviceapp.api.local.PreferencesHelper;
import org.mifos.selfserviceapp.injection.ApplicationContext;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by dilpreet on 21/7/17.
 */
@Module
public class TestApplicationModule {
    private Application application;

    public TestApplicationModule(Application application) {
        this.application = application;
    }

    @Provides
    Application provideApplication() {
        return application;
    }

    @Provides
    @ApplicationContext
    Context provideContext() {
        return application;
    }

    @Provides
    @Singleton
    PreferencesHelper providePrefManager(@ApplicationContext Context context) {
        return new PreferencesHelper(context);
    }

    @Provides
    @Singleton
    BaseApiManager provideBaseApiManager() {
        return new MockedBaseApiManager();
    }
}
