package com.melon.tmovie.update;

import android.os.Environment;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class UpdateService {
    private static OkHttpClient okHttpClient;

    public static void download(final String fileName, final UpdateCallback callback) {
        String url = "http://tv.hzdianyue.com/app/" + fileName;
        Request request = new Request.Builder()
                .addHeader("Accept-Encoding", "identity")
                .url(url).build();

        if (okHttpClient == null) {
            okHttpClient = new OkHttpClient();
        }

        okHttpClient.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                callback.onFailure();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.code() != 200) {
                    callback.onFailure();
                    return;
                }

                File filePath = new File(String.valueOf(Environment.getExternalStorageDirectory()));
                if (!filePath.exists()) {
                    filePath.mkdirs();
                }

                long contentLength = response.body().contentLength();
                byte[] buffer = new byte[1024];
                File file = new File(filePath.getCanonicalPath(), fileName);
                try (InputStream is = response.body().byteStream();
                     FileOutputStream fos = new FileOutputStream(file)) {

                    int length;
                    long sum = 0;
                    while ((length = is.read(buffer)) != -1) {
                        fos.write(buffer, 0, length);
                        sum += length;
                        int progress = (int) (sum * 1.0f / contentLength * 100);
                        callback.onProgress(progress);
                    }
                    fos.flush();
                    callback.onSuccess();
                } catch (Exception e) {
                    callback.onFailure();
                }
            }
        });
    }

    public interface UpdateCallback {
        void onSuccess();

        void onProgress(int progress);

        void onFailure();
    }
}
