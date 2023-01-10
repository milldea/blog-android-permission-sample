package com.example.permissionsample;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.default_pattern).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, DefaultActivity.class);
            startActivity(intent);
        });
        findViewById(R.id.pattern1).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, Pattern1Activity.class);
            startActivity(intent);
        });
        findViewById(R.id.pattern2).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, Pattern2Activity.class);
            startActivity(intent);
        });
        findViewById(R.id.pattern3).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, Pattern3Activity.class);
            startActivity(intent);
        });
        findViewById(R.id.pattern4).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, Pattern4Activity.class);
            startActivity(intent);
        });
    }



}