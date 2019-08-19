package org.mifos.mobile.ui.fragments

/*
~This project is licensed under the open source MPL V2.
~See https://github.com/openMF/self-service-app/blob/master/LICENSE.md
*/

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProviders
import kotlinx.android.synthetic.main.fragment_enable_two_factor_auth.*
import org.mifos.mobile.R
import org.mifos.mobile.ui.activities.base.BaseActivity
import org.mifos.mobile.ui.fragments.base.BaseFragment
import org.mifos.mobile.utils.Constants
import org.mifos.mobile.utils.QrCodeGenerator
import org.mifos.mobile.utils.Toaster
import org.mifos.mobile.viewModels.EnableTwoFactorAuthViewModel
import org.mifos.mobile.viewModels.TwoFactorAuthViewModelFactory
import javax.inject.Inject


class EnableTwoFactorAuthFragment : BaseFragment() {

    @Inject
    lateinit var twoFactorAuthViewModelFactory: TwoFactorAuthViewModelFactory
    lateinit var rootView: View
    private lateinit var viewModel: EnableTwoFactorAuthViewModel

    companion object {
        fun newInstance(): EnableTwoFactorAuthFragment {
            val fragment = EnableTwoFactorAuthFragment()
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.fragment_enable_two_factor_auth, container,
                false)
        setToolbarTitle(getString(R.string.title_two_factor))

        (activity as BaseActivity).activityComponent.inject(this)
        viewModel = ViewModelProviders.of(this, twoFactorAuthViewModelFactory)
                .get(EnableTwoFactorAuthViewModel::class.java)

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        iv_qr_code.setImageBitmap(QrCodeGenerator.encodeAsBitmap(viewModel.getGenerateSharedKey()))
        tv_key.text = viewModel.getGenerateSharedKey()

        tv_key.setOnLongClickListener {
            copyText()
            true
        }

        btn_submit.setOnClickListener {
            verify()
        }
    }

    private fun verify() {
        val stCode = et_code.text.toString()
        if (stCode.isNotEmpty()) {
            try {
                val code = Integer.parseInt(stCode)
                val isCodeValid = viewModel.verifyCode(code)
                if (isCodeValid) {
                    viewModel.saveSharedKey()
                    moveToActivity()
                    Toaster.show(rootView, getString(R.string.totp_enabled))
                } else {
                    Toaster.show(rootView, getString(R.string.incorrect_code))
                }
            } catch (e: Exception) {
                Toaster.show(rootView, getString(R.string.invalid_code))
            }

        } else {
            Toaster.show(rootView, getString(R.string.enter_verification_code))
        }
    }

    private fun moveToActivity() {
        val intent = Intent(activity, activity!!::class.java).apply {
            putExtra(Constants.HAS_SETTINGS_CHANGED, true)
        }
        startActivity(intent)
        activity!!.finish()
    }

    private fun copyText() {
        val clipboard = activity!!.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(getString(R.string.key_label),
                tv_key.text.toString())
        clipboard.primaryClip = clip
        Toast.makeText(context, getString(R.string.toast_copied), Toast.LENGTH_SHORT).show()
    }
}
