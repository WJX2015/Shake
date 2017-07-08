package com.example.lenovo_g50_70.sensorshake;

import android.os.Handler;
import android.os.Message;
import android.view.View;

import java.lang.ref.WeakReference;

import static com.example.lenovo_g50_70.sensorshake.MainActivity.AGAIN_SHAKE;
import static com.example.lenovo_g50_70.sensorshake.MainActivity.END_SHAKE;
import static com.example.lenovo_g50_70.sensorshake.MainActivity.START_SHAKE;

/**
 * Created by lenovo-G50-70 on 2017/6/12.
 */

public class MyHandler extends Handler {
    private WeakReference<MainActivity> mReference;
    private MainActivity mActivity;

    public MyHandler(MainActivity activity) {
        mReference = new WeakReference<MainActivity>(activity);
        if (mReference != null) {
            mActivity = mReference.get();
        }
    }

    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        switch (msg.what) {
            case START_SHAKE:
                mActivity.mVibrator.vibrate(300);
                //发出提示音
                //mActivity.mSoundPool.play(mActivity.mWeiChatAudio,1,1,0,0,1);
                mActivity.mTopLine.setVisibility(View.VISIBLE);
                mActivity.mBottomLine.setVisibility(View.VISIBLE);
                //两张图片分散开的动画
                mActivity.startAnimation(false);
                break;
            case AGAIN_SHAKE:
                mActivity.mVibrator.vibrate(300);
                break;
            case END_SHAKE:
                //展示上下两种图片回来的效果
                mActivity.startAnimation(true);
                mActivity.isShake = false;
                break;
        }
    }
}
