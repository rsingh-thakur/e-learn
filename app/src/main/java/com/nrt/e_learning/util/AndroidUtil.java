package com.nrt.e_learning.util;

import android.content.Context;
import android.widget.Toast;


public class AndroidUtil {

    public static void showToast(Context context , String Message){
        Toast.makeText(context, Message, Toast.LENGTH_LONG).show();
    }
}
