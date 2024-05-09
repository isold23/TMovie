package com.melon.tmovie.update;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import com.melon.tmovie.MainActivity;
import com.melon.tmovie.R;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class UpdateService {
    private static OkHttpClient okHttpClient;

    public static void download(Context context, final String fileName, final UpdateCallback callback) {
        String url = "http://"+ context.getString(R.string.domain) + "/app/" + fileName;
        Request request = new Request.Builder()
                .addHeader("Accept-Encoding", "identity")
                .url(url).build();

        if (okHttpClient == null) {
            okHttpClient = new OkHttpClient();
        }

        okHttpClient.newCall(request).enqueue(new Callback() {
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

                //File filePath = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "TMovie");
                File file1 = context.getExternalFilesDir(null);
                File filePath = null;
                if(file1 != null) {
                    filePath = new File(file1.getAbsolutePath());
                }
                if (filePath!=null && !filePath.exists()) {
                    if(!filePath.mkdirs()) {
                        Log.d("update", "create dirs failed."+filePath);
                        return;
                    }
                }

                float contentLength = response.body().contentLength();
                byte[] buffer = new byte[1024*10];
                File file = new File(filePath.getCanonicalPath(), fileName);
                try (InputStream is = response.body().byteStream();
                     FileOutputStream fos = new FileOutputStream(file)) {

                    int length;
                    float sum = 0f;
                    while ((length = is.read(buffer)) != -1) {
                        fos.write(buffer, 0, length);
                        sum += length;
                        int progress = (int) (sum / contentLength * 100);
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
