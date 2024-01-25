package com.nrt.e_learning.adapters;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class YourBroadcastReceiver extends BroadcastReceiver {

    public static final String ACTION_CUSTOM_BROADCAST = " ACTION_CUSTOM_BROADCAST";

    private void showNotification(Context context, String message) {
        // Implement your notification logic here
        // This is just a placeholder, replace it with your actual notification code
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        showNotification(context, "Broadcast Received");
    }
}