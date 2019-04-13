package com.example.lenovo.myapplication;

import java.text.SimpleDateFormat;
import java.util.Date;

public class GetSystemTime {
    private static SimpleDateFormat format;

    public static String getDate(String content)
    {
        format = new SimpleDateFormat(content);
        return format.format(new Date());
    }
    public static String getDate()
    {
        return getDate("hh:mm:ss");
    }
}
