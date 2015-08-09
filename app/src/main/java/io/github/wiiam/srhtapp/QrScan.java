package io.github.wiiam.srhtapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import javax.xml.transform.Result;

import io.github.wiiam.srhtapp.config.Config;
import me.dm7.barcodescanner.zxing.ZXingScannerView;

/**
 * Created by william on 9/08/15.
 */
public class QrScan extends Activity implements ZXingScannerView.ResultHandler{
    private ZXingScannerView mScannerView;
    private static final String DEBUG_TAG = "QRSCANNER";

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        mScannerView = new ZXingScannerView(this);   // Programmatically initialize the scanner view
        setContentView(mScannerView);                // Set the scanner view as the content view
    }

    @Override
    public void onResume() {
        super.onResume();
        mScannerView.setResultHandler(this); // Register ourselves as a handler for scan results.
        mScannerView.startCamera();          // Start camera on resume
    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();           // Stop camera on pause
    }

    @Override
    public void handleResult(com.google.zxing.Result result) {
        Log.v(DEBUG_TAG, result.getText()); // Prints scan results
        Log.v(DEBUG_TAG, result.getBarcodeFormat().toString()); // Prints the scan format (qrcode, pdf417 etc.)
        String resultString = result.getText();
        try {
            String[] splitresult = resultString.split(":");
            if(splitresult[0].equals("srht") || splitresult.length == 3){
                Log.v(DEBUG_TAG,"URL: " + splitresult[1]);
                Log.v(DEBUG_TAG,"KEY: " + splitresult[2]);
                Config.setUrl(splitresult[1]);
                Config.setApiKey(splitresult[2]);
                Toast.makeText(getApplicationContext(), "Your settings have been saved", Toast.LENGTH_SHORT);
            }
            else throw new Exception("Not a srht link");
        }
        catch(Exception e){
            Log.v(DEBUG_TAG,"QR ADDRESS PARSE EXCEPTION " + e.toString());
            Toast.makeText(getApplicationContext(), "Invalid QR Barcode", Toast.LENGTH_SHORT);
        }
        finish();
    }
}
