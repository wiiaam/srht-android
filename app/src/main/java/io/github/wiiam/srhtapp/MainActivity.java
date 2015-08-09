package io.github.wiiam.srhtapp;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;


public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent apisetup = new Intent(getApplicationContext(),ApiSetup.class);
        startActivity(apisetup);
    }
}
