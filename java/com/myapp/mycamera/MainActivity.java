package com.myapp.mycamera;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraControl;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.extensions.HdrImageCaptureExtender;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;


public class MainActivity extends AppCompatActivity {

    // Used to load the 'native-lib' library on application startup.
//    static {
//        System.loadLibrary("native-lib");
//    }

    private final SSd sSdnet=new SSd();

    private final Executor executor = Executors.newSingleThreadExecutor();
    private final String[] REQUIRED_PERMISSIONS = new String[]{"android.permission.CAMERA", "android.permission.WRITE_EXTERNAL_STORAGE"};
    private final int REQUEST_CODE_PERMISSIONS = 1001;

    private Button btn;
    private Button btn2;
    private MyView2 myView2;
    private final int back=1;
    private ImageView img;
    private TextView txt;

    Mypreview mPreviewView;

    private final int[] pics= {R.drawable.a1,R.drawable.a2,R.drawable.a3};
    private int pic_id=0;
    private Configuration mConfiguration;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //创建弹窗
        AlertDialog alertDialog=new AlertDialog.Builder(MainActivity.this).create();
        alertDialog.setCancelable(false);
        alertDialog.setTitle("友情提示");
        alertDialog.setMessage("在应用中心中有更多好玩有趣的app，欢迎前往下载体验。使用过程中如遇到bug可将其发送至邮箱1735375343@qq.com");
        alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "知道了",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.i("aa","知道了");
                    }
                });
        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "应用中心",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent=new Intent(MainActivity.this,DownLoad.class);
                        startActivity(intent);
                    }
                });
        alertDialog.show();

        boolean init=sSdnet.Init(getAssets());

        Log.i("aa",""+init);

        //获取设置的配置信息
        mConfiguration = this.getResources().getConfiguration();



        mPreviewView = findViewById(R.id.previewView);
        img=findViewById(R.id.image);
//        Bitmap bb=BitmapFactory.decodeResource(getResources(),R.drawable.pic);
//
//        SSd.Obj[] outs=sSdnet.Detect(bb,false);
//        Log.i("aa",outs+"");


        txt=findViewById(R.id.txt);

        if(allPermissionsGranted()){
            startCamera(); //start camera if permission has been granted by user
        } else{
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }

        btn=findViewById(R.id.btn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (camea_id==0){
                    camea_id=1;
                }else camea_id=0;
                bindPreview(cameraProvider);
            }
        });

        myView2=findViewById(R.id.myview);
        btn2=findViewById(R.id.btn2);
        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pic_id+=1;
                if (pic_id>2){
                    pic_id=0;
                }
                Bitmap bitmap2= BitmapFactory.decodeResource(getResources(),pics[pic_id]);
                myView2.bitmap=bitmap2;

            }
        });

    }

    private ProcessCameraProvider cameraProvider;
    private void startCamera() {

        final ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(new Runnable() {
            @Override
            public void run() {
                try {
                    cameraProvider = cameraProviderFuture.get();
                    bindPreview(cameraProvider);


                } catch (ExecutionException | InterruptedException e) {
                    // No errors need to be handled for this Future.
                    // This should never be reached.
                }
            }
        }, ContextCompat.getMainExecutor(this));
    }

    long t1=0;
    long t2=0;
    private int camea_id=1;
    private Bitmap bmp;
    private CameraControl cameraControl;
    private boolean su;
    private boolean heng;
    void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {

        Preview preview = new Preview.Builder()
                .build();

        @SuppressLint("WrongConstant") CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(camea_id)
                .build();

        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .build();
        //imageAnalysis.setAnalyzer(cameraExecutor, new MyAnalyzer());
        imageAnalysis.setAnalyzer(executor, new ImageAnalysis.Analyzer() {
            @Override
            public void analyze(@NonNull ImageProxy image) {
                runOnUiThread(() ->{

                    //获取当下屏幕状态
                    int ori = mConfiguration.orientation; //获取屏幕方向
                    if (ori == Configuration.ORIENTATION_LANDSCAPE) {
                        //横屏
                        heng=true;
                        su=false;
                    } else if (ori == Configuration.ORIENTATION_PORTRAIT) {
                        //竖屏
                        su=true;
                        heng=false;
                    }


                    t1=t2;
                    t2=System.currentTimeMillis();
                    long fps=1000/(t2-t1);
                    txt.setText("FPS:"+fps);

                    int rotationDegrees = image.getImageInfo().getRotationDegrees();
//                    Log.i("aa","angle1="+rotationDegrees);
                    //旋转角度
                    int rotation = mPreviewView.getDisplay().getRotation();
//                    Log.i("aa","angle2="+rotation);



                    //yuv图像数据转bitmap
                    ImageProxy.PlaneProxy[] planes = image.getPlanes();

                    //cameraX 获取yuv
                    ByteBuffer yBuffer = planes[0].getBuffer();
                    ByteBuffer uBuffer = planes[1].getBuffer();
                    ByteBuffer vBuffer = planes[2].getBuffer();

                    int ySize = yBuffer.remaining();
                    int uSize = uBuffer.remaining();
                    int vSize = vBuffer.remaining();

                    byte[] nv21 = new byte[ySize + uSize + vSize];

                    yBuffer.get(nv21, 0, ySize);
                    vBuffer.get(nv21, ySize, vSize);
                    uBuffer.get(nv21, ySize + vSize, uSize);
                    //获取yuvImage
                    YuvImage yuvImage = new YuvImage(nv21, ImageFormat.NV21, image.getWidth(), image.getHeight(), null);
                    //输出流
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    //压缩写入out
                    yuvImage.compressToJpeg(new Rect(0, 0, yuvImage.getWidth(), yuvImage.getHeight()), 50, out);
                    //转数组
                    byte[] imageBytes = out.toByteArray();
                    //生成bitmap
                    Bitmap bmp = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);

                    //旋转bitmap
                    Bitmap rotateBitmap=null;
                    if (camea_id==1 && su){
                        rotateBitmap = rotateBitmap(bmp, 90);
                    }else if(camea_id==0 && su){
                        rotateBitmap = rotateBitmap(bmp, 270);
                    }else if(camea_id==1 && heng){
                        rotateBitmap=bmp;
                    }else {
                        rotateBitmap=rotateBitmap(bmp, 0);
                    }


                    Bitmap bmp2=rotateBitmap.copy(Bitmap.Config.ARGB_8888, true);

                    SSd.Obj[] outcome=sSdnet.Detect(bmp2,false);
                    if(outcome!=null){
                        ArrayList<Data> datas=new ArrayList<>();
                        for (int i=0;i<outcome.length;i++){
                            Data data=new Data();

                            float x= outcome[i].x*(float)myView2.getWidth();
                            float y= outcome[i].y*(float)myView2.getHeight();
                            float w=outcome[i].w*(float)myView2.getWidth();
                            float h=outcome[i].h*(float)myView2.getHeight();
                            data.X=(int)x;
                            data.Y=(int)y;
                            data.L=(int)((w+h)/2);
                            datas.add(data);
                            myView2.points=datas;

                            Log.i("aa","ww="+x+" "+y+" "+w+" "+h);
                        }

                    }else {
                        ArrayList<Data> datas=new ArrayList<>();
                        myView2.points=datas;
                    }


//                    Canvas canvas = new Canvas( bmp2 );
//                    Paint paint = new Paint();
//                    paint.setColor( Color.RED );
//                    paint.setStrokeWidth( 10 );
//                    canvas.drawRect( 20,40,200,400,paint );
//                    img.setImageBitmap(bmp2);
                    //关闭
                    image.close();

                });


            }
        });

        ImageCapture.Builder builder = new ImageCapture.Builder();

        //Vendor-Extensions (The CameraX extensions dependency in build.gradle)
        HdrImageCaptureExtender hdrImageCaptureExtender = HdrImageCaptureExtender.create(builder);

        // Query if extension is available (optional).
        if (hdrImageCaptureExtender.isExtensionAvailable(cameraSelector)) {
            // Enable the extension if available.
            hdrImageCaptureExtender.enableExtension(cameraSelector);
        }

        final ImageCapture imageCapture = builder
                .setTargetRotation(this.getWindowManager().getDefaultDisplay().getRotation())
                .build();

        preview.setSurfaceProvider(mPreviewView.createSurfaceProvider());

        try {
            cameraProvider.unbindAll();
            Camera camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis, imageCapture);
            cameraControl=camera.getCameraControl();
            mPreviewView.cameraControl=cameraControl;
//            cameraControl.setLinearZoom(mPreviewView.rate);

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private Bitmap rotateBitmap(Bitmap origin, float alpha) {
        if (origin == null) {
            return null;
        }
        int width = origin.getWidth();
        int height = origin.getHeight();
        Matrix matrix = new Matrix();
        matrix.setRotate(alpha);
        if (camea_id==0){
            matrix.postScale(-1,1);
        }
        // 围绕原地进行旋转
        Bitmap newBM = Bitmap.createBitmap(origin, 0, 0, width, height, matrix, false);
        if (newBM.equals(origin)) {
            return newBM;
        }
        origin.recycle();
        return newBM;
    }

    //获取权限函数
    private boolean allPermissionsGranted(){
        for(String permission : REQUIRED_PERMISSIONS){
            if(ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED){
                return false;
            }
        }
        return true;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if(requestCode == REQUEST_CODE_PERMISSIONS){
            if(allPermissionsGranted()){
                startCamera();
            } else{
                Toast.makeText(this, "Permissions not granted by the user.", Toast.LENGTH_SHORT).show();
                this.finish();
            }
        }
    }

    public class Data{
        int X;
        int Y;
        int L;
    }
}