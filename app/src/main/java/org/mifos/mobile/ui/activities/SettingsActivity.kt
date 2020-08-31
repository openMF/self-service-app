package org.mifos.mobile.ui.activities

import android.os.Bundle
import org.mifos.mobile.R
import org.mifos.mobile.ui.activities.base.BaseActivity
import org.mifos.mobile.ui.fragments.SettingsFragment

class SettingsActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        setToolbarTitle(getString(R.string.settings))
        showBackButton()
        replaceFragment(SettingsFragment.newInstance(), false, R.id.container)
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }
}