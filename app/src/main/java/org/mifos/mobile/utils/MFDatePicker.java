/*
 * This project is licensed under the open source MPL V2.
 * See https://github.com/openMF/android-client/blob/master/LICENSE.md
 */

package org.mifos.mobile.utils;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.widget.DatePicker;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by ishankhanna on 30/06/14.
 */
public class MFDatePicker extends DialogFragment implements DatePickerDialog.OnDateSetListener {

    public static final String TAG = "MFDatePicker";
    static String dateSet;
    static Calendar mCalendar;
    private String startDateString;
    private long startDate;
    private int datePickerType = ALL_DAYS;

    /* Constants used to select which type of date picker is being called */
    public static final int PREVIOUS_DAYS = 1; // only past days
    public static final int FUTURE_DAYS = 2; // only future days
    public static final int ALL_DAYS = 3;  // any day can be picked


    static {

        mCalendar = Calendar.getInstance();
        dateSet = new StringBuilder()
                .append(mCalendar.get(Calendar.DAY_OF_MONTH) < 10 ?
                        ("0" + mCalendar.get(Calendar.DAY_OF_MONTH))
                        : mCalendar.get(Calendar.DAY_OF_MONTH))
                .append("-")
                .append(mCalendar.get(Calendar.MONTH) + 1 < 10 ?
                        ("0" + (mCalendar.get(Calendar.MONTH) + 1))
                        : mCalendar.get(Calendar.MONTH) + 1)
                .append("-")
                .append(mCalendar.get(Calendar.YEAR))
                .toString();
    }

    OnDatePickListener onDatePickListener;

    public MFDatePicker() {

    }

    public static MFDatePicker newInstance(Fragment fragment, int datePickerType, boolean active) {
        MFDatePicker mfDatePicker = new MFDatePicker();

        Bundle args = new Bundle();
        args.putInt(Constants.DATE_PICKER_TYPE, datePickerType);

        mfDatePicker.setArguments(args);

        mfDatePicker.onDatePickListener = (OnDatePickListener) fragment;

        if (!active) {
            mCalendar = Calendar.getInstance();
        }
        return mfDatePicker;
    }

    public static String getDatePickedAsString() {
        return dateSet;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        ThemeHelper themeHelper = new ThemeHelper(getContext());
        DatePickerDialog dialog = new DatePickerDialog(getActivity(),
                themeHelper.getDatePickerStyle(),
                this,
                mCalendar.get(Calendar.YEAR),
                mCalendar.get(Calendar.MONTH),
                mCalendar.get(Calendar.DAY_OF_MONTH));

        Bundle args = getArguments();
        this.datePickerType = args.getInt(Constants.DATE_PICKER_TYPE, this.ALL_DAYS);

        switch (datePickerType) {
            case FUTURE_DAYS:
                dialog.getDatePicker().setMinDate(new Date().getTime());
                break;

            case PREVIOUS_DAYS:
                dialog.getDatePicker().setMaxDate(new Date().getTime());
                break;
        }
        return dialog;
    }

    @Override
    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
        //TODO Fix Single digit problem that fails with the locale
        Calendar calendar = mCalendar;
        calendar.set(year, month, day);
        Date date = calendar.getTime();
        onDatePickListener.onDatePicked(startDateString = DateFormat.
                format("dd-MM-yyyy", date).toString());
        startDate = DateHelper.getDateAsLongFromString(startDateString, "dd-MM-yyyy");
    }

    public void setOnDatePickListener(OnDatePickListener onDatePickListener) {
        this.onDatePickListener = onDatePickListener;
    }

    public interface OnDatePickListener {
        public void onDatePicked(String date);
    }

}
