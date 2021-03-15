package org.mifos.mobile.api.local

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.text.TextUtils

import org.mifos.mobile.api.BaseURL
import org.mifos.mobile.api.SelfServiceInterceptor
import org.mifos.mobile.injection.ApplicationContext

import javax.inject.Inject
import javax.inject.Singleton

/**
 * @author Vishwajeet
 * @since 07/06/16
 */
@Singleton
class PreferencesHelper @Inject constructor(@ApplicationContext context: Context?) {
    private val sharedPreferences: SharedPreferences? = PreferenceManager.getDefaultSharedPreferences(context)
    fun clear() {
        val editor = sharedPreferences?.edit()
        //prevent deletion of url and tenant
        if (sharedPreferences != null)
            for ((key) in sharedPreferences.all) {
                if (!(key == BASE_URL || key == TENANT)) {
                    editor?.remove(key)
                }
            }
        editor?.apply()
    }

    fun getInt(preferenceKey: String?, preferenceDefaultValue: Int): Int? {
        return sharedPreferences?.getInt(preferenceKey, preferenceDefaultValue)
    }

    fun putInt(preferenceKey: String?, preferenceValue: Int) {
        sharedPreferences?.edit()?.putInt(preferenceKey, preferenceValue)?.apply()
    }

    fun getLong(preferenceKey: String?, preferenceDefaultValue: Long): Long? {
        return sharedPreferences?.getLong(preferenceKey, preferenceDefaultValue)
    }

    fun putLong(preferenceKey: String?, preferenceValue: Long) {
        sharedPreferences?.edit()?.putLong(preferenceKey, preferenceValue)?.apply()
    }

    fun getString(preferenceKey: String?, preferenceDefaultValue: String?): String? {
        return sharedPreferences?.getString(preferenceKey, preferenceDefaultValue)
    }

    private fun putString(preferenceKey: String?, preferenceValue: String?) {
        sharedPreferences?.edit()?.putString(preferenceKey, preferenceValue)?.apply()
    }

    fun putBoolean(preferenceKey: String?, preferenceValue: Boolean) {
        sharedPreferences?.edit()?.putBoolean(preferenceKey, preferenceValue)?.apply()
    }

    fun getBoolean(preferenceKey: String?, preferenceDefaultValue: Boolean): Boolean? {
        return sharedPreferences?.getBoolean(preferenceKey, preferenceDefaultValue)
    }

    fun saveToken(token: String?) {
        putString(TOKEN, token)
    }

    fun clearToken() {
        putString(TOKEN, "")
    }

    val token: String?
        get() = getString(TOKEN, "")
    val isAuthenticated: Boolean?
        get() = !TextUtils.isEmpty(token)
    var userId: Long?
        get() = getLong(USER_ID, -1)
        set(id) {
            if (id != null)
                putLong(USER_ID, id)
        }
    val tenant: String?
        get() = getString(TENANT, SelfServiceInterceptor.DEFAULT_TENANT)
    var passcode: String?
        get() = getString(PASSCODE, "")
        set(passcode) {
            putString(PASSCODE, passcode)
        }
    var clientId: Long?
        get() = getLong(CLIENT_ID, -1)
        set(clientId) {
            if (clientId != null)
                putLong(CLIENT_ID, clientId)
        }
    var userName: String?
        get() = getString(USER_NAME, "")
        set(userName) {
            putString(USER_NAME, userName)
        }
    var clientName: String?
        get() = getString(CLIENT_NAME, "")
        set(clientName) {
            putString(CLIENT_NAME, clientName)
        }
    var officeName: String?
        get() = getString(OFFICE_NAME, "")
        set(officeName) {
            putString(OFFICE_NAME, officeName)
        }
    var usePasscode: Boolean?
        get() = getBoolean(USE_PASSCODE, false)
        set(usePasscode) {
            putBoolean(USE_PASSCODE, usePasscode!!)
        }

    fun setOverviewState(state: Boolean) {
        putBoolean(OVERVIEW_STATE, state)
    }

    fun overviewState(): Boolean? {
        return getBoolean(OVERVIEW_STATE, true)
    }

    fun saveGcmToken(token: String?) {
        putString(GCM_TOKEN, token)
    }

    var userProfileImage: String?
        get() = getString(PROFILE_IMAGE, null)
        set(image) {
            putString(PROFILE_IMAGE, image)
        }
    val gcmToken: String?
        get() = getString(GCM_TOKEN, "")

    fun setSentTokenToServer(sentTokenToServer: Boolean) {
        putBoolean(SENT_TOKEN_TO_SERVER, sentTokenToServer)
    }

    fun sentTokenToServerState(): Boolean? {
        return getBoolean(SENT_TOKEN_TO_SERVER, false)
    }

    fun updateConfiguration(baseUrl: String?, tenant: String?) {
        sharedPreferences?.edit()
                ?.putString(BASE_URL, baseUrl)
                ?.putString(TENANT, tenant)
                ?.apply()
    }

    val baseUrl: String?
        get() = getString(BASE_URL, BaseURL().defaultBaseUrl)

    companion object {
        private const val USER_ID = "preferences_user_id"
        private const val TOKEN = "preferences_token"
        private const val CLIENT_ID = "preferences_client"
        private const val OFFICE_NAME = "preferences_office_name"
        private const val USER_NAME = "preferences_user_name"
        private const val PASSCODE = "preferences_passcode"
        private const val OVERVIEW_STATE = "preferences_overview_state"
        private const val SENT_TOKEN_TO_SERVER = "sentTokenToServer"
        private const val GCM_TOKEN = "gcm_token"
        private const val TENANT = "preferences_base_tenant"
        private const val BASE_URL = "preferences_base_url_key"
        private const val PROFILE_IMAGE = "preferences_profile_image"
        const val CLIENT_NAME = "client_name"
        private const val USE_PASSCODE = "preferences_use_passcode"
    }

}