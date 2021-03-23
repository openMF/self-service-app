package org.mifos.mobile.ui.activities

import android.content.DialogInterface
import android.os.Bundle

import org.mifos.mobile.R
import org.mifos.mobile.ui.activities.base.BaseActivity
import org.mifos.mobile.ui.fragments.RegistrationFragment
import org.mifos.mobile.utils.MaterialDialog

class RegistrationActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)
        replaceFragment(RegistrationFragment.newInstance(), false, R.id.container)
    }

    override fun onBackPressed() {
        MaterialDialog.Builder().init(this)
                .setTitle(getString(R.string.dialog_cancel_registration_title))
                .setMessage(getString(R.string.dialog_cancel_registration_message))
                .setPositiveButton(getString(R.string.cancel),
                        DialogInterface.OnClickListener { _, _ -> super.onBackPressed() })
                .setNegativeButton(R.string.continue_str,
                        DialogInterface.OnClickListener { dialog, _ -> dialog.dismiss() })
                .createMaterialDialog()
                .show()
    }
}