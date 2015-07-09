package io.github.wiiam.srhtapp;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import net.haxx.curl.CurlGlue;
import net.haxx.curl.CurlWrite;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

import javax.net.ssl.HttpsURLConnection;

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

                if (uri != null) {
                    id(Config.getApiKey() == "" || Config.getApiKey() == null){
                        Toast.makeText(getApplicationContext(),"API key not set",Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Toast.makeText(getApplicationContext(),"Uploading file...",Toast.LENGTH_SHORT).show();
                    String path = parseUriToFilename(uri);
                    String hostedurl = upload(path);
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

    private String upload(String filename) {
        String result = sendRequest("https://sr.ht/api/upload","POST",null, new File(filename), filename);
        System.out.println("result: " + result);
        return result;
    }

    private static final String REQUEST_BOUNDARY = "------------------------BKELWRNGPXMW";

    private String sendRequest(String url, String method, String apikey, File file, String fileField) {
        HttpURLConnection httpConn = null;
        StringBuilder httpContent = new StringBuilder();
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        byte[] fileContent = new byte[0];
        int fileSize = 0;

        // trying to read file
        if (file != null) {
            try {
                FileInputStream fileInputStream = new FileInputStream(file);

                /*
                httpContent.append(twoHyphens + REQUEST_BOUNDARY + lineEnd);
                httpContent.append("Content-Disposition: form-data; key=\"" + apikey + "\"");
                httpContent.append(apikey);
                */
                httpContent.append(REQUEST_BOUNDARY + lineEnd);
                httpContent.append("Content-Disposition: form-data; name=\"" + fileField + "\"; filename=\"" + file.getName() + "\"" + lineEnd);
                httpContent.append(lineEnd);

                fileSize = fileInputStream.available();
                fileContent = new byte[fileSize];
                fileInputStream.read(fileContent, 0, fileSize);
                fileInputStream.close();
            }
            catch (Exception e){
                Log.d(DEBUG_TAG, "Exception occured: " + e.toString());
            }
        }

        // trying to perform request
        try {
            httpConn = (HttpURLConnection) new URL(url).openConnection();

            if (httpConn != null) {
                httpConn.setDoInput(true);
                httpConn.setDoOutput(true);
                httpConn.setUseCaches(false);
                //httpConn.setConnectTimeout(CONNECTION_TIMEOUT_STRING);
                httpConn.setRequestMethod(method);

                if (file != null && httpContent.length() > 0) {
                    httpConn.addRequestProperty("Connection", "Keep-Alive");
                    httpConn.addRequestProperty("Content-Type", "multipart/form-data; boundary=" + REQUEST_BOUNDARY);
                    httpConn.addRequestProperty("Content-Type", "multipart/form-data; key=" + apikey);
                    httpConn.addRequestProperty("Content-Length", String.valueOf(fileSize));


                    DataOutputStream dos = new DataOutputStream(httpConn.getOutputStream());
                    dos.writeBytes(httpContent.toString());

                    dos.write(fileContent, 0, fileSize);
                    dos.writeBytes(lineEnd);
                    dos.writeBytes(REQUEST_BOUNDARY + lineEnd);
                    dos.flush();
                    dos.close();
                }

                httpConn.connect();

                int response = httpConn.getResponseCode();
                BufferedReader rd;

                if (httpConn.getErrorStream() == null) {
                    rd = new BufferedReader(new InputStreamReader(httpConn.getInputStream()));
                } else {
                    rd = new BufferedReader(new InputStreamReader(httpConn.getErrorStream()));
                }

                StringBuffer sb = new StringBuffer();
                String line;
                while ((line = rd.readLine()) != null) {
                    sb.append(line + "\n");
                }
                if (rd != null) {
                    rd.close();
                }
                return sb.toString();
            } else {
                Log.d(DEBUG_TAG, "Connection Error");
            }
        }
        catch (Exception e){
            Toast.makeText(getApplicationContext(), "Exception occured: " + e.toString(), Toast.LENGTH_SHORT).show();
        }
        finally {
            if (httpConn != null) {
                httpConn.disconnect();
            }
        }
        return null;
    }
}