package com.yasha.yasha;

import android.app.Application;

import com.parse.Parse;

public class YashaApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Parse.initialize(this, "mwB1nvKz9hlkFnbIumMRNGNDhJIpMpWJHyOSkjLd", "G8WEkJxhJkISPRfFISTrvmJY7TzaHWXo2Ir8rVkY");
    }
}
