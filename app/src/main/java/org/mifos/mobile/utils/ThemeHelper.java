package org.mifos.mobile.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Build;
import android.widget.RadioButton;

import androidx.annotation.ColorRes;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import org.mifos.mobile.R;
import org.mifos.mobile.injection.ActivityContext;

import javax.inject.Inject;

/**
 * Created by dnld on 02/08/16.
 */
public class ThemeHelper {

    public static final String DARK_THEME = "DARK_THEME";
    public static final String LIGHT_THEME = "LIGHT_THEME";

    private SharedPreferences prefs;
    private Context context;

    private String baseTheme;
    private int primaryColor;
    private int primaryDarkColor;
    private int accentColor;

    @Inject
    public ThemeHelper(@ActivityContext Context context) {
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
        this.context = context;
        updateTheme();
    }

    public void updateTheme() {
        baseTheme = prefs.getString(context.getString(R.string.themes_key), LIGHT_THEME);
        switch (baseTheme) {
            case LIGHT_THEME:
                this.primaryColor = getColor(R.color.primary);
                this.primaryDarkColor = getColor(R.color.primary_dark);
                this.accentColor = getColor(R.color.colorAccent);
                break;
            case DARK_THEME:
                this.primaryColor = getColor(R.color.dark_primary);
                this.primaryDarkColor = getColor(R.color.dark_primary_dark);
                this.accentColor = getColor(R.color.dark_accent);
                break;
        }
    }

    public int getPrimaryColor() {
        return primaryColor;
    }

    public int getPrimaryDarkColor() {
        return primaryDarkColor;
    }

    public int getAccentColor() {
        return accentColor;
    }

    public String getBaseTheme() {
        return baseTheme;
    }

    public static String getBaseTheme(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.themes_key), LIGHT_THEME);
    }

    public int getColor(@ColorRes int color) {
        return ContextCompat.getColor(context, color);
    }

    public static int getColor(Context context, @ColorRes int color) {
        return ContextCompat.getColor(context, color);
    }

    public int getBackgroundColor() {
        int color;
        switch (baseTheme) {
            case DARK_THEME:
                color = getColor(R.color.background_dark);
                break;
            case LIGHT_THEME:
            default:
                color = getColor(R.color.background);
        }
        return color;
    }

    public int getInvertedBackgroundColor() {
        int color;
        switch (baseTheme) {
            case DARK_THEME:
                color = getColor(R.color.background);
                break;
            case LIGHT_THEME:
            default:
                color = getColor(R.color.background_dark);
        }
        return color;
    }

    public int getTextColor() {
        int color;
        switch (baseTheme) {
            case DARK_THEME:
                color = getColor(R.color.md_grey_200);
                break;
            case LIGHT_THEME:
            default:
                color = getColor(R.color.md_grey_800);
        }
        return color;
    }

    public int getSubTextColor() {
        int color;
        switch (baseTheme) {
            case DARK_THEME:
                color = getColor(R.color.md_grey_400);
                break;
            case LIGHT_THEME:
            default:
                color = getColor(R.color.md_grey_600);
        }
        return color;
    }

    public int getCardBackgroundColor() {
        int color;
        switch (baseTheme) {
            case DARK_THEME:
                color = getColor(R.color.md_dark_cards);
                break;
            case LIGHT_THEME:
            default:
                color = getColor(R.color.md_light_cards);
        }
        return color;
    }


    public int getDrawerBackground() {
        int color;
        switch (baseTheme) {
            case DARK_THEME:
                color = getColor(R.color.md_dark_cards);
                break;
            case LIGHT_THEME:
            default:
                color = getColor(R.color.md_light_cards);
        }
        return color;
    }

    public int getDialogStyle() {
        int style;
        switch (getBaseTheme()) {
            case DARK_THEME:
                style = R.style.MaterialAlertDialogStyle_Dark;
                break;
            case LIGHT_THEME:
            default:
                style = R.style.MaterialAlertDialogStyle_Light;
                break;
        }
        return style;
    }

    public int getHighlightedItemColor() {
        int color;
        switch (baseTheme) {
            case DARK_THEME:
                color = getColor(R.color.md_grey_600);
                break;
            case LIGHT_THEME:
            default:
                color = getColor(R.color.md_grey_300);
        }
        return color;
    }

    private ColorStateList getRadioButtonColor() {
        return new ColorStateList(
                new int[][]{
                        new int[]{-android.R.attr.state_enabled}, // disabled
                        new int[]{android.R.attr.state_enabled} // enabled
                },
                new int[]{getTextColor(), getAccentColor()});
    }

    public void updateRadioButtonColor(RadioButton radioButton) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            radioButton.setButtonTintList(getRadioButtonColor());
            radioButton.setTextColor(getTextColor());
        }
    }

    public void setRadioTextButtonColor(RadioButton radioButton, int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            radioButton.setButtonTintList(getRadioButtonColor());
            radioButton.setTextColor(color);
        }
    }

    public int getDatePickerStyle() {
        int style;
        switch (getBaseTheme()) {
            case DARK_THEME:
                style = R.style.MaterialDatePickerTheme_Dark;
                break;
            case LIGHT_THEME:
            default:
                style = R.style.MaterialDatePickerTheme_Light;
                break;
        }
        return style;
    }
}
