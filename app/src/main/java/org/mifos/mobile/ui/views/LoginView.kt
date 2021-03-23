package org.mifos.mobile.ui.views

import org.mifos.mobile.ui.views.base.MVPView

/**
 * @author Vishwajeet
 * @since 05/06/16
 */
interface LoginView : MVPView {
    /**
     * Should be called when the user credentials are successfully
     * authenticated from the API.
     */
    fun onLoginSuccess()

    /**
     * Should be called when there is a problem with the user input that we
     * requested or with trying to authenticate the user from the API.
     *
     * The error could be of many types like
     *
     *  * Invalid Login Credentials
     *  * No network connection
     *
     *
     * The problem must be communicated back to the user clearly.
     * @param errorMessage Error message that tells the user about the problem.
     */
    fun showMessage(errorMessage: String?)
    fun showUsernameError(error: String?)
    fun showPasswordError(error: String?)
    fun clearUsernameError()
    fun clearPasswordError()

    /**
     * Should be called when the client is fetched successfully from API.
     * The client's name would be passed to the view so that we can
     * at least say hello!
     *
     * @param clientName name of the Client
     */
    fun showPassCodeActivity(clientName: String?)
}