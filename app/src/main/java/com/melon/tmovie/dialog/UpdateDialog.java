package com.melon.tmovie.dialog;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Looper;

public class UpdateDialog {
    public ProgressDialog downloadProgress;

    public interface UpdateDialogCallback {
        void onConfirm();
    }

    public void show(Context context, String content, final UpdateDialogCallback callback) {
        Handler mainHandler = new Handler(Looper.getMainLooper());
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("版本更新");
                builder.setMessage(content);
                builder.setPositiveButton("更新", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        Handler mainHandler = new Handler(Looper.getMainLooper());
                        mainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                downloadProgress = new ProgressDialog(context);
                                downloadProgress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                                downloadProgress.setCanceledOnTouchOutside(false);
                                downloadProgress.setCancelable(true);
                                downloadProgress.setTitle("正在下载");
                                downloadProgress.setMessage("请稍后...");
                                downloadProgress.setProgress(0);
                                downloadProgress.setMax(100);
                                downloadProgress.show();

                                // 调用回调方法，执行确认操作
                                callback.onConfirm();
                            }
                        });
                    }
                });
                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.show();
            }
        });
    }
}
