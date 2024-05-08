package com.melon.tmovie.update;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;
import android.widget.Toast;
import androidx.core.content.FileProvider;
import com.melon.tmovie.config.APPConfig;
import com.melon.tmovie.dialog.UpdateDialog;

import java.io.File;

public class APPUpdate {
    private APPConfig appConfig;
    public void update() {
        if(!checkVersion()) return;

    }

    private boolean checkVersion() {
        return appConfig.currentConfigInfo.versionCode < appConfig.latestConfigInfo.versionCode ? true : false;
    }

    public void downloadApk(UpdateDialog dialog) {
        String fileName = "TMovie.apk";
        UpdateService.download(fileName, new UpdateService.UpdateCallback() {
            @Override
            public void onSuccess() {
                dialog.downloadProgress.dismiss();

                if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                    return;
                }

                File file = new File(Environment.getExternalStorageDirectory() + fileName);
                try {
                    //LoggerUtils.getLogger().info("安装文件目录：" + file);
                    //LoggerUtils.getLogger().info("准备安装");
                    installApk(file);
                } catch (Exception e) {
                    //LoggerUtils.getLogger().info("获取打开方式错误", e);
                }
            }

            @Override
            public void onProgress(int progress) {
                dialog.downloadProgress.setProgress(progress);
            }

            @Override
            public void onFailure() {
                dialog.downloadProgress.dismiss();
            }
        });
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
