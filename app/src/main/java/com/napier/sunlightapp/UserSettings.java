package com.napier.sunlightapp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;

/**
 * A class to retrieve and store user settings.
 */
public class UserSettings {
    // User settings:
    private static String userName;
    private static String target;
    private static int remainingTarget;
    private static boolean targetAchieved=false;
    private static String notificationsEnabled = "true"; // default
    private static ArrayList<Walk> walks = new ArrayList<>();
    private static boolean latestWalkWasToday=false;
    private static Walk lastWalk;

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
     * Indicates if the daily target was achieved byt the user
     * @return true if achieved, false it not
     */
    public static boolean isTargetAchieved() { return targetAchieved; }

    /**
     * Gets the remaining target
     * @return remaining target
     */
    public static int getRemainingTarget() { return remainingTarget; }

    public static ArrayList<Walk> getWalks() {
        return walks;
    }

    public static boolean isLatestWalkWasToday() {
        return latestWalkWasToday;
    }

    public static Walk getLastWalk() {
        return lastWalk;
    }

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
            while (line != null) {
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
                line = reader.readLine();
            }
        } catch (Exception e) {
            // Error occurred when opening raw file for reading.
            e.printStackTrace();
        }
    }

    /**
     * Loads the walk history from the provided File Input Stream
     * @param fis File Input Stream from where to read
     */
    public static void loadWalkHistory(FileInputStream fis){
        /* Read from a file located in the internal storage directory provided by the system for this and only this app: */
        walks.clear(); // clear the existing walk list

        InputStreamReader inputStreamReader;
        // read from the file using the provided InputStreamReader:
        inputStreamReader = new InputStreamReader(fis, Charset.defaultCharset());
        try {
            BufferedReader reader = new BufferedReader(inputStreamReader);

            String line = reader.readLine();
            while(line!=null){ // loop through the lines
                Walk walk=new Walk();
                String[] words = line.split(",");
                if (!words[0].equals("null")){
                    walk.setDate(words[0]);
                }
                if (!words[1].equals("null")){
                    walk.setWalkTime(Integer.parseInt(words[1]));
                }
                if (!words[2].equals("null")){
                    walk.setNotes(words[2]);
                }
                walks.add(walk);
                line = reader.readLine();
            }

        } catch (Exception e) {
            // Error occurred when opening raw file for reading.
            e.printStackTrace();
        }

        // if there are past walks:
        if(walks.size()>0){
            // get the first item in the list (last walk):
            lastWalk = walks.get(0);

            // get the current date:
            Calendar calendar = Calendar.getInstance();
            String pattern = "dd-MM-yyy";
            String date = new SimpleDateFormat(pattern).format(calendar.getTime());

            // if the latest walk's date and today's date are the same:
            if(date.equals(lastWalk.getDate())){
                // set latest walk was today as true:
                latestWalkWasToday=true;
            }
        }

        // if a walk was recorded today:
        if(latestWalkWasToday){
            // if the user has a target set-up:
            if(target!=null && target.length()>0){
                // calculate the difference between the actual walk and the target:
                try {
                    remainingTarget=Integer.parseInt(target)-lastWalk.getWalkTime();
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
                // if the difference is negative:
                if(remainingTarget<=0){
                    // set target achieved as true:
                    targetAchieved=true;
                }
            }
        }
        // if there was no walk recorded for today:
        else {
            // if the user has a target set up:
            if(target!=null && target.length()>0) {
                // set target as the remaining time:
                try {
                    remainingTarget=Integer.parseInt(target);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Saves a new walk to walk history file.
     * @param file File to where the walk needs to be saved
     * @param walkToSave Walk to save
     * @throws IOException FileOutputStream exception
     */
    public static void saveWalk(File file, Walk walkToSave) throws IOException {
        // check if the latest walk is today (editing a walk case):
        if(isLatestWalkWasToday()){
            walks.remove(0); // remove the last walk
        }
        walks.add(0, walkToSave); // add a new walk to the beginning of the walk list

        // store only 30 last walks:
        if(walks.size()>30){
            walks.remove(walks.size()-1); // remove the oldest walk
        }
        // save all the walks:
        FileOutputStream stream = new FileOutputStream(file);
        try {
            for(Walk w: walks){
                String toSave = w.getDate()+","+w.getWalkTime()+','+w.getNotes()+"\n";
                stream.write(toSave.getBytes());
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            stream.close();
        }
    }
}
