/*
 * This project is licensed under the open source MPL V2.
 * See https://github.com/openMF/android-client/blob/master/LICENSE.md
 */
package org.mifos.mobile.utils

import android.util.Log

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by ishankhanna on 30/05/14.
 *
 * This is a helper class that will be used to convert List<Interger> Type Dates
 * from MifosX into Simple Strings or Date Formats</Interger>
 */
object DateHelper {

    val LOG_TAG: String? = DateHelper::class.java.simpleName
    const val FORMAT_dd_MMMM_yyyy = "dd MMMM yyyy"
    fun getCurrentDate(dateFormat: String?, separator: String): List<Int> {
        val date: MutableList<Int> = ArrayList()
        val s = SimpleDateFormat(dateFormat, Locale.getDefault()).format(Date())
        for (str in s.split(separator.toRegex()).toTypedArray()) {
            date.add(str.toInt())
        }
        return date
    }

    /**
     * the result string uses the list given in a reverse order ([x, y, z] results in "z y x")
     *
     * @param integersOfDate [year-month-day] (ex [2016, 4, 14])
     * @return date in the format day month year (ex 14 Apr 2016)
     */
    fun getDateAsString(integersOfDate: List<Int?>?): String {
        val stringBuilder = StringBuilder()
        if (integersOfDate != null) {
            stringBuilder.append(integersOfDate[2])
                    .append(' ')
                    .append(getMonthName(integersOfDate[1]))
                    .append(' ')
                    .append(integersOfDate[0])
        }
        return stringBuilder.toString()
    }

    fun getDateAsString(integersOfDate: List<Int>?, pattern: String?): String {
        return getFormatConverter("dd MMM yyyy",
                pattern, getDateAsString(integersOfDate))
    }

    /**
     * This Method converting the dd-MM-yyyy format type date string into dd MMMM yyyy
     *
     * @param format     Final Format of date string
     * @param dateString date string
     * @return dd MMMM yyyy format date string.
     */
    fun getSpecificFormat(format: String?, dateString: String?): String {
        val pickerFormat = SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH)
        val finalFormat = SimpleDateFormat(format, Locale.ENGLISH)
        var date: Date? = null
        try {
            date = pickerFormat.parse(dateString)
        } catch (e: ParseException) {
            Log.d(LOG_TAG, e.localizedMessage)
        }
        return finalFormat.format(date)
    }

    fun getFormatConverter(
            currentFormat: String?, requiredFormat: String?,
            dateString: String?
    ): String {
        val pickerFormat = SimpleDateFormat(currentFormat, Locale.ENGLISH)
        val finalFormat = SimpleDateFormat(requiredFormat, Locale.ENGLISH)
        var date: Date? = null
        try {
            date = pickerFormat.parse(dateString)
        } catch (e: ParseException) {
            Log.d(LOG_TAG, e.localizedMessage)
        }
        return finalFormat.format(date)
    }

    /**
     * @param month an integer from 1 to 12
     * @return string representation of the month like Jan or Feb..etc
     */
    private fun getMonthName(month: Int?): String? {
        var monthName = ""
        when (month) {
            1 -> monthName = "Jan"
            2 -> monthName = "Feb"
            3 -> monthName = "Mar"
            4 -> monthName = "Apr"
            5 -> monthName = "May"
            6 -> monthName = "Jun"
            7 -> monthName = "Jul"
            8 -> monthName = "Aug"
            9 -> monthName = "Sep"
            10 -> monthName = "Oct"
            11 -> monthName = "Nov"
            12 -> monthName = "Dec"
        }
        return monthName
    }

    fun getDateAsLongFromString(dateStr: String?, pattern: String?): Long? {
        val sdf = SimpleDateFormat(pattern)
        var date: Date? = null
        try {
            date = sdf.parse(dateStr)
        } catch (e: ParseException) {
            Log.d("TAG", e.message)
        }
        return date?.time
    }

    @kotlin.jvm.JvmStatic
    fun getDateAsLongFromList(integersOfDate: List<Int>?): Long? {
        val dateStr = getDateAsString(integersOfDate)
        return getDateAsLongFromString(dateStr, "dd MMM yyyy")
    }

    fun subtractWeeks(number: Int): Long {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.WEEK_OF_YEAR, -number)
        return calendar.timeInMillis
    }

    fun subtractMonths(number: Int): Long {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MONTH, -number)
        return calendar.timeInMillis
    }

    fun getDateAsStringFromLong(timeInMillis: Long?): String {
        val sdf = SimpleDateFormat("dd MMM yyyy")
        return sdf.format(timeInMillis?.let { Date(it) })
    }

    @kotlin.jvm.JvmStatic
    fun getDateAndTimeAsStringFromLong(timeInMillis: Long?): String {
        val sdf = SimpleDateFormat("HH:mm a dd MMM yyyy")
        return sdf.format(timeInMillis?.let { Date(it) })
    }

    fun equals(dateOne: List<Int>, dateTwo: List<Int>): Boolean {
        return dateOne[0] == dateTwo[0] && dateOne[1] == dateTwo[1] && dateOne[2] == dateTwo[2]
    }
}