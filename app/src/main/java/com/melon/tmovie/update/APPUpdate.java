package com.melon.tmovie.update;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;
import androidx.core.content.FileProvider;
import com.melon.tmovie.config.APPConfig;
import com.melon.tmovie.dialog.UpdateDialog;

import java.io.File;

public class APPUpdate {
    private APPConfig appConfig = new APPConfig();
    private UpdateDialog updateDialog = new UpdateDialog();

    public void update(Context context) {
        Thread thread = new Thread(() -> {
            if (!checkVersion(context)) return;
            updateDialog.show(context, appConfig.latestConfigInfo.updateContent, new UpdateDialog.UpdateDialogCallback() {
                @Override
                public void onConfirm() {
                    downloadApk(context);
                }
            });
        });
        thread.start();
    }

    private boolean checkVersion(Context context) {
        appConfig.init(context);
        return appConfig.currentConfigInfo.versionCode < appConfig.latestConfigInfo.versionCode ? true : false;
    }

    private void downloadApk(Context context) {
        String fileName = "TMovie.apk";
        if(!appConfig.currentConfigInfo.hasSdcard) return;
        UpdateService.download(fileName, new UpdateService.UpdateCallback() {
            @Override
            public void onSuccess() {
                updateDialog.downloadProgress.dismiss();
                try {
                    installApk(context, Environment.getExternalStorageDirectory() + fileName);
                } catch (Exception e) {
                    Log.d("update", e.toString());
                }
            }

            @Override
            public void onProgress(int progress) {
                updateDialog.downloadProgress.setProgress(progress);
            }

            @Override
            public void onFailure() {
                updateDialog.downloadProgress.dismiss();
            }
        });
    }

    private void installApk(Context context, String apkPath) {
        if (TextUtils.isEmpty(apkPath)) {
            Toast.makeText(context, "更新失败！未找到安装包", Toast.LENGTH_SHORT).show();
            return;
        }

        //File apkFile = new File(apkPath + apkCacheName);
        File apkFile = new File(apkPath);

        Intent intent = new Intent(Intent.ACTION_VIEW);
        //Android 7.0 系统共享文件需要通过 FileProvider 添加临时权限，否则系统会抛出 FileUriExposedException .
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Uri contentUri = FileProvider.getUriForFile(context, "com.melon.tmovie.fileprovider", apkFile);
            intent.setDataAndType(contentUri, "application/vnd.android.package-archive");
        } else {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setDataAndType(
                    Uri.fromFile(apkFile),
                    "application/vnd.android.package-archive");
        }
        context.startActivity(intent);
    }
}
