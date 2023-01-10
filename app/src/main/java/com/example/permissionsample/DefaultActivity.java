package com.example.permissionsample;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class DefaultActivity extends AppCompatActivity {
    private TextView permissionView;
    private final String UNAUTHORIZED = "UNAUTHORIZED";
    private final String AUTHORIZED = "AUTHORIZED";


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
                new AlertDialog.Builder(DefaultActivity.this)
                        .setMessage("許可されています")
                        .setPositiveButton(
                                "OK",
                                (dialogInterface, i) -> {})
                        .show();
            } else if (checkShouldShowRequestPermissionRationale(permissions)){
                new AlertDialog.Builder(DefaultActivity.this)
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

        for (int result : grantResults) {
            // 1つでも拒否されていたら、拒否にする
            if (result != PackageManager.PERMISSION_GRANTED) {
                permissionView.setText(UNAUTHORIZED);
                return;
            }
        }
        permissionView.setText(AUTHORIZED);
    }

    private void requestPermission(String[] permissions){
        requestPermissions(permissions,9999);
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