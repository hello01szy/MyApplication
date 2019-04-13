package com.example.lenovo.myapplication;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class CopyFile {
    public static boolean copyFile(String oldPath, String newPath)
    {
        File oldFile = new File(oldPath);
        if (!oldFile.exists() || !oldFile.isFile() || !oldFile.canRead()) {
            return false;
        }
        try {
            FileInputStream inputStream = new FileInputStream(oldPath);
            FileOutputStream outputStream = new FileOutputStream(newPath);
            byte[] buffer = new byte[1024];
            int byteRead;
            while((byteRead = inputStream.read(buffer)) != -1)
            {
                outputStream.write(buffer, 0, byteRead);
            }
            inputStream.close();
            outputStream.flush();
            outputStream.close();
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}
