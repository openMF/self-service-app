package org.mifos.mobile.ui.views;

import org.mifos.mobile.ui.views.base.MVPView;

/**
 * @author Vishwajeet
 * @since 05/06/16
 */

public interface LoginView extends MVPView {

    /**
     * Should be called when the user credentials are successfully
     * authenticated from the API.
     * The username would be passed to the view so that we can
     * at least say hello!
     *
     * @param userName Username of the user that successfully logged in!
     */
    void onLoginSuccess(String userName);

    /**
     * Should be called when there is a problem with the user input that we
     * requested or with trying to authenticate the user from the API.
     *
     * The error could be of many types like
     * <ul>
     * <li>Invalid Login Credentials</li>
     * <li>No network connection</li>
     * </ul>
     *
     * The problem must be communicated back to the user clearly.
     * @param errorMessage Error message that tells the user about the problem.
     */
    void showMessage(String errorMessage);

    void showUsernameError(String error);

    void showPasswordError(String error);

    void clearUsernameError();

    void clearPasswordError();

    /**
     * Should be called when the client is fetched successfully from API.
     *
     */
    void showPassCodeActivity();

    void showTwoFactorAuthActivity(String userName);

}
