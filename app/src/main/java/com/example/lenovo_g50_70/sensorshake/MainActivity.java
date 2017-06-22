package com.example.lenovo_g50_70.sensorshake;

import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private static final String TAG = "MainActivity";
    protected static final int START_SHAKE = 0x1;
    protected static final int AGAIN_SHAKE = 0x2;
    protected static final int END_SHAKE = 0x3;

    //传感器管理者
    protected SensorManager mSensorManager;
    //加速度传感器
    protected Sensor mAccelerometerSensor;
    //是否进行了摇一摇
    protected boolean isShake = false;
    //摇一摇音乐效果
    public SoundPool mSoundPool;
    //摇一摇震动效果
    public Vibrator mVibrator;

    //上半布局
    protected LinearLayout mTopLayout;
    //下半布局
    protected LinearLayout mBottomLayout;

    protected ImageView mTopLine;
    protected ImageView mBottomLine;

    protected MyHandler mHandler;
    protected int mWeiChatAudio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //设置只竖屏
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        initShakes();
        initViews();
    }

    private void initViews() {
        mTopLayout = (LinearLayout) findViewById(R.id.main_linear_top);
        mBottomLayout = (LinearLayout) findViewById(R.id.main_linear_bottom);
        mTopLine = (ImageView) findViewById(R.id.main_shake_top_line);
        mBottomLine = (ImageView) findViewById(R.id.main_shake_bottom_line);

        //默认状态，线条不显示
        mTopLine.setVisibility(View.GONE);
        mBottomLine.setVisibility(View.GONE);
    }

    private void initShakes() {
        mSoundPool = new SoundPool(1, AudioManager.STREAM_SYSTEM, 5);
        mWeiChatAudio = mSoundPool.load(this, R.raw.weichat_audio, 1);
        //获取震动服务
        mVibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        mHandler = new MyHandler(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        //获取SensorManager负责管理传感器
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (mSensorManager != null) {
            //获取加速度传感器
            mAccelerometerSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            if (mAccelerometerSensor != null) {
                //注册传感器
                mSensorManager.registerListener((SensorEventListener) this, mAccelerometerSensor, SensorManager.SENSOR_DELAY_UI);
            }
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        //反注册传感器
        if (mSensorManager != null) {
            mSensorManager.unregisterListener((SensorEventListener) this);
        }
    }

    /**
     * 开启 摇一摇动画
     *
     * @param isBack
     */
    public void startAnimation(boolean isBack) {
        //动画坐标移动的位置的类型是相对自己的
        int type = Animation.RELATIVE_TO_SELF;
        float topFromY;
        float topToY;
        float bottomFromY;
        float bottmToY;

        if (isBack) {
            topFromY = -0.5f;
            topToY = 0;
            bottomFromY = 0.5f;
            bottmToY = 0;
        } else {
            topFromY = 0;
            topToY = -0.5f;
            bottomFromY = 0;
            bottmToY = 0.5f;
        }
        //上面图片的动画效果
        TranslateAnimation topAnim = new TranslateAnimation(type, 0, type, 0, type, topFromY, type, topToY);
        topAnim.setDuration(500);
        //动画终止时停留在最后一帧,不然会回到没有执行之前的状态
        topAnim.setFillAfter(true);

        //下面图片的动画效果
        TranslateAnimation bottomAnim = new TranslateAnimation(type, 0, type, 0, type, bottomFromY, type, bottmToY);
        bottomAnim.setDuration(500);
        bottomAnim.setFillAfter(true);

        //动画回来效果结束后，需要将两条线隐藏
        if (isBack) {
            bottomAnim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    mTopLine.setVisibility(View.GONE);
                    mBottomLine.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
        }

        //执行动画
        mTopLayout.startAnimation(topAnim);
        mBottomLayout.startAnimation(bottomAnim);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        //获取传感器的类型
        int type = event.sensor.getType();

        //如果是加速度传感器
        if (type == Sensor.TYPE_ACCELEROMETER) {
            //获取三个方向的值
            float[] values = event.values;
            //左右方向
            float x = values[0];
            //上下方向
            float y = values[1];
            //前后方向
            float z = values[2];

            //获取各方向变动的绝对值
            if ((Math.abs(x)) > 17 || (Math.abs(y)) > 17 || (Math.abs(z)) > 17) {
                isShake = true;

                //实现摇动逻辑，后进行震动
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            //开始震动 发出提示音 展示动画效果
                            mHandler.obtainMessage(START_SHAKE).sendToTarget();
                            Thread.sleep(1000);
                            //再来一次震动提示
                            mHandler.obtainMessage(AGAIN_SHAKE).sendToTarget();
                            Thread.sleep(1000);
                            //摇动结束
                            mHandler.obtainMessage(END_SHAKE).sendToTarget();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}
