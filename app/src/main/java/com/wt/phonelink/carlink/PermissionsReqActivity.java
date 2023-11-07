package com.wt.phonelink.carlink;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.util.Arrays;

public abstract class PermissionsReqActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 0xEF;

    private String[] permissions = {};

    protected abstract String[] getPermissions();

    protected abstract void onPermissionsGranted();

    protected abstract void onPermissionsDenied();


    //    onPostCreate()主要用于框架使用（尽管你可以覆盖它）。
//    文档说它是在onStart()和onRestoreInstanceState()之后调用的
    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        permissions = getPermissions();
        if (!checkSelfPermissions(permissions)) {
            ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE);
        } else {
            onPermissionsGranted();
        }
    }

    @Override
    public final void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d(getClass().getSimpleName(), "onRequestPermissionsResult: " + Arrays.toString(grantResults));
        if (checkSelfPermissions(this.permissions)) {
            onPermissionsGranted();
        } else {
            onPermissionsDenied();
        }
    }

    private boolean checkSelfPermissions(String[] permissions) {
        if (permissions == null)
            return true;

        for (String permission : permissions)
            if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                Log.d(getClass().getSimpleName(), "checkSelfPermissions: " + permission + " does not granted");
                return false;
            }

        return true;
    }
}
