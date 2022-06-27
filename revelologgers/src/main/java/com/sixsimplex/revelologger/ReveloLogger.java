package com.sixsimplex.revelologger;
import android.content.Context;
import android.util.Log;


import com.sixsimplex.revelologger.FileUtils;
import com.sixsimplex.revelologger.SystemUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class ReveloLogger {

    private static final String className = "ReveloLogger";
    public static long startTime = System.nanoTime();
    private static Context reveloLoggerContext = null;
    private static String trailLogFolderPath = null;
    private static String trailUserName = "trailUser";
    private static String trailUserSurveyName = "trailUserSurveyName";
    private static String trailDeviceInfo = "No device info found for trailUser";
    private static final String TAG = "ReveloTrails";
    private static ReveloLogger INSTANCE = null;

    // other instance variables can be here

    private ReveloLogger() {
    }

    public static ReveloLogger getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ReveloLogger();
        }
        return (INSTANCE);
    }

    public void initialize(Context context, String userName, String surveyName, String logFolderPath, String deviceInfo) {
        reveloLoggerContext = context;
        startTime = System.nanoTime();
        trailLogFolderPath = logFolderPath;
        trailUserName = userName;
        trailDeviceInfo = deviceInfo;
        trailUserSurveyName = surveyName;
    }

    public void deleteLogFolder(Context context) {
        try {

            File logFolder = FileUtils.createLogFolder(context, trailLogFolderPath);

            if (logFolder != null && logFolder.exists()) {//check log folder is exist or not
                File[] logFiles = logFolder.listFiles();//get all log files
                if (logFiles != null) {
                    for (File logFile : logFiles) {
                        if (logFile != null) {
                            logFile.delete();//delete all log files
                        }
                    }
                }
            }

        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void trace(String resource, String operation, String message) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss.SSSZ", Locale.getDefault());
            String logDateTime = sdf.format(new Date());

            /*new Thread(new Runnable() {
                @Override
                public void run() {*/
            try {
                // addLog("TRACE", resource, operation, message);
                //insertLogEntry(reveloLoggerContext,"TRACE", resource, operation, message,logDateTime);
                Log.v(TAG, "Resource :" + resource + " .. " + "Operation :" + operation + " .. " + "Message :" + message);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
          /*      }
            }).start();*/

        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void warn(String resource, String operation, String message) {

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss.SSSZ", Locale.getDefault());
            String logDateTime = sdf.format(new Date());

          /*  new Thread(new Runnable() {
                @Override
                public void run() {*/
            try {
                //addLog("WARN", resource, operation, message);
                //insertLogEntry(reveloLoggerContext,"WARN", resource, operation, message,logDateTime);
                Log.w(TAG, "Resource :" + resource + " .. " + "Operation :" + operation + " .. " + "Message :" + message);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
           /*     }
            }).start();*/
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void error(String resource, String operation, String message) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss.SSSZ", Locale.getDefault());
            String logDateTime = sdf.format(new Date());

            /*new Thread(new Runnable() {
                @Override
                public void run() {*/
            try {
                // addLog("ERROR", resource, operation, message, logDateTime);
                //insertLogEntry(reveloLoggerContext,"ERROR", resource, operation, message,logDateTime);
                Log.e(TAG, "Resource :" + resource + " .. " + "Operation :" + operation + " .. " + "Message :" + message);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
              /*  }
            }).start();*/
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void addLog(String level, String resource, String operation, String message, String logDateTime) {
        if (reveloLoggerContext != null) {
            File logFile = getLogFile(reveloLoggerContext);

            if (logFile == null || ! logFile.exists()) {
                try {
                    if (trailLogFolderPath != null) {
                        File logFolder = FileUtils.createLogFolder(reveloLoggerContext, trailLogFolderPath);
                        if (logFolder != null && ! logFolder.exists()) {
                            boolean logFolderIsCreate = logFolder.mkdirs();
                            if (logFolderIsCreate) { //create log file
                                createLogFile(reveloLoggerContext, trailUserName, logFolder.getAbsolutePath());
                            }
                        }
                    }
                    logFile = getLogFile(reveloLoggerContext);
                    if (logFile == null || ! logFile.exists()) {
                        return;
                    }
                }
                catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            try {
                //BufferedWriter for performance, true to set append to file flag
                BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));


                JSONObject logJsonObject = new JSONObject();
                logJsonObject.put("time", logDateTime);
                logJsonObject.put("level", level);
                logJsonObject.put("userName", trailUserName);
                logJsonObject.put("resource", resource);
                logJsonObject.put("operation", operation);
                logJsonObject.put("projectName", trailUserSurveyName);
                logJsonObject.put("sourceAppName", "mobile");
                message = trailDeviceInfo + "\n" + message;
                logJsonObject.put("message", message);

                JSONObject fileJsonObject = new JSONObject();
                fileJsonObject.put("logFilePath", logFile.getAbsolutePath());
                fileJsonObject.put("logJsonObject", logJsonObject);
                buf.append(fileJsonObject.toString());
                buf.newLine();
                buf.close();
            }
            catch (IOException | JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }


    }

    public File getLogFile(Context context) {

        File logFile = null;

        if (trailLogFolderPath == null) {
            return null;
        }

        File logFolder = new File(trailLogFolderPath);

        if (logFolder.exists()) {
            File[] logFileNames = logFolder.listFiles();
            if (logFileNames != null) {
                if (logFileNames.length != 0) {
                    logFile = logFileNames[0];
                }
            }//add else for 'if log file is manually deleted, it wont be created again!'
        }
        return logFile;
    }

    private void createLogFile(Context context, String userName, String logFolderPath) {

        try {

            Date currentDate = Calendar.getInstance().getTime();
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy_hh_mm_ss", Locale.getDefault());
            String currentDateString = dateFormat.format(currentDate);

            String logFileName = "Revelo_" + userName + "_" + currentDateString + ".json";

            String logFilePath = logFolderPath + File.separator + logFileName;
            File logFile = new File(logFilePath);

            boolean isCreate = logFile.createNewFile();

            if (isCreate) {
                debug(className, "file creation", "Log file created at " + SystemUtils.getCurrentDateTime());
                timeLog(className, "file creation", "Log file created at " + SystemUtils.getCurrentDateTime());
                info(className, "System Info of " + userName, trailDeviceInfo);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void debug(String resource, String operation, String message) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss.SSSZ", Locale.getDefault());
            String logDateTime = sdf.format(new Date());

            /*new Thread(new Runnable() {
                @Override
                public void run() {*/
            try {
                //  addLog("DEBUG", resource, operation, message);
                //insertLogEntry(reveloLoggerContext,"DEBUG", resource, operation, message,logDateTime);
                Log.d(TAG, "Resource :" + resource + " .. " + "Operation :" + operation + " .. " + "Message :" + message);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
           /*     }
            }).start();*/
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void timeLog(String resource, String operation, String message) {

        try {
            //addLog("TRACE", resource, operation, message);
            Log.w(TAG,
                  "Resource :" + resource + " .. " + "Operation :" + operation + " .. " + "TimeTAKEN :" + ((System.nanoTime() - startTime) / 1000000) + "ms .. " + "Message :" + message);
            startTime = System.nanoTime();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void info(String resource, String operation, String message) {

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss.SSSZ", Locale.getDefault());
            String logDateTime = sdf.format(new Date());
            /*new Thread(new Runnable() {
                @Override
                public void run() {*/
            try {
                // addLog("INFO", resource, operation, message);
                //insertLogEntry(reveloLoggerContext,"INFO", resource, operation, message,logDateTime);
                Log.i(TAG, "Resource :" + resource + " .. " + "Operation :" + operation + " .. " + "Message :" + message);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
           /*     }
            }).start();*/
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void addLog(String level, String resource, String operation, String message) {
        if (reveloLoggerContext != null) {
            File logFile = getLogFile(reveloLoggerContext);

            if (logFile == null || ! logFile.exists()) {
                try {
                    if (trailLogFolderPath != null) {
                        File logFolder = FileUtils.createLogFolder(reveloLoggerContext, trailLogFolderPath);
                        if (logFolder != null && ! logFolder.exists()) {
                            boolean logFolderIsCreate = logFolder.mkdirs();
                            if (logFolderIsCreate) { //create log file
                                createLogFile(reveloLoggerContext, trailUserName, logFolder.getAbsolutePath());
                            }
                        }
                    }
                    logFile = getLogFile(reveloLoggerContext);
                    if (logFile == null || ! logFile.exists()) {
                        return;
                    }
                }
                catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            try {
                //BufferedWriter for performance, true to set append to file flag
                BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss.SSSZ", Locale.getDefault());
                String logDateTime = sdf.format(new Date());

                JSONObject logJsonObject = new JSONObject();
                logJsonObject.put("time", logDateTime);
                logJsonObject.put("level", level);
                logJsonObject.put("userName", trailUserName);
                logJsonObject.put("resource", resource);
                logJsonObject.put("operation", operation);
                logJsonObject.put("projectName", trailUserSurveyName);
                logJsonObject.put("sourceAppName", "mobile");
                message = trailDeviceInfo + "\n" + message;
                logJsonObject.put("message", message);

                JSONObject fileJsonObject = new JSONObject();
                fileJsonObject.put("logFilePath", logFile.getAbsolutePath());
                fileJsonObject.put("logJsonObject", logJsonObject);
                buf.append(fileJsonObject.toString());
                buf.newLine();
                buf.close();
            }
            catch (IOException | JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }


    }
}
