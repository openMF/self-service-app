package org.mifos.mobile.ui.fragments.base

import android.app.Activity
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment

import org.mifos.mobile.ui.views.BaseActivityCallback
import org.mifos.mobile.utils.LanguageHelper
import org.mifos.mobile.utils.ProgressBarHandler

open class BaseFragment : Fragment() {
    private var callback: BaseActivityCallback? = null
    private var progressBarHandler: ProgressBarHandler? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        progressBarHandler = activity?.let { ProgressBarHandler(it) }
    }

    /**
     * Used to show Progress bar. `callback` is implemented in
     * [org.mifos.mobile.ui.activities.base.BaseActivity]
     * @param message Message you want to display
     */
    protected fun showMifosProgressDialog(message: String?) {
        if (callback != null) callback?.showProgressDialog(message)
    }

    /**
     * Used for hiding the progressbar.
     */
    protected fun hideMifosProgressDialog() {
        if (callback != null) callback?.hideProgressDialog()
    }

    /**
     * Used for setting title of Toolbar
     * @param title String you want to display as title
     */
    protected fun setToolbarTitle(title: String?) {
        callback?.setToolbarTitle(title)
    }

    /**
     * Displays [ProgressBarHandler]
     */
    protected fun showProgressBar() {
        progressBarHandler?.show()
    }

    /**
     * Hides [ProgressBarHandler]
     */
    protected fun hideProgressBar() {
        progressBarHandler?.hide()
    }

    override fun onAttach(context: Context) {
        super.onAttach(LanguageHelper.onAttach(context)!!)
        val activity = if (context is Activity) context else null
        callback = try {
            activity as BaseActivityCallback?
        } catch (e: ClassCastException) {
            throw ClassCastException(activity.toString()
                    + " must implement BaseActivityCallback methods")
        }
    }

    override fun onDetach() {
        super.onDetach()
        callback = null
    }
}