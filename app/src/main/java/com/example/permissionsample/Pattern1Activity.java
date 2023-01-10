package com.example.permissionsample;

import static java.lang.Thread.sleep;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class Pattern1Activity extends AppCompatActivity {
    private TextView permissionView;
    // システムダイアログをオーバーレイで表示したか
    private boolean showSystemDialog = false;
    private final String UNAUTHORIZED = "UNAUTHORIZED";
    private final String AUTHORIZED = "AUTHORIZED";

    @Override
    public void onPause() {
        super.onPause();
        // OS のダイアログを表示していた場合（パターン1）
        Thread thread = new Thread(() -> {
            try {
                sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            ActivityManager activityManager = (ActivityManager)getApplicationContext()
                    .getSystemService(Activity.ACTIVITY_SERVICE);
            String topActivityName = activityManager
                    .getAppTasks()
                    .get(0)
                    .getTaskInfo()
                    .topActivity
                    .getClassName();
            if (!showSystemDialog) {
                String thisActivityName = Pattern1Activity.this.getClass().getName();
                showSystemDialog = !thisActivityName.equals(topActivityName);
            }
        });
        thread.start();
    }

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
                new AlertDialog.Builder(Pattern1Activity.this)
                        .setMessage("許可されています")
                        .setPositiveButton(
                                "OK",
                                (dialogInterface, i) -> {})
                        .show();
            } else if (checkShouldShowRequestPermissionRationale(permissions)){
                new AlertDialog.Builder(Pattern1Activity.this)
                        .setMessage("このアプリでは、とても重要な理由により、詳細な位置情報を使います。")
                        .setPositiveButton("OK", (dialogInterface, i) -> {
                            requestPermission(permissions);
                        })
                        .show();
            } else {
                requestPermission(permissions);
            }
        });
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // OS のダイアログを表示していた場合（パターン1）
        if (!showSystemDialog) {
            Log.d("permission sample", "判定パターン1");
        }

        for (int result : grantResults) {
            // 1つでも拒否されていたら、要求する
            if (result != PackageManager.PERMISSION_GRANTED) {
                permissionView.setText(UNAUTHORIZED);
                // OS のダイアログを表示していたらそのまま終わる
                if (showSystemDialog) {
                    showSystemDialog = false;
                    return;
                }
                new AlertDialog.Builder(Pattern1Activity.this)
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
        Uri uri = Uri.fromParts("package", Pattern1Activity.this.getPackageName(), null);
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