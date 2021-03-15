package org.mifos.mobile.ui.activities

import android.content.Intent
import android.os.Bundle

import org.mifos.mobile.api.local.PreferencesHelper
import org.mifos.mobile.ui.activities.base.BaseActivity
import org.mifos.mobile.utils.Constants

/*
* Created by saksham on 01/June/2018
*/
class SplashActivity : BaseActivity() {

    private var preferencesHelper: PreferencesHelper? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        val intent: Intent?
        super.onCreate(savedInstanceState)
        activityComponent?.inject(this)
        preferencesHelper = PreferencesHelper(this)
        if (preferencesHelper?.isAuthenticated == true) {
            if (preferencesHelper?.usePasscode == true) {
                intent = Intent(this, PassCodeActivity::class.java)
                intent.putExtra(Constants.INTIAL_LOGIN, true)
            } else {
                intent = Intent(this, HomeActivity::class.java)
            }
        } else {
            intent = Intent(this, LoginActivity::class.java)
        }
        startActivity(intent)
        finish()
    }
}