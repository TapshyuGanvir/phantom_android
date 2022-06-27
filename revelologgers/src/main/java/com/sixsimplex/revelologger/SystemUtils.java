package com.sixsimplex.revelologger;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SystemUtils {

    /**
     * Logs error and returns it bundled in the response JSON
     *
     * @param message
     * @return
     */
    public static JSONObject logAndReturnErrorMessage(String message, Exception exception) {

        JSONObject responseJSON = new JSONObject();
        try {
            responseJSON.put("status", "failure");

            String augmentedMessage = message;
            if (exception != null) {
                exception.printStackTrace();
                augmentedMessage += "Exception: " + exception.getMessage();
            }
            responseJSON.put("message", augmentedMessage);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        return responseJSON;
    }

    /**
     * @param message
     * @return
     */
    public static JSONObject logAndReturnMessage(String status, String message) {

        JSONObject responseJSON = new JSONObject();
        try {
            responseJSON.put("status", status);
            responseJSON.put("message", message);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        return responseJSON;
    }

    public static JSONObject logAndReturnObject(String status, String message, Object resultObject) {

        JSONObject responseJSON = new JSONObject();
        try {
            responseJSON.put("status", status);
            responseJSON.put("message", message);
            responseJSON.put("result", resultObject);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        return responseJSON;
    }

    public static String getCurrentDateTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault());
        return sdf.format(getDate());
    }

    public static Date getDate() {
        return new Date();
    }

    public static String getCurrentDateTimeMiliSec() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss.SSS", Locale.getDefault());
        return sdf.format(getDate());
    }


    public static void showOkDialogBox(String message, Activity activity) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);


        alertDialogBuilder.setMessage(message).setNeutralButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        try {
            alertDialog.setCancelable(false);
            alertDialog.show();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void showOkDialogBoxWithCallback(String message, Activity activity, final OkDialogBox okDialogBox) {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);

        alertDialogBuilder.setMessage(message).setCancelable(false).setNeutralButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                okDialogBox.onOkClicked(dialog);
            }
        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }


    public interface OkDialogBox {
        void onOkClicked(DialogInterface alertDialog);
    }

}
