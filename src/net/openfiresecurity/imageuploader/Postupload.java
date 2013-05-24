/*
 * Copyright (c) 2013. Alexander Martinz.
 */

package net.openfiresecurity.imageuploader;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import net.openfiresecurity.helper.Constants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

public class Postupload extends Activity implements OnClickListener {

    @NotNull
    private String result = "", image_viewer = "", image_short_url = "",
            image_delete_link = "", shorturl = "", status = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handleIntent(getIntent());
        setContentView(R.layout.postupload);
        jsonToText(result);

        @NotNull Button bBack = (Button) findViewById(R.id.bBack);
        @NotNull Button bImageViewer = (Button) findViewById(R.id.bImageViewer);
        @NotNull Button bImageShort = (Button) findViewById(R.id.bImageShort);
        @NotNull Button bImageShortened = (Button) findViewById(R.id.bImageShortened);
        @NotNull Button bImageDelete = (Button) findViewById(R.id.bImageDelete);

        bBack.setOnClickListener(this);
        bImageViewer.setOnClickListener(this);
        bImageShort.setOnClickListener(this);
        bImageShortened.setOnClickListener(this);
        bImageDelete.setOnClickListener(this);

    }

    private void jsonToText(String json) {
        try {
            JSONObject jsonObject = new JSONObject(json);
            status += jsonObject.get("status_code");
            status += "\n" + jsonObject.get("status_txt");
            jsonObject = jsonObject.getJSONObject("data");
            image_viewer = (String) jsonObject.get("image_viewer");
            image_short_url = (String) jsonObject.get("image_short_url");
            image_delete_link = (String) jsonObject.get("image_delete_link");
            shorturl = (String) jsonObject.get("shorturl");
        } catch (Exception e) {
            Log.e(Constants.TAG, e.getMessage());
        }
    }

    private void handleIntent(@NotNull Intent intent) {
        @Nullable Bundle extras = intent.getExtras();
        if (extras != null) {
            result = extras.getString("result");
        }
    }

    @Override
    public void onClick(@NotNull View arg0) {
        switch (arg0.getId()) {
            case R.id.bBack:
                finish();
                break;
            case R.id.bImageViewer:
                displayLink(image_viewer);
                break;
            case R.id.bImageShort:
                displayLink(image_short_url);
                break;
            case R.id.bImageShortened:
                displayLink(shorturl);
                break;
            case R.id.bImageDelete:
                displayLink(image_delete_link);
                break;
        }
    }

    private void displayLink(String url) {
        @NotNull Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }
}
