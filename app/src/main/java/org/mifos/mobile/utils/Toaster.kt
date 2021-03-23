package org.mifos.mobile.utils

import android.content.Context
import android.graphics.Color
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.TextView

import com.google.android.material.snackbar.Snackbar
import org.mifos.mobile.MifosSelfServiceApp
import org.mifos.mobile.R
import java.util.*

object Toaster {

    private val snackbarsQueue = ArrayList<Snackbar>()

    @JvmOverloads
    fun show(view: View?, text: String?, duration: Int = Snackbar.LENGTH_LONG) {
        val imm = MifosSelfServiceApp.context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view?.windowToken, 0)
        val snackbar = Snackbar.make(view!!, text!!, duration)
        MifosSelfServiceApp.context?.resources?.getColor(R.color.primary)?.let {
            snackbar.setActionTextColor(it)
        }
        val sbView = snackbar.view
        val textView = sbView.findViewById<TextView>(R.id.snackbar_text)
        textView.setTextColor(Color.WHITE)
        textView.textSize = 12f
        snackbar.setAction("OK") { }
        snackbar.addCallback(object : Snackbar.Callback() {

            override fun onDismissed(transientBottomBar: Snackbar, event: Int) {
                super.onDismissed(transientBottomBar, event)
                if (snackbarsQueue.isNotEmpty()) {
                    snackbarsQueue.removeAt(0)
                    if (snackbarsQueue.isNotEmpty()) {
                        snackbarsQueue[0].show()
                    }
                }
            }
        })
        snackbarsQueue.add(snackbar)
        if (!snackbarsQueue[0].isShown) {
            snackbarsQueue[0].show()
        }
    }

    fun show(view: View, res: Int, duration: Int) {
        show(view, MifosSelfServiceApp.context?.resources?.getString(res), duration)
    }

    fun cancelTransfer(
            view: View?, text: String?, buttonText: String?,
            listener: View.OnClickListener?
    ) {
        val snackbar = Snackbar.make(view!!, text!!, Snackbar.LENGTH_LONG)
        snackbar.setAction(buttonText, listener)
        snackbar.show()
    }


    fun show(view: View?, res: Int) {
        show(view, MifosSelfServiceApp.context?.resources?.getString(res))
    }
}