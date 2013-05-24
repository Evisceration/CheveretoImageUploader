/*
 * Copyright (c) 2013. Alexander Martinz.
 */

package net.openfiresecurity.helper;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;
import net.openfiresecurity.imageuploader.CustomMultiPartEntity;
import net.openfiresecurity.imageuploader.CustomMultiPartEntity.ProgressListener;
import net.openfiresecurity.imageuploader.Main;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Uploader extends AsyncTask<String, Integer, String> {

    private final Main c;
    @NotNull
    private final ProgressDialog pd;
    private long totalSize;

    /**
     * Constructer of the Uploader, takes the main activity as param, so we can display a progress dialog.
     *
     * @param context Our Mainactivity
     */
    public Uploader(Main context) {
        c = context;
        pd = new ProgressDialog(c);
        pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        pd.setMessage("Uploading Picture...");
        pd.setCancelable(false);
        pd.show();
    }

    @Override
    protected void onPreExecute() {
    }

    @Override
    protected String doInBackground(String... Path) {
        String encodedImg = Path[0];
        String resize = Path[1];

        @NotNull HttpClient httpClient = new DefaultHttpClient();
        @NotNull HttpContext httpContext = new BasicHttpContext();
        @NotNull HttpPost httpPost = new HttpPost(Constants.BASEURL);
        try {
            @NotNull CustomMultiPartEntity multipartContent = new CustomMultiPartEntity(
                    HttpMultipartMode.BROWSER_COMPATIBLE,
                    new ProgressListener() {
                        @Override
                        public void transferred(long num) {
                            publishProgress((int) ((num / (float) totalSize) * 100));
                        }
                    });

            multipartContent.addPart("key", new StringBody(
                    Constants.APIKEY));
            multipartContent.addPart("upload", new StringBody(encodedImg));
            multipartContent.addPart("function", new StringBody("json"));
            if (!resize.equals("")) {
                multipartContent
                        .addPart("resize_width", new StringBody(resize));
            }

            totalSize = multipartContent.getContentLength();
            httpPost.setEntity(multipartContent);
            try {
                HttpResponse httpResponse = httpClient.execute(httpPost,
                        httpContext);
                return (entityToString(httpResponse.getEntity()));
            } catch (Exception exc) {
                c.ErrorDialog(exc.getLocalizedMessage());
                return (exc.getMessage());
            }
        } catch (Exception excp) {
            c.ErrorDialog(excp.getLocalizedMessage());
            return (excp.getMessage());
        }
    }

    /**
     * Converts HttpEntities into readable text.
     *
     * @param entity Entity, which should get converted to a String.
     */
    @NotNull
    String entityToString(@NotNull HttpEntity entity) {
        @Nullable InputStream is = null;
        @Nullable StringBuilder str = null;
        try {
            is = entity.getContent();
            @NotNull BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(is));
            str = new StringBuilder();

            @Nullable String line = null;
            while ((line = bufferedReader.readLine()) != null) {
                str.append(line);
            }
        } catch (IOException e) {
            Log.e(Constants.TAG, e.getMessage());
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                Log.e(Constants.TAG, e.getMessage());
            }
        }
        return str.toString();
    }

    @Override
    protected void onProgressUpdate(Integer... progress) {
        pd.setProgress((progress[0]));
    }

    @Override
    protected void onPostExecute(String result) {
        pd.dismiss();
        c.displayResult(result);
    }
}
