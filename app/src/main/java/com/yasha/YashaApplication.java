package com.yasha;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseCrashReporting;

public class YashaApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        ParseCrashReporting.enable(this);
        Parse.initialize(this, "mwB1nvKz9hlkFnbIumMRNGNDhJIpMpWJHyOSkjLd", "G8WEkJxhJkISPRfFISTrvmJY7TzaHWXo2Ir8rVkY");
    }
}
