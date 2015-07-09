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
                    if(Config.getApiKey() == "" || Config.getApiKey() == null){
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

    private String upload(String filepath) {
        String filename = filepath.split("/")[filepath.split("/").length-1];
        String result = sendRequest("https://sr.ht/api/upload", Config.getApiKey(), new File(filepath), filename);
        Log.d(DEBUG_TAG, "RESULT: " + result);
        Toast.makeText(getApplicationContext(),result,Toast.LENGTH_SHORT).show();
        return result;
    }



    private final String REQUEST_BOUNDARY = "BKELWRNGPXMW";





    private String sendRequest(String url, String apikey, File file, String fileField) {
        // trying to read file
        try {
            String lineEnd = "\r\n";
            FileInputStream fileInputStream = new FileInputStream(file);

            
            int fileSize = (int)file.length();
            byte[] fileContent = new byte[fileSize];
            fileInputStream.read(fileContent);//, 0, fileSize);
            fileInputStream.close();

            HttpURLConnection httpConn = (HttpURLConnection) new URL(url).openConnection();

            if (httpConn != null) {
                httpConn.setDoInput(true);
                httpConn.setDoOutput(true);
                httpConn.setUseCaches(false);
                //httpConn.setConnectTimeout(CONNECTION_TIMEOUT_STRING);
                httpConn.setRequestMethod("POST");

                if (file != null) {
                    //httpConn.setRequestProperty("User-Agent", "Mozilla");
                    httpConn.setRequestProperty("Connection", "Keep-Alive");
                    httpConn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + REQUEST_BOUNDARY);// + "; key=" + apikey);
                    //httpConn.setRequestProperty("Content-Type", "multipart/form-data; key=" + apikey);
                    //httpConn.setRequestProperty("key" , apikey);
                    //httpConn.addRequestProperty("Content-Length", "" + fileSize);
                    //httpConn.connect();

                    DataOutputStream dos = new DataOutputStream(httpConn.getOutputStream());
                    //dos.writeBytes("--" + REQUEST_BOUNDARY + lineEnd);
                    //dos.writeBytes("Content-Disposition: form-data; name=\"key\'" + lineEnd);
                    //dos.writeBytes(apikey + lineEnd);
                    dos.writeBytes("--" + REQUEST_BOUNDARY + lineEnd);
                    dos.writeBytes("Content-Disposition: form-data; name=\"" + fileField + "\"; filename=\"" + file.getName() + "\"" + lineEnd);
                    dos.writeBytes("Content-Type: image/jpeg" + lineEnd);
                    dos.writeBytes(lineEnd);
                    dos.write(fileContent, 0, fileSize);
                    /*
                    FileInputStream fileInputStream = new FileInputStream(file);
                    int bytesRead = fileInputStream.read(fileContent, 0, fileSize);

                    while (bytesRead > 0) {
                        dos.write(fileContent, 0, bytesRead);
                        bytesRead = fileInputStream.read(fileContent, 0, fileSize);
                    }
                    */
                    dos.writeBytes(lineEnd);
                    dos.writeBytes("--" + REQUEST_BOUNDARY + "--" + lineEnd);
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
                Log.d(DEBUG_TAG,sb.toString());
                httpConn.disconnect();
                return sb.toString();
            } else {
                Log.d(DEBUG_TAG, "Connection Error");
            }

        }
        catch (Exception e){
            Toast.makeText(getApplicationContext(), "Exception occured: " + e.toString(), Toast.LENGTH_SHORT).show();
        }
        return null;
    }
}