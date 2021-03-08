package com.napier.sunlightapp;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class UserSettings {
    // User settings:
    private static String userName;
    private static String target;
    private static String notificationsEnabled = "true"; // default

    public static String getUserName() {
        return userName;
    }

    public static String getTarget() {
        return target;
    }

    public static String getNotificationsEnabled() {
        return notificationsEnabled;
    }

    /**
     * Reads from the userSettings file and saves user settings in this class
     */
    public static void loadUserSettings(FileInputStream fis){
        /* Read from a file located in the internal storage directory provided by the system for this and only this app: */

        InputStreamReader inputStreamReader = null;
        // read from the file using InputStreamReader:
        inputStreamReader = new InputStreamReader(fis, StandardCharsets.UTF_8);

        try {
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            // if the file is not empty, get the username and the target:
            if (line != null) {
                String words[] = line.split(",");
                if (!words[0].equals("null")){
                    userName=words[0];
                }
                if (!words[1].equals("null")){
                    target=words[1];
                }
                notificationsEnabled=words[2];
            }
        } catch (Exception e) {
            // Error occurred when opening raw file for reading.
        }
    }
}
