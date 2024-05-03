package com.example.myapplication.utils;

import android.Manifest;

public class Constant {
    public static final int REQUEST_CODE_PERMISSIONS = 1;
    public static final int PICK_FROM_GALLERY = 2;
    public static final int RESULT_LOAD_IMG = 3;
    public static final String[] REQUIRED_PERMISSIONS = new String[]{Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_EXTERNAL_STORAGE};
}
