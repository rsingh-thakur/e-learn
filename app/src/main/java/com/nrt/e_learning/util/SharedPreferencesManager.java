package com.nrt.e_learning.util;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.nrt.e_learning.LoginActivity;

public class SharedPreferencesManager {
    private static final String PREFERENCES_NAME = "MyAppPreferences";
    private static final String KEY_LOGGED_IN_USER = "LoggedInUser";
    private static final String KEY_LOGIN_TIME = "LoginTime";

    // Set the user login state
    public static void setLoggedInUser(Context context, boolean isLoggedIn) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putBoolean(KEY_LOGGED_IN_USER, isLoggedIn);
        editor.apply();
    }

    // Get the user login state
    public static boolean isLoggedInUser(Context context) {
        return getSharedPreferences(context).getBoolean(KEY_LOGGED_IN_USER, false);
    }

    // Set the login time
    public static void setLoginTime(Context context, long loginTime) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putLong(KEY_LOGIN_TIME, loginTime);
        editor.apply();
    }

    // Get the login time
    public static long getLoginTime(Context context) {
        return getSharedPreferences(context).getLong(KEY_LOGIN_TIME, 0);
    }

    // Clear all SharedPreferences (useful for logout)
    public static void clearSharedPreferences(Context context) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.clear();
        editor.apply();
    }

    private static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    public static void clearUserData(Context context) {

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove(KEY_LOGGED_IN_USER);
        editor.remove(KEY_LOGIN_TIME);
        // Add other user data keys to remove if needed
        editor.apply();
    }

    public static void logOut(Context context){
        SharedPreferencesManager.setLoggedInUser(context, false);
        SharedPreferencesManager.setLoginTime(context, 0);
    }
}
