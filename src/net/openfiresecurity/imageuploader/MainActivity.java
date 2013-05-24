/*
 * Copyright (c) 2013. Alexander Martinz.
 */

package net.openfiresecurity.imageuploader;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DownloadManager;
import android.app.DownloadManager.Request;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.Toast;
import net.openfiresecurity.helper.Constants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressLint({"InlinedApi", "NewApi"})
public class MainActivity extends Activity {
    private DownloadManager mgr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mgr = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        @NotNull ImageButton b = (ImageButton) findViewById(R.id.bUpload);
        b.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                startActivity(new Intent(MainActivity.this, Main.class));
            }

        });

        @NotNull ImageButton a = (ImageButton) findViewById(R.id.bExit);
        a.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                System.exit(0);
            }

        });

        @NotNull ImageButton c = (ImageButton) findViewById(R.id.bUpdate);
        c.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                new CheckVersion(MainActivity.this).execute();
            }

        });

        @NotNull BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, @NotNull Intent intent) {
                @Nullable String action = intent.getAction();
                if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
                    @NotNull Intent i = new Intent();
                    i.setAction(DownloadManager.ACTION_VIEW_DOWNLOADS);
                    startActivity(i);
                }
            }
        };

        registerReceiver(receiver, new IntentFilter(
                DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }

    public void update(String result) {
        try {
            @NotNull String appname = "_";
            @NotNull String version = getVersionNumber("net.openfiresecurity.imageuploader");
            if (Integer.parseInt(version) < Integer.parseInt(result)) {
                appname = Constants.fileName + result + ".apk";
                @NotNull Request req = new Request(Uri.parse(Constants.urls + appname));

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    req.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
                }
                req.setDescription("Updating!");
                req.setTitle(appname);
                req.setMimeType("application/vnd.android.package-archive");
                req.setDestinationInExternalPublicDir(
                        Environment.DIRECTORY_DOWNLOADS, appname);

                mgr.enqueue(req);
                makeToast("Downloading!");
            } else {
                makeToast("No new Update available!");
            }
        } catch (Exception exc) {
            Log.d("IMAGE", exc.getLocalizedMessage());
            makeToast("Couldnt contact Update Server!");
        }
    }

    void makeToast(String msg) {
        Toast.makeText(MainActivity.this, msg, Toast.LENGTH_LONG).show();
    }

    @NotNull
    String getVersionNumber(String PackageName) {
        int version = -1;
        try {
            PackageInfo pi = getPackageManager().getPackageInfo(PackageName, 0);
            version = pi.versionCode;
        } catch (Exception e) {
            Log.e(Constants.TAG, e.getMessage());
        }
        return ("" + version);
    }

}
