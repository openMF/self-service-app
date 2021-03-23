package org.mifos.mobile

import android.content.Context

import androidx.appcompat.app.AppCompatDelegate
import androidx.multidex.MultiDex
import androidx.multidex.MultiDexApplication

import com.mifos.mobile.passcode.utils.ForegroundChecker
import com.raizlabs.android.dbflow.config.FlowConfig
import com.raizlabs.android.dbflow.config.FlowManager

import com.google.firebase.crashlytics.FirebaseCrashlytics

import org.mifos.mobile.injection.component.ApplicationComponent
import org.mifos.mobile.injection.component.DaggerApplicationComponent
import org.mifos.mobile.injection.module.ApplicationModule
import org.mifos.mobile.utils.LanguageHelper.onAttach

import java.util.*

/**
 * @author ishan
 * @since 08/07/16
 */
class MifosSelfServiceApp : MultiDexApplication() {
    private var applicationComponent: ApplicationComponent? = null

    companion object {
        private var instance: MifosSelfServiceApp? = null
        operator fun get(context: Context): MifosSelfServiceApp {
            return context.applicationContext as MifosSelfServiceApp
        }

        val context: Context?
            get() = instance

        init {
            AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        }
    }

    override fun onCreate() {
        super.onCreate()
        MultiDex.install(this)
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true)
        instance = this
        FlowManager.init(FlowConfig.Builder(this).build())
        ForegroundChecker.init(this)
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(onAttach(base, Locale.getDefault().language))
    }

    fun component(): ApplicationComponent? {
        if (applicationComponent == null) {
            applicationComponent = DaggerApplicationComponent.builder()
                    .applicationModule(ApplicationModule(this))
                    .build()
        }
        return applicationComponent
    }

    // Needed to replace the component with a test specific one
    fun setComponent(applicationComponent: ApplicationComponent?) {
        this.applicationComponent = applicationComponent
    }
}