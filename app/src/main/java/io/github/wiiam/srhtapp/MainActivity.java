package io.github.wiiam.srhtapp;

import android.os.StrictMode;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.util.Linkify;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONException;
import java.io.File;
import java.io.IOException;
import io.github.wiiam.srhtapp.config.Config;


public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        try {
            Config.load(new File(getFilesDir(),"config.json"));
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(),e.toString(),Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Toast.makeText(getApplicationContext(),"test",Toast.LENGTH_LONG).show();
        TextView getApiKey = (TextView) findViewById(R.id.getApiKey);
        final EditText apiKey = (EditText) findViewById(R.id.apiKey);
        apiKey.setText(Config.getApiKey());
        getApiKey.setText("Your API key can be found at https://sr.ht under API Info");
        Linkify.addLinks(getApiKey, Linkify.ALL);
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
                    Toast.makeText(getApplicationContext(),"Your API key has been set",Toast.LENGTH_LONG).show();
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
    }
    /**
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    */
}
