package org.mifos.mobile;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.crashlytics.android.Crashlytics;
import com.mifos.mobile.passcode.utils.ForegroundChecker;
import com.raizlabs.android.dbflow.config.FlowConfig;
import com.raizlabs.android.dbflow.config.FlowManager;

import org.mifos.mobile.injection.component.ApplicationComponent;
import org.mifos.mobile.injection.component.DaggerApplicationComponent;
import org.mifos.mobile.injection.module.ApplicationModule;
import org.mifos.mobile.utils.LanguageHelper;
import org.mifos.mobile.utils.ThemeHelper;

import java.util.Locale;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.multidex.MultiDex;
import androidx.multidex.MultiDexApplication;
import io.fabric.sdk.android.Fabric;

/**
 * @author ishan
 * @since 08/07/16
 */
public class MifosSelfServiceApp extends MultiDexApplication {

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

    @Override
    public void onCreate() {
        super.onCreate();
        MultiDex.install(this);
        Fabric.with(this, new Crashlytics());
        instance = this;
        FlowManager.init(new FlowConfig.Builder(this).build());
        ForegroundChecker.init(this);
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(this);
        String themePref = sharedPreferences.getString("dark_mode", ThemeHelper.DEFAULT_MODE);
        ThemeHelper.applyTheme(themePref);
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
}
