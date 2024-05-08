package com.melon.tmovie.update;

import com.melon.tmovie.config.APPConfig;

public class APPUpdate {
    private APPConfig appConfig;
    public void update() {
        if(!checkVersion()) return;

    }

    private boolean checkVersion() {
        return appConfig.currentConfigInfo.versionCode < appConfig.latestConfigInfo.versionCode ? true : false;
    }
}
