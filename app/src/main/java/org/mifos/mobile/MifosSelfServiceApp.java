package org.mifos.mobile;

import android.app.Activity;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.multidex.MultiDex;
import androidx.multidex.MultiDexApplication;
import androidx.work.Worker;

import com.crashlytics.android.Crashlytics;
import com.mifos.mobile.passcode.utils.ForegroundChecker;
import com.raizlabs.android.dbflow.config.FlowConfig;
import com.raizlabs.android.dbflow.config.FlowManager;

import org.jetbrains.annotations.NotNull;
import org.mifos.mobile.injection.component.ApplicationComponent;
import org.mifos.mobile.injection.component.DaggerApplicationComponent;
import org.mifos.mobile.injection.module.ApplicationModule;
import org.mifos.mobile.utils.LanguageHelper;

import java.util.Locale;

import chat.rocket.android.app.RocketChatInitializer;
import chat.rocket.android.app.RocketChatInjector;
import dagger.android.AndroidInjector;
import io.fabric.sdk.android.Fabric;

/**
 * @author ishan
 * @since 08/07/16
 */
public class MifosSelfServiceApp extends MultiDexApplication implements RocketChatInjector {

    ApplicationComponent applicationComponent;

    private static MifosSelfServiceApp instance;

    public static MifosSelfServiceApp get(Context context) {
        return (MifosSelfServiceApp) context.getApplicationContext();
    }

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    public static Context getContext() {
        return instance;
    }

    RocketChatInitializer rocketChatInitializer;

    @Override
    public void onCreate() {
        super.onCreate();
        rocketChatInitializer = new RocketChatInitializer(this);
        rocketChatInitializer.init();
        MultiDex.install(this);
        Fabric.with(this, new Crashlytics());
        instance = this;
        FlowManager.init(new FlowConfig.Builder(this).build());
        ForegroundChecker.init(this);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LanguageHelper.onAttach(base, Locale.getDefault().getLanguage()));
    }

    public ApplicationComponent component() {
        if (applicationComponent == null) {
            applicationComponent = DaggerApplicationComponent.builder()
                    .applicationModule(new ApplicationModule(this))
                    .build();
        }
        return applicationComponent;
    }

    // Needed to replace the component with a test specific one
    public void setComponent(ApplicationComponent applicationComponent) {
        this.applicationComponent = applicationComponent;
    }

    @NotNull
    @Override
    public AndroidInjector<Activity> activityInjector() {
        return rocketChatInitializer.activityInjector();
    }

    @NotNull
    @Override
    public AndroidInjector<BroadcastReceiver> broadcastReceiverInjector() {
        return rocketChatInitializer.broadcastReceiverInjector();
    }

    @NotNull
    @Override
    public AndroidInjector<Service> serviceInjector() {
        return rocketChatInitializer.serviceInjector();
    }

    @NotNull
    @Override
    public AndroidInjector<Worker> workerInjector() {
        return rocketChatInitializer.workerInjector();
    }
}
