package com.example.accball;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements SensorEventListener, SurfaceHolder.Callback {

    //m = メンバー変数
    SensorManager mSensorManager;
    Sensor mAccSensor;

    SurfaceHolder mHolder;
    //サーフィスビューの幅
    int mSurfaceWidth;
    //サーフィスビューの縦
    int mSurfaceHeight;

    //半径
    static final float RADIUS = 150.0f;
    static final int DIA = (int)RADIUS * 2;
    //移動量
    static final float COEF = 100.0f;

    //x
    float mBallX;
    float mBallY;
    float mVX;
    float mVY;

    //加速した時間
    long mT0;

    //ボールの画像
    Bitmap mBallBitMap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_main);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        mAccSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        SurfaceView surfaceView = (SurfaceView) findViewById(R.id.surfaceView);
        mHolder = surfaceView.getHolder();
        mHolder.addCallback(this);

        //サーフィスビューの透明か
        mHolder.setFormat(PixelFormat.TRANSLUCENT);
        surfaceView.setZOrderOnTop(true);

        //ボールの画像を用意する
        Bitmap ball = BitmapFactory.decodeResource(getResources(),R.drawable.ball);
        mBallBitMap = Bitmap.createScaledBitmap(ball,DIA,DIA,false);
    }

    //加速度センサーの値に変化があった時に呼ばれる
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if(sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
            float x = -sensorEvent.values[0];
            float y = sensorEvent.values[1];

            //時間tを求める
            if(mT0 == 0){
                mT0 = sensorEvent.timestamp;
                return;
            }
            //経過時間
            float t = sensorEvent.timestamp - mT0;
            mT0 = sensorEvent.timestamp;
            //ナノに変換
            t = t/1000000000.0f;

            //移動距離を求める
            float dx = (mVX * t) + (x*t*t/2.0f);
            float dy = (mVY * t) + (y * t * t / 2.0f);

            //移動距離からボールの今の位置を更新
            mBallX = mBallX + dx *COEF;
            mBallY = mBallY + dy * COEF;

            //現在のボールの移動速度を更新
            mVX = mVX + (x*t);
            mVY = mVY + (y*t);

            //ボールが画面の外に出ないようにする処理
            if(mBallX - RADIUS < 0 && mVX < 0){
                mVX -= mVX / 1.5f;
                mBallX = RADIUS;
            }else if(mBallX + RADIUS > mSurfaceWidth && mVX > 0){
                mVX -= mVX / 1.5f;
                mBallX = mSurfaceWidth - RADIUS;
            }

            if(mBallY - RADIUS < 0 && mVY < 0){
                mVY -= mVY / 1.5f;
                mBallY = RADIUS;
            }else if(mBallY + RADIUS > mSurfaceHeight && mVY > 0){
                mVY -= mVY / 1.5f;
                mBallY = mSurfaceHeight - RADIUS;
            }

            //加速度から算出したボールの現在位置で、ボールをキャンパスに描画し直す
            drawCanvas();




        }

    }

    private void drawCanvas() {
        //画面にボールを表示する

        Canvas c = mHolder.lockCanvas();
        c.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        Paint paint = new Paint();
        c.drawBitmap(mBallBitMap,mBallX-RADIUS,mBallY - RADIUS,paint);

        mHolder.unlockCanvasAndPost(c);
    }

    //加速度センサーの精度が変更された時に呼ばれる
    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    protected void onResume() {
        super.onResume();


    }

    @Override
    protected void onPause() {
        super.onPause();


    }

    //作成された時
    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        mSensorManager.registerListener(this,mAccSensor,SensorManager.SENSOR_DELAY_GAME);

    }

    //変更された時
    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        mSurfaceWidth = i1;
        mSurfaceHeight = i2;

        //ボールの最初の位置を指定する
        mBallX = mSurfaceWidth / 2;
        mBallY = mSurfaceHeight / 2;

        //速度、時間を初期化
        mVX = 0;
        mVY = 0;
        mT0 = 0;
    }

    //消去された時
    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        mSensorManager.unregisterListener(this);

    }
}
