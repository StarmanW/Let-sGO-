package com.tarcrsd.letsgo.Module;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.widget.DatePicker;

import android.support.v4.app.DialogFragment;

import com.tarcrsd.letsgo.CreateEventActivity;

import java.text.DateFormatSymbols;
import java.util.Calendar;

public class DatePickerFragment extends DialogFragment {

    /**
     * Creates the date picker dialog with the current date from Calendar.
     *
     * @param savedInstanceState Saved instance state bundle
     * @return DatePickerDialog     The date picker dialog
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current date as the default date in the picker.
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        // Create a new instance of DatePickerDialog and return it.
        return new DatePickerDialog(getActivity(), (DatePickerDialog.OnDateSetListener) getActivity(), year, month, day);
    }
}
