package org.mifos.mobile.ui.activities

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar

import butterknife.BindView
import butterknife.ButterKnife

import org.mifos.mobile.R
import org.mifos.mobile.ui.activities.base.BaseActivity

/**
 * @author Rajan Maurya
 * On 11/03/19.
 */
class PrivacyPolicyActivity : BaseActivity() {

    @JvmField
    @BindView(R.id.webView)
    var webView: WebView? = null

    @JvmField
    @BindView(R.id.progress_bar)
    var progressBar: ProgressBar? = null
    private var showOrHideWebViewInitialUse = "show"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_privacy_policy)
        ButterKnife.bind(this)
        setToolbarTitle(getString(R.string.privacy_policy))
        showBackButton()

        // Force links and redirects to open in the WebView instead of in a browser
        webView?.webViewClient = WebViewClient()

        // Enable Javascript
        val webSettings = webView?.settings
        webSettings?.javaScriptEnabled = true

        // REMOTE RESOURCE
        webView?.settings?.domStorageEnabled = true
        webView?.overScrollMode = WebView.OVER_SCROLL_NEVER
        webView?.loadUrl(getString(R.string.privacy_policy_host_url))
        webView?.webViewClient = MyWebViewClient()
    }

    override fun onBackPressed() {
        if (webView?.canGoBack() == true) {
            webView?.goBack()
        } else {
            super.onBackPressed()
        }
    }

    internal inner class MyWebViewClient : WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
            if (Uri.parse(url)?.host
                            ?.endsWith(getString(R.string.privacy_policy_host)) == true) {
                return false
            }
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            return true
        }

        override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
            // only make it invisible the FIRST time the app is run
            if (showOrHideWebViewInitialUse == "show") {
                webView?.visibility = View.INVISIBLE
            }
        }

        override fun onPageFinished(view: WebView, url: String) {
            showOrHideWebViewInitialUse = "hide"
            progressBar?.visibility = View.GONE
            view.visibility = View.VISIBLE
            super.onPageFinished(view, url)
        }
    }
}