package com.myapp.mycamera;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;

import java.util.ArrayList;

public class MyView2 extends SurfaceView implements Runnable, SurfaceHolder.Callback {


    private SurfaceHolder mHolder; // 用于控制SurfaceView
    private Thread t; // 声明一条线程
    private boolean flag; // 线程运行的标识，用于控制线程
    private Canvas mCanvas; // 声明一张画布
    private Paint p; // 声明一支画笔
    private final int x = -1;
    private final int y = -1;
    private final int r = 500; // 圆的坐标和半径

    public Bitmap bitmap;
    public float rate=1f;

    public ArrayList<MainActivity.Data> points=new ArrayList<>();

    public MyView2(Context context) {
        super(context);
        Log.i("aa","111");
    }

    int ww,hh;

    public MyView2(Context context, AttributeSet attrs) {
        super(context, attrs);
        mHolder = getHolder(); // 获得SurfaceHolder对象
        mHolder.addCallback(this); // 为SurfaceView添加状态监听

        //设置背景透明
        setZOrderOnTop(true);
        mHolder.setFormat(PixelFormat.TRANSLUCENT);


        p = new Paint(); // 创建一个画笔对象
        p.setColor(Color.WHITE); // 设置画笔的颜色为白色

        p.setStyle(Paint.Style.STROKE);
        //设置描边宽度
        p.setStrokeWidth(8);

        setFocusable(true); // 设置焦点


    }

    @Override

    public void surfaceCreated(@NonNull SurfaceHolder holder) {

        bitmap= BitmapFactory.decodeResource(getResources(),R.drawable.a1);
        t = new Thread(this); // 创建一个线程对象
        flag = true; // 把线程运行的标识设置成true
        t.start(); // 启动线程

    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
        flag=false;

    }

    @Override
    public void run() {
        while (flag){
            //[184, 368, 552, 736, 920, 1104, 1288, 1472]

            try {
                doDraw();
//                long drawEndTime = System.currentTimeMillis();
                // 休眠时间为每帧动画持续时间减去绘制一帧所耗时间
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }


    }

    private final int s=0;
    private float angle=0.f;
    public void doDraw() {
        //角度
        angle+=10;


        mCanvas = mHolder.lockCanvas(); // 获得画布对象，开始对画布画画.

        Log.i("aa",bitmap.getWidth()+"  "+bitmap.getHeight());
        //mCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        //mCanvas.drawRGB(0, 0, 0); // 把画布填充为黑色
        //清除canvas画布
        //  方法一：
        mCanvas.drawColor(0,PorterDuff.Mode.CLEAR);

//        Matrix matrix=new Matrix();
//        //            //图像平移
//
//        matrix.setTranslate(0,0);
//        //matrix.preScale(0.5f,0.5f,bitmap.getWidth()/2,bitmap.getHeight()/2);
////        matrix.preScale(0.5f,0.5f);
//        //图像旋转角度和旋转中心
//        matrix.preRotate(angle,bitmap.getWidth()/2,bitmap.getHeight()/2);
//        mCanvas.drawBitmap(bitmap,matrix,null);





        try{

            for (MainActivity.Data p:points){
                int w=bitmap.getWidth();
                int h=bitmap.getHeight();

                rate=p.L*2f/w;

//                rate+=(float) p.L/500f;

                Log.i("aa","pll="+p.L);

                Matrix matrix = new Matrix();
                //图像缩放

//            //图像平移
                matrix.setTranslate(p.X-w/2,p.Y-h/2);
                matrix.preScale(rate,rate,w/2,h/2);
                //图像旋转角度和旋转中心
                matrix.preRotate(angle,w/2,h/2);

                mCanvas.drawBitmap(bitmap,matrix,null);

            }

        } catch (Exception e) {
            e.printStackTrace();
        }


        //截取图像区域
        //Rect srcRect=new Rect(s*(bitmap.getWidth()/8),0,(s+1)*bitmap.getWidth()/8,bitmap.getHeight());
        //屏幕绘画区域
//        bb-=0.01;
        //Rect dstRect=new Rect(500,0,(int)(bitmap.getWidth()/8*bb)+500,(int)(bitmap.getHeight()*bb));

//        s+=1;
//        if (s==8){
//            s=0;
//        }

        //Log.i("aa",""+bitmap.getWidth()+" "+bitmap.getHeight());
        //mCanvas.rotate(45);//顺时针旋转画布

        // 旋转图片 动作
//        Matrix matrix = new Matrix();
//        matrix.postScale(0.5f, 0.5f);

        //图像平移
        //matrix.setTranslate(bitmap.getWidth()/2,bitmap.getHeight()/2);
        //图像旋转
//        matrix.preRotate(bb,bitmap.getWidth()/2,bitmap.getHeight()/2);

        // 创建新的图片
//        Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0,
//                bitmap.getWidth(), bitmap.getHeight(), matrix, true);
//
//        bb+=5;


//        Rect srcRect=new Rect(0,0,bitmap.getWidth(),bitmap.getHeight());
//        Rect dstcRect=new Rect(0,0,bitmap.getWidth(),bitmap.getHeight());


        //mCanvas.rotate(bb,bitmap.getWidth()/2,bitmap.getHeight()/2);

//        mCanvas.drawBitmap(resizedBitmap,srcRect,dstcRect,null);
//        mCanvas.drawBitmap(bitmap,matrix,null);



//        //图像平移
//        matrix.setTranslate(bitmap.getWidth()/2,bitmap.getHeight()/2);
//        //图像旋转角度和旋转中心
//        matrix.preRotate(bb,bitmap.getWidth()/2,bitmap.getHeight()/2);
//        //图像缩放
//        matrix.postScale(1.5f,1.5f);
//
//        mCanvas.drawBitmap(bitmap,matrix,null);

//        Rect srcRect2=new Rect(0,0,bitmap.getWidth(),bitmap.getHeight());
//        Rect dstcRect2=new Rect(500,500,bitmap.getWidth()+500,bitmap.getHeight()+500);




        mHolder.unlockCanvasAndPost(mCanvas); // 完成画画，把画布显示在屏幕上
    }

//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
//        p.setARGB((int) (Math.random()*255),(int)(Math.random()*255),(int)(Math.random()*255),(int)(Math.random()*255));
//
//        if (event.getAction()==MotionEvent.ACTION_DOWN){
//            x = (int) event.getX(); // 获得屏幕被触摸时对应的X轴坐标
//            y = (int) event.getY(); // 获得屏幕被触摸时对应的Y轴坐标
//            Point neepoint=new Point(x,y);
//            points.add(neepoint);
//        }
//
//        return false;
//    }
}
