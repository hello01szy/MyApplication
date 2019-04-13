package com.example.lenovo.myapplication;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class CheckNetWork {
    //判断网络是否连接
    public static boolean netAvailable(Context context)
    {
        if(context != null)
        {
            ConnectivityManager manager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = manager.getActiveNetworkInfo();
            if(networkInfo != null)
            {
                return networkInfo.isConnected();
            }
        }
        return false;
    }
}
