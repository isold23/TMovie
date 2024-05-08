package com.melon.tmovie.update;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.widget.Toast;
import androidx.core.content.FileProvider;
import com.melon.tmovie.config.APPConfig;

import java.io.File;

public class APPUpdate {
    private APPConfig appConfig;
    public void update() {
        if(!checkVersion()) return;

    }

    private boolean checkVersion() {
        return appConfig.currentConfigInfo.versionCode < appConfig.latestConfigInfo.versionCode ? true : false;
    }

    public void downloadApk() {

    }

    public void installApk(Context context, String apkPath) {
        if (TextUtils.isEmpty(apkPath)){
            Toast.makeText(context,"更新失败！未找到安装包", Toast.LENGTH_SHORT).show();
            return;
        }

        //File apkFile = new File(apkPath + apkCacheName);
        File apkFile = new File(apkPath);

        Intent intent = new Intent(Intent.ACTION_VIEW);
        //Android 7.0 系统共享文件需要通过 FileProvider 添加临时权限，否则系统会抛出 FileUriExposedException .
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.N){
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Uri contentUri = FileProvider.getUriForFile(context,"com.skyrin.bingo.fileprovider",apkFile);
            intent.setDataAndType(contentUri,"application/vnd.android.package-archive");
        }else {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setDataAndType(
                    Uri.fromFile(apkFile),
                    "application/vnd.android.package-archive");
        }
        context.startActivity(intent);
    }
}
