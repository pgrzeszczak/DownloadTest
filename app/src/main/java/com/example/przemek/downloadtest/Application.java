package com.example.przemek.downloadtest;

import com.liulishuo.filedownloader.FileDownloader;

/**
 * Created by przemek on 16.02.16.
 */
public class Application extends android.app.Application {
    @Override
    public void onCreate() {
        super.onCreate();
        FileDownloader.init(this);
    }
}
