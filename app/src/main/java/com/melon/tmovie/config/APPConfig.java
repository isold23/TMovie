package com.melon.tmovie.config;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.StringReader;

public class APPConfig {
    private String config_url = "http://tv.hzdianyue.com/app/config.xml";
    public ConfigInfo latestConfigInfo;
    public ConfigInfo currentConfigInfo;

    public void getLatestConfig() {
        Thread thread = new Thread(() -> {
            OkHttpClient okHttpClient = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(config_url)
                    .build();
            try {
                Response response = okHttpClient.newCall(request).execute();
                if(response.body() != null) {
                    parseAppInfo(response.body().string());
                }
                response.close();
            } catch (IOException | XmlPullParserException e) {
                e.printStackTrace();
            }
        });
        thread.start();
    }

    private void parseAppInfo(String xmlData) throws XmlPullParserException, IOException {
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        XmlPullParser parser = factory.newPullParser();
        parser.setInput(new StringReader(xmlData));
        int eventType = parser.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT) {
            String tagName = parser.getName();
            switch (eventType) {
                case XmlPullParser.START_TAG:
                    if("minVersionCode".equals(tagName)) {
                        latestConfigInfo.minVersionCode = Integer.parseInt(parser.nextText());
                    }
                    if("versionCode".equals(tagName)) {
                        latestConfigInfo.versionCode = Integer.parseInt(parser.nextText());
                    }
                    if ("versionName".equals(tagName)) {
                        latestConfigInfo.versionName = parser.nextText();
                    }
                    break;
                case XmlPullParser.END_TAG:
                    break;
            }
            eventType = parser.next();
        }
    }

    public void getCurrentConfig(Context context) {
        try {
            PackageManager manager = context.getPackageManager();
            PackageInfo info = manager.getPackageInfo(context.getPackageName(),
                    0);
            currentConfigInfo.versionName = info.versionName;
            currentConfigInfo.versionCode = info.versionCode;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
