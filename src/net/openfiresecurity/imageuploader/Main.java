/*
 * Copyright (c) 2013. Alexander Martinz.
 */

package net.openfiresecurity.imageuploader;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.*;
import android.widget.CompoundButton.OnCheckedChangeListener;
import net.openfiresecurity.helper.Uploader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

@SuppressLint("SimpleDateFormat")
public class Main extends Activity implements OnClickListener {

    private final int RESULT_LOAD_IMAGE = 123123123, CAMERA_REQUEST = 321321321;
    @NotNull
    private Button bSend;
    @NotNull
    private String filename = "";
    private ImageView iwPreview;
    private EditText etResize;
    private CheckBox cbResize;
    private TextView tvDimensions;
    private boolean ALLOK = false;
    private Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        @NotNull Button bLoad = (Button) findViewById(R.id.buttonLoadPicture);
        bSend = (Button) findViewById(R.id.bSend);
        bSend.setEnabled(false);
        iwPreview = (ImageView) findViewById(R.id.iwPreview);
        tvDimensions = (TextView) findViewById(R.id.tvDimensions);
        tvDimensions.setText("");

        etResize = (EditText) findViewById(R.id.etResize);
        cbResize = (CheckBox) findViewById(R.id.cbResize);
        cbResize.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
                etResize.setEnabled(arg1);
            }

        });
        etResize.setEnabled(false);

        bLoad.setOnClickListener(this);
        bSend.setOnClickListener(this);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @NotNull Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == RESULT_LOAD_IMAGE) {
                try {
                    // We need to recyle unused bitmaps
                    if (bitmap != null) {
                        bitmap.recycle();
                    }
                    // get dimensions
                    @Nullable InputStream stream = getContentResolver().openInputStream(
                            data.getData());
                    @NotNull BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inJustDecodeBounds = true;
                    BitmapFactory.decodeStream(stream, null, options);
                    stream.close();
                    int inWidth = options.outWidth;
                    int inHeight = options.outHeight;
                    //

                    stream = getContentResolver().openInputStream(
                            data.getData());
                    bitmap = BitmapFactory.decodeStream(stream);
                    stream.close();
                    iwPreview.setImageBitmap(bitmap);
                    bSend.setEnabled(true);
                    tvDimensions.setText(inWidth + "x" + inHeight);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (requestCode == CAMERA_REQUEST) {
                if (bitmap != null) {
                    bitmap.recycle();
                }
                resizeImage();

                iwPreview.setImageBitmap(bitmap);
                bSend.setEnabled(true);

            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    void resizeImage() {
        try {
            int dstWidth = 1900, dstHeight = 1900;
            @Nullable InputStream in = new FileInputStream(filename);

            // decode image size (decode metadata only, not the whole image)
            @NotNull BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(in, null, options);
            in.close();
            in = null;

            // save width and height
            int inWidth = options.outWidth;
            int inHeight = options.outHeight;
            tvDimensions.setText(inWidth + "x" + inHeight);

            if ((inWidth >= 1900) || (inHeight >= 1900)) {
                // decode full image pre-resized
                in = new FileInputStream(filename);
                options = new BitmapFactory.Options();
                // calc rought re-size (this is no exact resize)
                options.inSampleSize = Math.max(inWidth / dstWidth, inHeight
                        / dstHeight);
                // decode full image
                Bitmap roughBitmap = BitmapFactory.decodeStream(in, null,
                        options);

                // calc exact destination size
                @NotNull Matrix m = new Matrix();
                @NotNull RectF inRect = new RectF(0, 0, roughBitmap.getWidth(),
                        roughBitmap.getHeight());
                @NotNull RectF outRect = new RectF(0, 0, dstWidth, dstHeight);
                m.setRectToRect(inRect, outRect, Matrix.ScaleToFit.CENTER);
                @NotNull float[] values = new float[9];
                m.getValues(values);

                // resize bitmap
                bitmap = Bitmap.createScaledBitmap(roughBitmap,
                        (int) (roughBitmap.getWidth() * values[0]),
                        (int) (roughBitmap.getHeight() * values[4]), true);
            } else {
                @NotNull BitmapFactory.Options options1 = new BitmapFactory.Options();
                options1.inJustDecodeBounds = false;
                options1.inPreferQualityOverSpeed = true;

                bitmap = BitmapFactory.decodeFile(filename, options1);
            }
        } catch (IOException e) {
            Log.e("Image", e.getMessage(), e);
        }
    }

    @Override
    public void onClick(@NotNull View v) {
        switch (v.getId()) {
            case R.id.buttonLoadPicture:
                @NotNull AlertDialog.Builder dialog = new AlertDialog.Builder(Main.this);
                dialog.setTitle("Choose Method!")
                        .setMessage(
                                "Click 'Load' to load and use an existing Image.\n\nClick 'Take' to take and use a new Picture.\n");

                dialog.setNegativeButton("Load",
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                @NotNull Intent i = new Intent(
                                        Intent.ACTION_PICK,
                                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                                startActivityForResult(i, RESULT_LOAD_IMAGE);
                            }
                        });

                dialog.setPositiveButton("Take",
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                @NotNull File pictureFileDir = getDir();
                                pictureFileDir.mkdirs();
                                @NotNull SimpleDateFormat dateFormat = new SimpleDateFormat(
                                        "yyyymmddhhmmss");
                                String date = dateFormat.format(new Date());
                                @NotNull String photoFile = "Picture_" + date + ".jpg";

                                filename = pictureFileDir.getPath()
                                        + File.separator + photoFile;
                                @NotNull Intent cameraIntent = new Intent(
                                        android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                                @NotNull Uri imageFileUri = Uri.fromFile(new File(filename));
                                cameraIntent.putExtra(
                                        android.provider.MediaStore.EXTRA_OUTPUT,
                                        imageFileUri);
                                startActivityForResult(cameraIntent, CAMERA_REQUEST);
                            }
                        });

                dialog.show();
                break;
            case R.id.bSend:
                @NotNull ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] b = baos.toByteArray();
                bSend.setEnabled(false);
                String res = getResize();
                if (ALLOK) {
                    new Uploader(this).execute(new String[]{
                            Base64.encodeToString(b, Base64.DEFAULT), res});
                }
                break;
        }
    }

    @NotNull
    private File getDir() {
        @NotNull File sdDir = Environment
                .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        return new File(sdDir, "OpenFireImage");
    }

    String getResize() {
        if (cbResize.isChecked()) {
            String resize = etResize.getText().toString();
            resize = resize.trim();
            try {
                Integer.parseInt(resize);
            } catch (Exception exc) {
                ALLOK = false;
                ErrorDialog(exc.getLocalizedMessage());
            }
            ALLOK = true;
            return (resize);
        }
        ALLOK = true;
        return "";
    }

    public void ErrorDialog(String msg) {
        @NotNull AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Error!").setMessage(msg);
        dialog.setNeutralButton("Ok", null);
        dialog.show();
    }

    public void displayResult(String result) {
        @NotNull Intent intent = new Intent(Main.this, Postupload.class);
        intent.putExtra("result", result);
        startActivity(intent);
        finish();
    }

    public void makeToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }
}
