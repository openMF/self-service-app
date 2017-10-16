package org.mifos.selfserviceapp.injection.component;

import org.mifos.selfserviceapp.injection.module.TestApplicationModule;

import javax.inject.Singleton;

import dagger.Component;

/**
 * Created by dilpreet on 21/7/17.
 */
@Singleton
@Component(modules = TestApplicationModule.class)
public interface TestApplicationComponent extends ApplicationComponent {

}
