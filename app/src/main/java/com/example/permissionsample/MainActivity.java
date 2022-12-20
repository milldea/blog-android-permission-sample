package com.example.permissionsample;

import static java.lang.Thread.sleep;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private TextView permissionView;
    // システムダイアログをオーバーレイで表示したか
    private boolean showSystemDialog = false;
    private final String UNAUTHORIZED = "UNAUTHORIZED";
    private final String AUTHORIZED = "AUTHORIZED";
    private final String PREFERENCE_NAME = "PERMISSION_SAMPLE";
    private long requestTime = 0;

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
                String thisActivityName = MainActivity.this.getClass().getName();
                showSystemDialog = !thisActivityName.equals(topActivityName);
            }
        });
        thread.start();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        final String NOT_DETERMINE = "NOT_DETERMINE";
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        permissionView = findViewById(R.id.permission_state);
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
        findViewById(R.id.button).setOnClickListener(v -> {
            if (checkPermissions(permissions)) {
                new AlertDialog.Builder(MainActivity.this)
                        .setMessage("許可されています")
                        .setPositiveButton(
                                "OK",
                                (dialogInterface, i) -> {})
                        .show();
            } else if (checkShouldShowRequestPermissionRationale(permissions)){
                new AlertDialog.Builder(MainActivity.this)
                        .setMessage("このアプリでは、とても重要な理由により、詳細な位置情報を使います。")
                        .setPositiveButton("OK", (dialogInterface, i) -> {
                            requestPermission(permissions);
                        })
                        .show();
            } else {
                // SharedPreferences に権限状態を保持しておいて比較する（パターン3）
                SharedPreferences pref = getSharedPreferences(PREFERENCE_NAME, MODE_PRIVATE);
                boolean ACCESS_COARSE_LOCATION = pref.getBoolean(Manifest.permission.ACCESS_COARSE_LOCATION, false);
                boolean ACCESS_FINE_LOCATION = pref.getBoolean(Manifest.permission.ACCESS_FINE_LOCATION, false);
                if (ACCESS_COARSE_LOCATION != shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION) &&
                        ACCESS_FINE_LOCATION != shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)
                ) {
                    Log.d("permission sample", "判定パターン3");
                }

                requestPermission(permissions);
            }
        });
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // OS のダイアログを表示していた場合（パターン1）
        if (!showSystemDialog) {
            Log.d("permission sample", "判定パターン2");
        }

        // 100 ミリ秒いないなら、システムダイアログが出ていないと判定（パターン2）
        if (System.currentTimeMillis() - requestTime < 100) {
            Log.d("permission sample", "判定パターン2");
        }
        if (grantResults[0] != PackageManager.PERMISSION_GRANTED &&
                grantResults[1] != PackageManager.PERMISSION_GRANTED) {
            // SharedPreferences に権限状態を保持しておいて比較する（パターン3）
            SharedPreferences pref = getSharedPreferences(PREFERENCE_NAME, MODE_PRIVATE);
            SharedPreferences.Editor editor = pref.edit();
            editor.putBoolean(Manifest.permission.ACCESS_COARSE_LOCATION, true);
            editor.putBoolean(Manifest.permission.ACCESS_FINE_LOCATION, true);
            editor.apply();

            // 次に出ない状態かどうかを見分ける（パターン4）
            // （今回出なかったどうかの判定ではない）
            if (!shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION) &&
                    !shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)
            ) {
                Log.d("permission sample", "判定パターン4");
            }
        }


        for (int result : grantResults) {
            // OS のダイアログを表示していたら抜ける
            if (showSystemDialog) {
                showSystemDialog = false;
                break;
            }
            // 1つでも拒否されていたら、要求する
            if (result != PackageManager.PERMISSION_GRANTED) {
                new AlertDialog.Builder(MainActivity.this)
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
        requestTime = System.currentTimeMillis();
        requestPermissions(permissions,9999);
    }
    private void openSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", MainActivity.this.getPackageName(), null);
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