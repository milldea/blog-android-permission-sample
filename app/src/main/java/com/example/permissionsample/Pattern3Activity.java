package com.example.permissionsample;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class Pattern3Activity extends AppCompatActivity {
    private TextView permissionView;
    private boolean showSystemDialog = false;
    private final String UNAUTHORIZED = "UNAUTHORIZED";
    private final String AUTHORIZED = "AUTHORIZED";
    private final String PREFERENCE_NAME = "PERMISSION_SAMPLE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        final String NOT_DETERMINE = "NOT_DETERMINE";
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request);
        permissionView = findViewById(R.id.permission_request_state);
        permissionView.setText(NOT_DETERMINE);
        String[] permissions = {
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
        };
        if (checkPermissions(permissions)) {
            permissionView.setText(AUTHORIZED);
        } else {
            permissionView.setText(UNAUTHORIZED);
        }

        // 権限チェックを開始する
        findViewById(R.id.request_button).setOnClickListener(v -> {
            if (checkPermissions(permissions)) {
                new AlertDialog.Builder(Pattern3Activity.this)
                        .setMessage("許可されています")
                        .setPositiveButton(
                                "OK",
                                (dialogInterface, i) -> {})
                        .show();
            } else if (checkShouldShowRequestPermissionRationale(permissions)){
                new AlertDialog.Builder(Pattern3Activity.this)
                        .setMessage("このアプリでは、とても重要な理由により、詳細な位置情報を使います。")
                        .setPositiveButton("OK", (dialogInterface, i) -> {
                            requestPermission(permissions);
                        })
                        .show();
                showSystemDialog = true;
            } else {
                // SharedPreferences に権限状態を保持しておいて比較する（パターン3）
                SharedPreferences pref = getSharedPreferences(PREFERENCE_NAME, MODE_PRIVATE);
                boolean ACCESS_COARSE_LOCATION = pref.getBoolean(Manifest.permission.ACCESS_COARSE_LOCATION, false);
                boolean ACCESS_FINE_LOCATION = pref.getBoolean(Manifest.permission.ACCESS_FINE_LOCATION, false);
                showSystemDialog = ACCESS_COARSE_LOCATION == shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION) ||
                        ACCESS_FINE_LOCATION == shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION);

                requestPermission(permissions);
            }
        });
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] != PackageManager.PERMISSION_GRANTED &&
                grantResults[1] != PackageManager.PERMISSION_GRANTED) {
            // SharedPreferences に権限状態を保持しておいて比較する（パターン3）
            SharedPreferences pref = getSharedPreferences(PREFERENCE_NAME, MODE_PRIVATE);
            SharedPreferences.Editor editor = pref.edit();
            editor.putBoolean(Manifest.permission.ACCESS_COARSE_LOCATION, true);
            editor.putBoolean(Manifest.permission.ACCESS_FINE_LOCATION, true);
            editor.apply();
        }


        for (int result : grantResults) {
            // 1つでも拒否されていたら、要求する
            if (result != PackageManager.PERMISSION_GRANTED) {
                permissionView.setText(UNAUTHORIZED);
                if (showSystemDialog) {
                    showSystemDialog = false;
                    return;
                }
                new AlertDialog.Builder(Pattern3Activity.this)
                        .setMessage("位置情報が許可されていません")
                        .setPositiveButton("設定画面へ", (dialogInterface, i) -> openSettings())
                        .setNegativeButton("設定しない", (dialogInterface, i) -> {
                        })
                        .create()
                        .show();
                return;
            }
        }
        permissionView.setText(AUTHORIZED);
    }

    private void requestPermission(String[] permissions){
        requestPermissions(permissions,9999);
    }
    private void openSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", Pattern3Activity.this.getPackageName(), null);
        intent.setData(uri);
        startActivity(intent);
    }

    // 許可されていないものがないかチェックする
    private boolean checkPermissions(String[] permissions) {
        for (String permission : permissions) {
            if (checkSelfPermission(permission) !=
                    PackageManager.PERMISSION_GRANTED) {
                permissionView.setText(UNAUTHORIZED);
                return false;
            }
        }
        return true;
    }

    // ダイアログを表示すべきかどうか
    private boolean checkShouldShowRequestPermissionRationale(String[] permissions) {
        for (String permission : permissions) {
            if (shouldShowRequestPermissionRationale(permission)) {
                return true;
            }
        }
        return false;
    }

}