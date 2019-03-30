package com.tarcrsd.letsgo.Module;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateFormatterModule {

    public static String getDate(Date date) {
        SimpleDateFormat fm = new SimpleDateFormat("dd MMM E");
        return fm.format(date);
    }

    public static String getTime(Date time) {
        SimpleDateFormat fm = new SimpleDateFormat("hh:mm a");
        return fm.format(time);
    }
}
