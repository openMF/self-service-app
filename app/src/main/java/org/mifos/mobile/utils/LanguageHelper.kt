package org.mifos.mobile.utils

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.view.View
import androidx.core.text.TextUtilsCompat

import androidx.preference.PreferenceManager

import org.mifos.mobile.R

import java.util.*

/**
 * Created by dilpreet on 02/10/17.
 */
object LanguageHelper {
    //https://gunhansancar.com/change-language-programmatically-in-android/
    fun onAttach(context: Context): Context? {
        val lang = getPersistedData(context, Locale.getDefault().language)
        return lang?.let { setLocale(context, it) }
    }

    @kotlin.jvm.JvmStatic
    fun onAttach(context: Context, defaultLanguage: String): Context? {
        val lang = getPersistedData(context, defaultLanguage)
        return lang?.let { setLocale(context, it) }
    }

    fun setLocale(context: Context?, language: String): Context? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            updateResources(context!!, language)
        } else updateResourcesLegacy(context, language)
    }

    @TargetApi(Build.VERSION_CODES.O)
    fun isRTL(): Boolean {
        return TextUtilsCompat.getLayoutDirectionFromLocale(Locale.getDefault()) == View.LAYOUT_DIRECTION_LOCALE;
    }

    private fun getPersistedData(context: Context, defaultLanguage: String): String? {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        return preferences.getString(context.getString(R.string.language_type), defaultLanguage)
    }

    @TargetApi(Build.VERSION_CODES.N)
    private fun updateResources(context: Context, language: String): Context {
        val locale = Locale(language)
        Locale.setDefault(locale)
        val configuration = context.resources.configuration
        configuration.setLocale(locale)
        configuration.setLayoutDirection(locale)
        return context.createConfigurationContext(configuration)
    }

    private fun updateResourcesLegacy(context: Context?, language: String): Context? {
        val locale = Locale(language)
        Locale.setDefault(locale)
        val resources = context?.resources
        val configuration = resources?.configuration
        configuration?.locale = locale
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            configuration?.setLayoutDirection(locale)
        }
        resources?.updateConfiguration(configuration, resources.displayMetrics)
        return context
    }
}