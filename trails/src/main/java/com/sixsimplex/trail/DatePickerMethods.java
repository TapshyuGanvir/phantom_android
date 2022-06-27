package com.sixsimplex.trail;

import android.app.DatePickerDialog;
import android.content.Context;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

public class DatePickerMethods {

    public static void datePicker(final Context context, Long startDate, Long endDate, GetDate getDate) {

        Calendar c = Calendar.getInstance();
        final int[] mYear = {c.get(Calendar.YEAR)};
        final int[] mMonth = {c.get(Calendar.MONTH)};
        final int[] mDay = {c.get(Calendar.DAY_OF_MONTH)};

        DatePickerDialog datePickerDialog = new DatePickerDialog(context, (view, year, monthOfYear, dayOfMonth) -> {

            mYear[0] = year;
            mMonth[0] = monthOfYear;
            mDay[0] = dayOfMonth;

            String date = "";
            if (view.isShown()) {
                if (mDay[0] <= 9 && (mMonth[0] + 1) <= 9) {
                    //  date = "0" + mDay[0] + "-0" + (mMonth[0] + 1) + "-" + mYear[0];
                    date = "0" + (mMonth[0] + 1) + "-0" + mDay[0] + "-" + mYear[0];//MM-dd-yyyy
                    //editTextDate.setText("0" + mDay[0] + "-0" + (mMonth[0] + 1) + "-" + mYear[0]);//dd-MM-yyyy
                    //editTextDate.setText(mYear[0] + "-0" + (mMonth[0] + 1) + "-0" + mDay[0]);//yyyy-MM-dd
                }
                else if (mDay[0] <= 9) {
                    //date = "0" + mDay[0] + "-" + (mMonth[0] + 1) + "-" + mYear[0];
                    date = (mMonth[0] + 1) + "-" + "0" + mDay[0] + "-" + mYear[0];//MM-dd-yyyy
                    //editTextDate.setText("0" + mDay[0] + "-" + (mMonth[0] + 1) + "-" + mYear[0]);
                    //editTextDate.setText(mYear[0] + "-" + (mMonth[0] + 1) + "-0" + mDay[0]);
                }
                else if ((mMonth[0] + 1) <= 9) {
                    //date = mDay[0] + "-0" + (mMonth[0] + 1) + "-" + mYear[0];
                    date = "0" + (mMonth[0] + 1) + "-" + mDay[0] + "-" + mYear[0];//MM-dd-yyyy
                    //editTextDate.setText(mDay[0] + "-0" + (mMonth[0] + 1) + "-" + mYear[0]);
                    // editTextDate.setText(mYear[0] + "-0" + (mMonth[0] + 1) + "-" + mDay[0]);
                }
                else {
                    //date = mDay[0] + "-" + (mMonth[0] + 1) + "-" + mYear[0];
                    date = (mMonth[0] + 1) + "-" + mDay[0] + "-" + mYear[0];//MM-dd-yyyy
                    //editTextDate.setText(mDay[0] + "-" + (mMonth[0] + 1) + "-" + mYear[0]);
                    //  editTextDate.setText(mYear[0] + "-" + (mMonth[0] + 1) + "-" + mDay[0]);
                }
            }
            getDate.getSelectedDate(date);
        }, mYear[0], mMonth[0], mDay[0]);
        datePickerDialog.show();

        if (startDate != null) {
            datePickerDialog.getDatePicker().setMinDate(startDate);
        }

        if (endDate != null) {
            datePickerDialog.getDatePicker().setMaxDate(endDate);
        }
    }

    public static Date convertStringToDate2(String dateString) {
        try {
            SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.ENGLISH);
            return formatter.parse(dateString);
//            return getRuntimeDateFormatter().parse(dateString);
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Date convertStringDateToDate(String dateString) {
        try {
            SimpleDateFormat formatter = new SimpleDateFormat("MM-dd-yyyy", Locale.ENGLISH);
            return formatter.parse(dateString);
//            return getRuntimeDateFormatter().parse(dateString);
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getValidDateTimeStamp(Object valueObject, AppPreferences appPreferences) {

        String value = "";
        try {
            if (valueObject instanceof GregorianCalendar) {

                GregorianCalendar gregorianCalendar = (GregorianCalendar) valueObject;
                Date date = gregorianCalendar.getTime();
                value = convertDateToStringDateTimeStamp(date, appPreferences);

            }
            else if (valueObject instanceof Long) {
                value = convertLongDateIntoStringDateTimestamp((long) valueObject, appPreferences);
            }
            else if (valueObject instanceof String) {
                value = convertStringDateToProperString((String) valueObject, appPreferences);
            }
            else {
                value = convertToValidDateString((String) valueObject, false, appPreferences);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return value;

    }

    public static String convertDateToStringDateTimeStamp(Date date, AppPreferences appPreferences) {
        return getRuntimeTimestampFormatter(appPreferences).format(date);
        /*Format formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.ENGLISH);
        return formatter.format(date);*/
    }

    public static String convertLongDateIntoStringDateTimestamp(Long longDate, AppPreferences appPreferences) {
        if (longDate != null) {
            //return android.text.format.DateFormat.format("dd-MM-yyyy HH:mm:ss", new Date(longDate)).toString();
            return getRuntimeTimestampFormatter(appPreferences).format(new Date(longDate));
        }
        else {
            return null;
        }
    }

    public static String convertStringDateToProperString(String date, AppPreferences appPreferences) {
        //SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault());
        return getRuntimeTimestampFormatter(appPreferences).format(date);
    }

  /*  public static SimpleDateFormat getRuntimeTimestampFormatter(){
        String runtimetimestampFormat = UserInfoPreferenceUtility.getTimestampFormat();
        if(runtimetimestampFormat.isEmpty()){
            runtimetimestampFormat = "yyyy-MM-dd";
        }
        SimpleDateFormat sdf = new SimpleDateFormat(runtimetimestampFormat, Locale.ENGLISH);
        return sdf;
    }

    public static SimpleDateFormat getRuntimeDateFormatter(){
        String runtimetimestampFormat = UserInfoPreferenceUtility.getDateFormat();
        if(runtimetimestampFormat.isEmpty()){
            runtimetimestampFormat = "E MMM dd HH:mm:ss zzz yyyy";
        }
        SimpleDateFormat sdf = new SimpleDateFormat(runtimetimestampFormat, Locale.ENGLISH);
        return sdf;
    }*/

    public static String convertToValidDateString(String dateInAnyFormat, boolean isYearFirst, AppPreferences appPreferences) {

        Date simpleDateFormat = null;

        if (dateInAnyFormat != null && ! dateInAnyFormat.equalsIgnoreCase("") && ! dateInAnyFormat.equalsIgnoreCase("null")) {

            if (dateInAnyFormat.contains("IST")) {
                dateInAnyFormat = dateInAnyFormat.replace("IST", "GMT");
            }

            try {
                if (dateInAnyFormat.matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{3}+\\d{4}")) {
                    //"yyyy-MM-ddThh:mm:ss.000+0000: 2010-06-14T18:30:00.000+0000"

                    dateInAnyFormat = dateInAnyFormat.replace("T", " ");
                    //"2010-06-14T18:30:00.000+0000"

                    if (dateInAnyFormat.endsWith(".000+0000")) {
                        dateInAnyFormat = dateInAnyFormat.replace(".000+0000", "");
                    }
                    simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(dateInAnyFormat);

                }
                else if (dateInAnyFormat.matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}Z")) {
                    //"yyyy-MM-ddThh:mm:ss.000+0000: 2010-06-14T18:30:00"

                    dateInAnyFormat = dateInAnyFormat.replace("T", " ");
                    //"2010-06-14T18:30:00Z"

                    if (dateInAnyFormat.endsWith(".000+0000")) {
                        dateInAnyFormat = dateInAnyFormat.replace(".000+0000", "");
                    }
                    simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(dateInAnyFormat);

                }
                else if (dateInAnyFormat.matches("\\d{4}-\\d{2}-\\d{2}([A-Z])\\d{2}:\\d{2}:\\d{2}.\\d{3}([A-Z])")) {
                    //"yyyy-MM-dd([A-Z])hh:mm:ss.000([A-Z]): 1968-12-31T18:30:00.000Z"

                    simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).parse(dateInAnyFormat);

                }
                else if (dateInAnyFormat.matches("\\w{3} \\w{3} \\d{2} \\d{2}:\\d{2}:\\d{2} \\w{3} \\d{4}") || dateInAnyFormat.matches(
                        "\\w{3} \\w{3} \\d{1} \\d{2}:\\d{2}:\\d{2} \\w{3} \\d{4}")) {
                    //Thu Dec 01 15:30:03 GMT 2016
                    simpleDateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.getDefault()).parse(dateInAnyFormat);

                }
                else if (dateInAnyFormat.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}") || dateInAnyFormat.matches(
                        "\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}.\\d{1}") || dateInAnyFormat.matches(
                        "\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}.\\d{2}")) {
                    //yyyy-MM-dd HH:mm:ss: 2010-06-14 10:08:26
                    simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(dateInAnyFormat);

                }
                else if (dateInAnyFormat.matches("\\d{4}-\\d{2}-\\d{2}Z")) {
                    //yyyy-dd-MMZ 1969-12-31Z
                    dateInAnyFormat = dateInAnyFormat.replace("Z", "");
                    simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateInAnyFormat);

                }
                else if (dateInAnyFormat.matches("\\d{2}-\\d{2}-\\d{4}")) {
                    //dd-MM-yyyy 14-06-2010
                    simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).parse(dateInAnyFormat);

                }
                else if (dateInAnyFormat.matches("\\d{4}-\\d{2}-\\d{2}") || dateInAnyFormat.matches(
                        "\\d{4}-\\d{2}-\\d{1}") || dateInAnyFormat.matches("\\d{4}-\\d{1}-\\d{2}") || dateInAnyFormat.matches(
                        "\\d{4}-\\d{1}-\\d{1}")) {
                    //yyyy-MM-dd: 2010-06-14
                    simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateInAnyFormat);

                }
                else if (dateInAnyFormat.matches("\\w{3} \\w{3} \\d{2} \\d{2}:\\d{2}:\\d{2} \\w{3} \\d{4}") || dateInAnyFormat.matches(
                        "\\d{4}-\\d{2}-\\d{1}")) {
                    //Mon Nov 28 00:00:00 GMT 2016
                    simpleDateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.getDefault()).parse(dateInAnyFormat);

                }
                else if (dateInAnyFormat.matches("\\d{4}-\\d{2}-\\d{2}")) {
                    //yyyy-MM-dd 2010-06-14
                    simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateInAnyFormat);
                }
                else if (dateInAnyFormat.matches("\\d{2}-\\d{2}-\\d{4} \\d{2}:\\d{2}:\\d{2}")) {
                    //dd-MM-yyyy HH:mm:ss 21-02-2020 10:08:26
                    simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault()).parse(dateInAnyFormat);
                }

                DateFormat dateFormat;

                if (isYearFirst) {
                    dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                }
                else {
                    dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                }

                /*if (simpleDateFormat != null) {
                    return dateFormat.format(simpleDateFormat);
                } else {
                    return null;
                }*/
                String validDateString = null;
                if (simpleDateFormat != null) {
                    validDateString = getRuntimeTimestampFormatter(appPreferences).format(simpleDateFormat);
                }
                else {
                    validDateString = getRuntimeTimestampFormatter(appPreferences).format(dateInAnyFormat);
                }
                return validDateString;
            }
            catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
        else {
            return null;
        }
    }

    public static SimpleDateFormat getRuntimeTimestampFormatter(AppPreferences appPreferences) {
        String runtimetimestampFormat = appPreferences.getString("RUNTIMESTAMPFORMAT", "");
        if (runtimetimestampFormat.isEmpty()) {
            runtimetimestampFormat = "yyyy-MM-dd HH:mm:ss";
        }
        // runtimetimestampFormat = "MM-dd-yyyy HH:mm:ss";
        SimpleDateFormat sdf = new SimpleDateFormat(runtimetimestampFormat, Locale.ENGLISH);
        return sdf;
    }

    public static String getCurrentDateString(AppPreferences appPreferences) {
        //  SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault());
        //  SimpleDateFormat sdf = new SimpleDateFormat("E MMM dd HH:mm:ss zzz yyyy", Locale.getDefault());
        return getRuntimeTimestampFormatter(appPreferences).format(getCurrentDate());
    }

    public static Date getCurrentDate() {
        return new Date();
    }

    public static String getCurrentDateString_metadata() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        // SimpleDateFormat sdf = new SimpleDateFormat("E MMM dd HH:mm:ss zzz yyyy", Locale.getDefault());
        return sdf.format(getCurrentDate());
    }

    public static String getCurrentDateString_mmddyyyyHHmmss() {
        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss", Locale.getDefault());
        // SimpleDateFormat sdf = new SimpleDateFormat("E MMM dd HH:mm:ss zzz yyyy", Locale.getDefault());
        return sdf.format(getCurrentDate());
    }

    public static String getCurrentDateString_mmddyyyy() {
        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy", Locale.getDefault());
        // SimpleDateFormat sdf = new SimpleDateFormat("E MMM dd HH:mm:ss zzz yyyy", Locale.getDefault());
        return sdf.format(getCurrentDate());
    }

    public static String getCurrentDate_TimeStampString_yyyymmddhhmmss() {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            String str_date = sdf.format(getCurrentDate());
            return str_date;
            /*Date date = (Date) sdf.parse(str_date);
            java.sql.Timestamp timeStampDate = new Timestamp(date.getTime());
            return timeStampDate.toString();*/
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String getCurrentDate_DateString_yyyymmddhhmmss() {
        try {
            Date date = getCurrentDate();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String str_date = sdf.format(date);
           /* Date dateonly = (Date) sdf.parse(str_date);
            java.sql.Timestamp timeStampDate = new Timestamp(dateonly.getTime());
            return timeStampDate.toString();*/
            return str_date;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String getDefaultDateString(AppPreferences appPreferences) {

        return getRuntimeTimestampFormatter(appPreferences).format("01-01-1990 00:00:00");
//        return "01-01-1990 00:00:00";
    }

    public interface GetDate {
        void getSelectedDate(String date);
    }
}
