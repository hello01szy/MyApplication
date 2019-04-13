package com.example.lenovo.myapplication;

import android.graphics.Bitmap;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class NetUtil {
    public static byte[] getAvator(String avatorUrl) {
        Bitmap imag = null;
        try {
            URL url = new URL(avatorUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            InputStream is = connection.getInputStream();
            connection.setConnectTimeout(5*1000);
            connection.setReadTimeout(5*1000);
            connection.setRequestMethod("GET");
            byte[] buffer = new byte[1024];
            int len = 0;
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            while((len = is.read(buffer)) != -1)
            {
                bos.write(buffer,0,len);
            }
            bos.close();
            connection.disconnect();
            return bos.toByteArray();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    //访问服务器InsertServlet
    public static boolean sendRequest(String requestUrl)
    {
        try
        {
            URL url = new URL(requestUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(5*1000);
            connection.setReadTimeout(5*1000);
            connection.setRequestMethod("GET");
            connection.connect();
            if(connection.getResponseCode() == 200)
            {
                connection.disconnect();
                return true;
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}
