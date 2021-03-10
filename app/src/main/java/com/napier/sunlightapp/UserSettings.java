package com.napier.sunlightapp;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * A class to retrieve and store user settings.
 */
public class UserSettings {
    // User settings:
    private static String userName;
    private static String target;
    private static String remainingTarget;
    private static String notificationsEnabled = "true"; // default

    /**
     * Gets username.
     * @return username
     */
    public static String getUserName() {
        return userName;
    }

    /**
     * Gets Target
     * @return target
     */
    public static String getTarget() {
        return target;
    }

    /**
     * Gets notification setting
     * @return "true" if notifications are on, "false" if not
     */
    public static String getNotificationsEnabled() {
        return notificationsEnabled;
    }

    /**
     * Gets the remaining target
     * @return remaining target
     */
    public static String getRemainingTarget() { return remainingTarget; }

    /**
     * Reads from the userSettings.csv file in the app's internal storage and saves user settings in this class
     */
    public static void loadUserSettings(FileInputStream fis){
        /* Read from a file located in the internal storage directory provided by the system for this and only this app: */

        InputStreamReader inputStreamReader;
        // read from the file using InputStreamReader:
        inputStreamReader = new InputStreamReader(fis, Charset.defaultCharset());

        try {
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            // if the file is not empty, get the user settings:
            if (line != null) {
                String[] words = line.split(",");
                if (!words[0].equals("null")){
                    userName=words[0];
                }
                if (!words[1].equals("null")){
                    target=words[1];
                }
                if (!words[2].equals("null")){
                    notificationsEnabled=words[2];
                }
                if(!words[3].equals("null")){
                    remainingTarget=words[3];
                }

            }
        } catch (Exception e) {
            // Error occurred when opening raw file for reading.
            e.printStackTrace();
        }
    }
}
