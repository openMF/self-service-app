package org.mifos.mobile.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import org.mifos.mobile.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormatSymbols;

import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

/**
 * Created by michaelsosnick on 12/12/16.
 */

public class Utils {
    public static String getMonth(int month) {
        return new DateFormatSymbols().getMonths()[month - 1];
    }

    public static void setToolbarIconColor(Context context, Menu menu, int color) {
        for (int i = 0; i < menu.size(); i++) {
            Drawable drawable = menu.getItem(i).getIcon();
            if (drawable != null) {
                drawable.mutate();
                drawable.setColorFilter(
                        ContextCompat.getColor(context, color), PorterDuff.Mode.SRC_IN);
            }
        }
    }

    public static LayerDrawable setCircularBackground(int colorId, Context context) {
        Drawable color = new ColorDrawable(ContextCompat.getColor(context, colorId));
        Drawable image = ContextCompat.getDrawable(context, R.drawable.circular_background);
        LayerDrawable ld = new LayerDrawable(new Drawable[]{image, color});
        return ld;
    }


    public static Uri getImageUri(Context context, Bitmap bitmap) {
        try {

            File cachePath = new File(context.getCacheDir(), "images");
            cachePath.mkdirs();
            FileOutputStream stream = new FileOutputStream(cachePath + "/image.png");
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            stream.close();
        } catch (IOException e) {
            Log.d(Utils.class.getName(), e.toString());
        }

        File imagePath = new File(context.getCacheDir(), "images");
        File newFile = new File(imagePath, "image.png");

        return FileProvider.getUriForFile(context, "org.mifos.mobilebanking.fileprovider",
                newFile);
    }

    public static String generateFormString(String[][] data) {
        StringBuilder formString = new StringBuilder();
        formString.setLength(0);
        for (String[] aData : data) {
            formString.append(aData[0]).append(" : ").append(aData[1]).append('\n');
        }
        return formString.toString();
    }

    public static String formatTransactionType(String type) {
        StringBuilder builder = new StringBuilder();
        for (String str : type.toLowerCase().split("_")) {
            builder.append(String.valueOf(str.charAt(0)).toUpperCase() + str.substring(1,
                    str.length()) + " ");
        }
        return builder.toString();
    }

    public static void hideSoftKeyboard(Activity context) {
        View view = context.getCurrentFocus();
        if (view != null) {
            InputMethodManager inputMethodManager = (InputMethodManager)
                    context.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}

