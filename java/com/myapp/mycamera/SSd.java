package com.myapp.mycamera;

import android.content.res.AssetManager;
import android.graphics.Bitmap;

public class SSd {

    public native boolean Init(AssetManager mgr);
    public class Obj
    {
        public float x;
        public float y;
        public float w;
        public float h;
        public String label;
        public float prob;
    }
    public native Obj[] Detect(Bitmap bitmap, boolean use_gpu);

    static {
        System.loadLibrary("native-lib");
    }
}
