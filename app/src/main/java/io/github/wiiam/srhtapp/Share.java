package io.github.wiiam.srhtapp;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.webkit.MimeTypeMap;
import android.widget.Toast;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
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

        InputStream stream = new FileInputStream(new File(filepath));
        params.put("file", stream, filename + extension);

        AsyncHttpClient client = new AsyncHttpClient(true, 80, 443);
        client.post("https://" + url + "/api/upload", params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    Uri result = Uri.parse(response.getString("url"));
                    ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("sr.ht URL", result.toString());
                    clipboard.setPrimaryClip(clip);
                    Intent launchBrowser = new Intent(Intent.ACTION_VIEW, result);
                    startActivity(launchBrowser);
                    Toast.makeText(getApplicationContext(), "File uploaded, URL copied to clipboard.", Toast.LENGTH_SHORT).show();
                } catch (JSONException e) {
                    Toast.makeText(getApplicationContext(), "Upload failed", Toast.LENGTH_SHORT).show();
                }
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