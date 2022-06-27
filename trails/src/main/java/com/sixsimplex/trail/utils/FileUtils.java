package com.sixsimplex.trail.utils;

import android.content.Context;

import java.io.File;

public class FileUtils {

    public static File createLogFolder(Context context, String logFolderPath) {
        if(logFolderPath==null||logFolderPath.isEmpty())
            return null;
        return createFile(logFolderPath, true);
    }

    private static File createFile(String path, boolean isFolder) {

        File file = new File(path);
        try {
            if (!file.exists()) {
                if (isFolder) {
                    file.mkdirs();
                } else {
                    file.createNewFile();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return file;
    }
}
