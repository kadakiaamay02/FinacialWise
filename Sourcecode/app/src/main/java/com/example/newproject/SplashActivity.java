package com.example.newproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {
    private Button startAppButton;

//---- This can act as a splash page if needed ----//
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        //Disable ActionBar
        getSupportActionBar().hide();

        startAppButton = findViewById(R.id.startButton);

        startAppButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //We go to the first activity of the app, login activity
                startActivity(new Intent(SplashActivity.this, LoginActivity.class));
            }
        });
    }
}