package io.github.wiiam.srhtapp;

import android.app.Activity;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;

import java.io.File;
import java.io.IOException;

import io.github.wiiam.srhtapp.R;
import io.github.wiiam.srhtapp.config.Config;

/**
 * Created by william on 10/07/15.
 */
public class ApiSetup extends Activity{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.api_setup);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        try {
            Config.load(new File(getFilesDir(), "config.json"));
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //Toast.makeText(getApplicationContext(),"test",Toast.LENGTH_LONG).show();
        ImageView bgimage = (ImageView)findViewById(R.id.bgimage);
        int imageNumber = (int)Math.ceil(Math.random() * 9);
        int height = 0;
        switch (imageNumber){
            case 1: bgimage.setImageResource(R.drawable.one);break;
            case 2: bgimage.setImageResource(R.drawable.two);break;
            case 3: bgimage.setImageResource(R.drawable.three);break;
            case 4: bgimage.setImageResource(R.drawable.four); break;
            case 5: bgimage.setImageResource(R.drawable.five); break;
            case 6: bgimage.setImageResource(R.drawable.six); break;
            case 7: bgimage.setImageResource(R.drawable.seven); break;
            case 8: bgimage.setImageResource(R.drawable.eight); break;
            case 9: bgimage.setImageResource(R.drawable.nine); break;
        }
        Log.d("IMAGEID", "" + imageNumber);

        //bgimage.setImageResource(R.drawable.one);
        findViewById(R.id.apiKeyTitle).bringToFront();
        findViewById(R.id.title).bringToFront();
        TextView getApiKey = (TextView) findViewById(R.id.getApiKey);
        //getApiKey.setText(Html.fromHtml("<a href =\"https://sr.ht\">sr.ht</a>"));
        //getApiKey.setText(Html.fromHtml("wow u r 10th user! click <a href =\"https://sr.ht\">here</a>, to claim ur prize!"));
        getApiKey.setMovementMethod(LinkMovementMethod.getInstance());
        getApiKey.bringToFront();

        final EditText apiKey = (EditText) findViewById(R.id.apiKey);
        apiKey.bringToFront();
        apiKey.setText(Config.getApiKey());
        Button set = (Button)findViewById(R.id.set);
        set.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                final EditText akey = apiKey;
                String key = akey.getText().toString().trim();
                if(key == "" || key == null){
                    Toast.makeText(getApplicationContext(),"No API key specified",Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(getApplicationContext(),"Your API key has been set",Toast.LENGTH_SHORT).show();
                    try{
                        Config.setApiKey(key);
                    }
                    catch (NullPointerException e){
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(), "No API key specified", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        Button clear = (Button)findViewById(R.id.clear);
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                apiKey.setText("");
            }
        });
    }
}
