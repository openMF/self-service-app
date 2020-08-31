package org.mifos.mobile.ui

import android.os.Bundle
import org.mifos.mobile.R
import org.mifos.mobile.ui.activities.base.BaseActivity

class Notification: BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification)
        showBackButton()
        setToolbarTitle(getString(R.string.notification))
    }
}