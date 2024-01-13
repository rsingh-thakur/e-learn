package com.nrt.e_learning.util;
import android.content.Context;
import android.content.SharedPreferences;
import java.util.Random;

public class OTPManager {

    private static final String OTP_PREFS_NAME = "OTP_PREFERENCES";
    private static final String OTP_KEY = "OTP_VALUE";
    private static final String TIMESTAMP_KEY = "TIMESTAMP";

    // Generate a random 6-digit OTP
    public static String generateOTP() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }

    // Save OTP and current timestamp to SharedPreferences
    public static void saveOTP(Context context, String otp) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(OTP_PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(OTP_KEY, otp);
        editor.putLong(TIMESTAMP_KEY, System.currentTimeMillis());
        editor.apply();
    }

    // Retrieve stored OTP from SharedPreferences
    public static String getStoredOTP(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(OTP_PREFS_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(OTP_KEY, null);
    }

    // Check if the stored OTP is still valid (within 10 minutes)
    public static boolean isOTPValid(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(OTP_PREFS_NAME, Context.MODE_PRIVATE);
        long timestamp = sharedPreferences.getLong(TIMESTAMP_KEY, 0);
        long currentTime = System.currentTimeMillis();
        return (currentTime - timestamp) <= (10 * 60 * 1000); // 10 minutes in milliseconds
    }

    // Clear stored OTP from SharedPreferences
    public static void clearOTP(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(OTP_PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(OTP_KEY);
        editor.remove(TIMESTAMP_KEY);
        editor.apply();
    }
}
