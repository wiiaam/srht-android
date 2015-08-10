package io.github.wiiam.srhtapp;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.NotificationCompat;
import android.util.JsonReader;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.Toast;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import com.loopj.android.http.*;
import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import io.github.wiiam.srhtapp.config.Config;

/**
 * Created by william on 9/07/15.
 */
public class Share extends Activity {

    private final int maxBufferSize = 104857600;
    private static final String DEBUG_TAG = "SHARE-DEBUG";
    private static final String tag = "SHARE-DEBUG";
    public NotificationManager mNotifyManager;
    public NotificationCompat.Builder mBuilder;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        String action = intent.getAction();
        if (Intent.ACTION_SEND.equals(action)) {
            if (extras.containsKey(Intent.EXTRA_STREAM)) {
                // Get resource path
                Uri uri = (Uri) extras.getParcelable(Intent.EXTRA_STREAM);
                loadConfig();
                if (uri != null) {
                    if(Config.getApiKey() == "" || Config.getApiKey() == null){
                        Toast.makeText(getApplicationContext(),"API key not set",Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Toast.makeText(getApplicationContext(),"Uploading file...",Toast.LENGTH_SHORT).show();
                    String path = parseUriToFilename(uri);
                    try {
                        upload(path, intent.getType());
                    }
                    catch (Exception e) {
                        Toast.makeText(getApplicationContext(), "Upload failed", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }
    private String parseUriToFilename(Uri uri) {
        String selectedImagePath = null;
        String filemanagerPath = uri.getPath();
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        if (cursor != null) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            selectedImagePath = cursor.getString(column_index);
        }
        if (selectedImagePath != null) {
            return selectedImagePath;
        }
        else if (filemanagerPath != null) {
            return filemanagerPath;
        }
        return null;
    }

    private void upload(String filepath, String mimetype) throws FileNotFoundException, IOException {
        String filename = filepath.split("/")[filepath.split("/").length-1];
        RequestParams params = new RequestParams();
        params.put("key", Config.getApiKey());
        final int NOTIF_ID = (int)(Math.random()*1000);
        String url = Config.getUrl();
        String extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimetype);
        if (extension != null) {
            extension = "." + extension;
        } else {
            extension = "";
            if (mimetype.equals("image/*")) {
                extension = ".png"; // stupid guess, browsers can probably handle it anyway
            }
        }
        File toupload = new File(filepath);
        params.put("file", toupload);

        AsyncHttpClient client = new AsyncHttpClient(true, 80, 443);
        mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setContentTitle("File Upload to sr.ht")
                .setContentText(toupload.getName())
                .setSmallIcon(R.drawable.icon)
                .setProgress(100, 0, true);
        Notification notif = mBuilder.build();
        notif.flags = Notification.FLAG_ONGOING_EVENT;
        mNotifyManager.notify(NOTIF_ID,notif);
        client.post("https://" + url + "/api/upload", params, new AsyncHttpResponseHandler() {

            @Override
            public void onProgress(int bytesWritten, int totalSize) {
                int per = (int)(((double)bytesWritten/(double)totalSize)*100);
                Log.d(DEBUG_TAG, "UPLOAD pos: " + bytesWritten + " len: " + totalSize + " per: " + per + "%");
                mBuilder.setProgress(100, per, false);
                mBuilder.setContentInfo(per + "%");
                Notification notif = mBuilder.build();
                notif.flags = Notification.FLAG_ONGOING_EVENT;
                mNotifyManager.notify(NOTIF_ID, notif);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Log.d(DEBUG_TAG, "UPLOAD FAIL " + error.toString());
                mBuilder.setProgress(0, 0, false)
                        .setContentInfo("");
                if(error.getClass().getSimpleName().equals("IOException")){
                    mBuilder.setContentText("Could not resolve host");
                }
                else if(error.getClass().getSimpleName().equals("HttpResponseException")){
                    mBuilder.setContentText("Invalid API Key. Check settings and try again");
                }
                else {
                    mBuilder.setContentText("Upload failed. Check settings and try again");
                }
                mNotifyManager.notify(NOTIF_ID, mBuilder.build());
                Toast.makeText(getApplicationContext(), "Upload failed", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onSuccess(int statusCode, org.apache.http.Header[] headers, byte[] responseBody){
                Log.d(DEBUG_TAG,"Upload completed");
                try {
                    mBuilder.setContentText("Upload complete. Tap to view")
                            .setProgress(0, 0, false)
                            .setContentInfo("");

                    JSONObject json = new JSONObject(new String(responseBody));
                    Log.d(DEBUG_TAG,"JSON: " + json.toString());
                    Uri result = Uri.parse(json.getString("url"));
                    Intent launchBrowser = new Intent(Intent.ACTION_VIEW, result);
                    PendingIntent resultPendingIntent =
                            PendingIntent.getActivity(
                                    getApplicationContext(),
                                    0,
                                    launchBrowser,
                                    PendingIntent.FLAG_UPDATE_CURRENT
                            );
                    mBuilder.setContentIntent(resultPendingIntent);
                    mNotifyManager.notify(NOTIF_ID, mBuilder.build());
                    ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("sr.ht URL", result.toString());
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(getApplicationContext(), "File uploaded, URL copied to clipboard.", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Log.d(DEBUG_TAG,"Upload complete with exception " + e.toString());
                    mBuilder.setContentText("Upload failed. Check URL and API key")
                            .setProgress(0, 0, false)
                            .setContentInfo("");
                    mNotifyManager.notify(NOTIF_ID, mBuilder.build());
                    Toast.makeText(getApplicationContext(), "Upload failed", Toast.LENGTH_SHORT).show();
                }
                finish();
            }
        });
    }
    private void loadConfig(){
        try {
            Config.load(new File(getFilesDir(), "config.json"));
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}