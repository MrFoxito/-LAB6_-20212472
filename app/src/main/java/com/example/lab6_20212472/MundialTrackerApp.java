package com.example.lab6_20212472;

import android.app.Application;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;

public class MundialTrackerApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("es"));
    }
}
