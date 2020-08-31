package org.mifos.mobile.ui.activities.base;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.mifos.mobile.passcode.BasePassCodeActivity;

import org.mifos.mobile.MifosSelfServiceApp;
import org.mifos.mobile.R;
import org.mifos.mobile.injection.component.ActivityComponent;
import org.mifos.mobile.injection.component.DaggerActivityComponent;
import org.mifos.mobile.injection.module.ActivityModule;
import org.mifos.mobile.ui.activities.PassCodeActivity;
import org.mifos.mobile.ui.views.BaseActivityCallback;
import org.mifos.mobile.utils.LanguageHelper;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.transition.Fade;

/**
 * @author ishan
 * @since 08/07/16
 */
@SuppressLint("Registered")
public class BaseActivity extends BasePassCodeActivity implements BaseActivityCallback {

    protected Toolbar toolbar;
    private ActivityComponent activityComponent;
    private ProgressDialog progress;

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            setToolbarElevation();
        }
    }

    /**
     * Used for removing elevation from toolbar
     */
    public void hideToolbarElevation() {
        ViewCompat.setElevation(toolbar, 0);
    }

    /**
     * Used for setting toolbar elevation
     */
    public void setToolbarElevation() {
        ViewCompat.setElevation(toolbar, 8f);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * Used for dependency injection
     *
     * @return {@link ActivityComponent} which is used for injection
     */
    public ActivityComponent getActivityComponent() {
        if (activityComponent == null) {
            activityComponent = DaggerActivityComponent.builder()
                    .activityModule(new ActivityModule(this))
                    .applicationComponent(MifosSelfServiceApp.get(this).component())
                    .build();
        }
        return activityComponent;
    }

    /**
     * This method is use to provide back button feature in the toolbar of activities
     */
    protected void showBackButton() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Displays a toast in current activity. In this method the duration
     * supplied is Short by default. If you want to specify duration
     * use {@link BaseActivity#showToast(String, int)} method.
     *
     * @param message Message that the toast must show.
     */
    public void showToast(String message) {
        showToast(message, Toast.LENGTH_SHORT);
    }

    /**
     * Displays a toast in current activity. The duration can of two types:
     * <ul>
     * <li>SHORT</li>
     * <li>LONG</li>
     * </ul>
     *
     * @param message   Message that the toast must show.
     * @param toastType Duration for which the toast must be visible.
     */
    public void showToast(@NonNull String message, @NonNull int toastType) {
        Toast.makeText(BaseActivity.this, message, toastType).show();
    }

    /**
     * Calls a method {@code showProgressDialog("Working")} which displays ProgressDialog
     */
    public void showProgressDialog() {
        showProgressDialog(getString(R.string.working));
    }

    /**
     * Displays a ProgressDialog
     *
     * @param message Message you want to display in Progress Dialog
     */
    @Override
    public void showProgressDialog(String message) {
        if (progress == null) {
            progress = new ProgressDialog(this, ProgressDialog.STYLE_SPINNER);
            progress.setCancelable(false);
        }
        progress.setMessage(message);
        progress.show();

    }

    /**
     * Hides the progress dialog if it is currently being shown
     */
    @Override
    public void hideProgressDialog() {
        if (progress != null && progress.isShowing()) {
            progress.dismiss();
            progress = null;
        }
    }

    /**
     * Used for setting title of Toolbar
     *
     * @param title String you want to display as title
     */
    public void setActionBarTitle(String title) {
        if (getSupportActionBar() != null && getTitle() != null) {
            setTitle(title);
        }
    }

    protected void setActionBarTitle(int title) {
        setActionBarTitle(getResources().getString(title));
    }

    /**
     * @return Returns toolbar linked with current activity
     */
    public Toolbar getToolbar() {
        return toolbar;
    }

    /**
     * Calls {@code setActionBarTitle()} to set Toolbar title
     *
     * @param title String you want to set as title
     */
    @Override
    public void setToolbarTitle(String title) {
        setActionBarTitle(title);
    }

    @Override
    public Class getPassCodeClass() {
        return PassCodeActivity.class;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LanguageHelper.onAttach(base));
    }

    /**
     * Replace Fragment in FrameLayout Container.
     *
     * @param fragment       Fragment
     * @param addToBackStack Add to BackStack
     * @param containerId    Container Id
     */
    public void replaceFragment(Fragment fragment, boolean addToBackStack, int containerId) {
        invalidateOptionsMenu();
        String backStateName = fragment.getClass().getName();
        boolean fragmentPopped = getSupportFragmentManager().popBackStackImmediate(backStateName,
                0);

        if (!fragmentPopped && getSupportFragmentManager().findFragmentByTag(backStateName) ==
                null) {
            Fragment previousFragment = getSupportFragmentManager().findFragmentById(containerId);
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transition(previousFragment, fragment);
            transaction.replace(containerId, fragment, backStateName);
            if (addToBackStack) {
                transaction.addToBackStack(backStateName);
            }
            transaction.commitAllowingStateLoss();
        }
    }

    public void transition(Fragment previousFragment, Fragment nextFragment) {
        if (previousFragment != null) {
            Fade exitFade = new Fade();
            exitFade.setDuration(android.R.integer.config_shortAnimTime);
            previousFragment.setExitTransition(exitFade);
        }

        Fade enterFade = new Fade();
        enterFade.setDuration(android.R.integer.config_mediumAnimTime);
        nextFragment.setEnterTransition(enterFade);
    }

    /**
     * It pops all the fragments which are currently in the backStack
     */
    public void clearFragmentBackStack() {
        FragmentManager fm = getSupportFragmentManager();
        int backStackCount = getSupportFragmentManager().getBackStackEntryCount();
        for (int i = 0; i < backStackCount; i++) {
            int backStackId = getSupportFragmentManager().getBackStackEntryAt(i).getId();
            fm.popBackStack(backStackId, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }
    }

    public int stackCount() {
        return getSupportFragmentManager().getBackStackEntryCount();
    }


    public static void hideKeyboard(Context context) {
        Activity activity = (Activity) context;
        InputMethodManager inputMethodManager =
                (InputMethodManager) context.getSystemService(
                        Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(
                activity.getCurrentFocus().getWindowToken(), 0);
    }
}
