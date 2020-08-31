package org.mifos.mobile.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.net.Uri
import android.util.Log
import android.view.Menu
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import org.mifos.mobile.R
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.DateFormatSymbols
import java.util.*

/**
 * Created by michaelsosnick on 12/12/16.
 */
object Utils {
    fun getMonth(month: Int): String {
        return DateFormatSymbols().months[month - 1]
    }

    @JvmStatic
    fun setToolbarIconColor(context: Context?, menu: Menu, color: Int) {
        for (i in 0 until menu.size()) {
            val drawable = menu.getItem(i).icon
            if (drawable != null) {
                drawable.mutate()
                drawable.setColorFilter(
                        ContextCompat.getColor(context!!, color), PorterDuff.Mode.SRC_IN)
            }
        }
    }

    @JvmStatic
    fun setCircularBackground(colorId: Int, context: Context?): LayerDrawable {
        val color: Drawable = ColorDrawable(ContextCompat.getColor(context!!, colorId))
        val image = ContextCompat.getDrawable(context, R.drawable.circular_background)
        return LayerDrawable(arrayOf(image, color))
    }

    @JvmStatic
    fun getImageUri(context: Context?, bitmap: Bitmap): Uri {
        try {
            val cachePath = File(context?.cacheDir, "images")
            cachePath.mkdirs()
            val stream = FileOutputStream("$cachePath/image.png")
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            stream.close()
        } catch (e: IOException) {
            Log.d(Utils::class.java.name, e.toString())
        }
        val imagePath = File(context?.cacheDir, "images")
        val newFile = File(imagePath, "image.png")
        return FileProvider.getUriForFile(context!!, "org.mifos.mobilebanking.fileprovider",
                newFile)
    }

    fun generateFormString(data: Array<Array<String?>>): String {
        val formString = StringBuilder()
        formString.setLength(0)
        for (aData in data) {
            formString.append(aData[0]).append(" : ").append(aData[1]).append('\n')
        }
        return formString.toString()
    }

    @kotlin.jvm.JvmStatic
    fun formatTransactionType(type: String?): String? {
        val builder = StringBuilder()
        for (str in type?.toLowerCase(Locale.ROOT)?.split("_".toRegex())?.toTypedArray()!!) {
            builder.append(str[0].toString().toUpperCase(Locale.ROOT) + str.substring(1,
                    str.length) + " ")
        }
        return builder.toString()
    }
}