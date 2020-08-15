package com.example.myapplication.ui.utils;

import java.io.FileOutputStream;
import java.io.InputStream;

public class FileUtil {
    public static FileOutputStream Input2File(InputStream in,String path) throws Exception
    {
        FileOutputStream outputStream = new FileOutputStream(path);
        int ch;
        while ((ch = in.read()) != -1) {
            outputStream.write(ch);
        }
        return outputStream;
    }
}
