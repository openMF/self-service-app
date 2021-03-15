package org.mifos.mobile.utils

import android.content.Context
import android.content.Intent
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import androidx.preference.DialogPreference.TargetFragment
import androidx.preference.Preference
import androidx.preference.PreferenceDialogFragmentCompat
import butterknife.BindView
import butterknife.ButterKnife
import com.google.android.material.textfield.TextInputLayout
import org.mifos.mobile.R
import org.mifos.mobile.api.BaseApiManager.Companion.createService
import org.mifos.mobile.api.local.PreferencesHelper
import org.mifos.mobile.ui.activities.LoginActivity
import java.net.MalformedURLException
import java.net.URL

/**
 * Created by dilpreet on 11/03/18.
 */
class ConfigurationDialogFragmentCompat : PreferenceDialogFragmentCompat(), TargetFragment, TextWatcher {
    @kotlin.jvm.JvmField
    @BindView(R.id.et_tenant)
    var etTenant: EditText? = null

    @kotlin.jvm.JvmField
    @BindView(R.id.et_base_url)
    var etBaseUrl: EditText? = null

    @kotlin.jvm.JvmField
    @BindView(R.id.til_tenant)
    var tilTenant: TextInputLayout? = null

    @kotlin.jvm.JvmField
    @BindView(R.id.til_base_url)
    var tilBaseUrl: TextInputLayout? = null
    private var preferencesHelper: PreferencesHelper? = null
    override fun onCreateDialogView(context: Context): View {
        return LayoutInflater.from(context).inflate(R.layout.preference_configuration, null)
    }

    override fun onBindDialogView(view: View) {
        super.onBindDialogView(view)
        ButterKnife.bind(this, view)
        preferencesHelper = PreferencesHelper(context)
        val preference = preference as ConfigurationPreference
        etBaseUrl?.setText(preference.baseUrl)
        etTenant?.setText(preference.tenant)
        etBaseUrl?.text?.length?.let { etBaseUrl?.setSelection(it) }
        etTenant?.addTextChangedListener(this)
        etBaseUrl?.addTextChangedListener(this)
    }

    override fun onDialogClosed(positiveResult: Boolean) {
        if (positiveResult && !isFieldEmpty && isUrlValid) {
            val preference = preference as ConfigurationPreference
            if (!(preference.baseUrl.toString().equals(etBaseUrl!!.text.toString()) && preference.tenant.toString().equals(etTenant!!.text.toString()))) {
                preference.updateConfigurations(etBaseUrl?.text.toString(), etTenant?.text.toString())
                preferencesHelper?.clear()
                val baseUrl = preferencesHelper?.baseUrl
                val tenant = preferencesHelper?.tenant
                createService(baseUrl, tenant, "")
                val loginIntent = Intent(activity, LoginActivity::class.java)
                loginIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(loginIntent)
                activity?.finish()
            }
        }
    }

    override fun findPreference(key: CharSequence): Preference {
        return preference
    }

    val isFieldEmpty: Boolean
        get() {
            if (etBaseUrl?.text.toString().trim { it <= ' ' }.isEmpty()) {
                return true
            }
            return etTenant?.text.toString().trim { it <= ' ' }.isEmpty()
        }
    val isUrlValid: Boolean
        get() = try {
            URL(etBaseUrl?.text.toString())
            true
        } catch (e: MalformedURLException) {
            false
        }

    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        if (s.toString().isEmpty()) {
            if (etBaseUrl?.text.toString().isEmpty()) {
                tilBaseUrl?.isErrorEnabled = true
                tilBaseUrl?.error = getString(R.string.error_validation_blank,
                        getString(R.string.base_url))
            }
            if (etTenant?.text.toString().isEmpty()) {
                tilTenant?.isErrorEnabled = true
                tilTenant?.error = getString(R.string.error_validation_blank,
                        getString(R.string.tenant))
            }
        } else {
            if (etBaseUrl?.text.toString().length != 0) {
                tilBaseUrl?.isErrorEnabled = false
            }
            if (etTenant?.text.toString().length != 0) {
                tilTenant?.isErrorEnabled = false
            }
        }
    }

    override fun afterTextChanged(s: Editable) {}
}