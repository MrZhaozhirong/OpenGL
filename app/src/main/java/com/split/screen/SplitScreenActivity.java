package com.split.screen;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ConfigurationInfo;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.langtao.device.DeviceStatusManager;
import com.langtao.device.FishEyeDeviceDataSource;
import com.langtao.device.SDKinitUtil;
import com.langtao.device.YUVFrame;
import com.pixel.opengl.R;

import java.lang.ref.WeakReference;

import static com.langtao.device.DeviceStatusManager.DEV_ID;

/**
 * Created by zzr on 2017/8/10.
 */

public class SplitScreenActivity extends Activity  {


    private SafeHandler safeHandler = new SafeHandler(this);
    static class SafeHandler extends Handler {
        WeakReference<SplitScreenActivity> mActivity;

        public SafeHandler(SplitScreenActivity activity){
            mActivity = new WeakReference<SplitScreenActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            SplitScreenActivity activity = mActivity.get();
            switch (msg.what) {
                case 101:
                    if(activity!=null){
                        activity.initOpenGL();
                    }
                    break;
                default:
                    break;
            }
        }
    }

    private FishEyeDeviceDataSource revisionFishEyeDevice;
    private SSDeviceStatusListener ssDeviceStatusListener = new SSDeviceStatusListener();
    public class SSDeviceStatusListener extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if(action.equalsIgnoreCase(DeviceStatusManager.DSM_ON_PUSH_SVRINFO_CALL) ||
                    action.equalsIgnoreCase(DeviceStatusManager.DSM_ON_CHANGED_CALL)){

                Bundle extras = intent.getExtras();
                String devId = (String) extras.get(DEV_ID);
                if("v3027ee621".equals(devId)){
                    revisionFishEyeDevice.connect("v3027ee621","admin","admin",0,1,2);
                }
            }

        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_split_screen);
        initView();
        //  设备相关部分代码
        SDKinitUtil.gClient.addGID("v3027ee621");

        IntentFilter filter = new IntentFilter();
        filter.addAction(DeviceStatusManager.DSM_ON_CHANGED_CALL);
        filter.addAction(DeviceStatusManager.DSM_ON_PUSH_SVRINFO_CALL);
        registerReceiver(ssDeviceStatusListener,filter);

        revisionFishEyeDevice = new FishEyeDeviceDataSource(this);
        revisionFishEyeDevice.setYuvCallback(new FishEyeDeviceDataSource.YuvCallback() {
            @Override
            public void yuv_callback(int width, int height, YUVFrame frame) {
                if(!rendererSet){
                    safeHandler.sendEmptyMessage(101);
                }
            }
        });
    }

    private LinearLayout root_layout;
    private RelativeLayout layout1;
    private RelativeLayout layout2;
    private RelativeLayout layout3;
    private RelativeLayout layout4;
    private GLSurfaceView glSurfaceView;
    private SplitScreenRenderer renderer;
    private void initView() {
        root_layout = (LinearLayout) this.findViewById(R.id.root_layout);
        layout1 = (RelativeLayout) this.findViewById(R.id.layout1);
        layout2 = (RelativeLayout) this.findViewById(R.id.layout2);
        layout3 = (RelativeLayout) this.findViewById(R.id.layout3);
        layout4 = (RelativeLayout) this.findViewById(R.id.layout4);
    }


    private boolean rendererSet = false;
    private void initOpenGL(){
        if(rendererSet) return;
        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        int width = wm.getDefaultDisplay().getWidth();
        int height = wm.getDefaultDisplay().getHeight();
        //**********************************************************************
        glSurfaceView = new GLSurfaceView(this);
        if(checkGLEnvironment()){
            glSurfaceView.setEGLContextClientVersion(2);
            glSurfaceView.setPreserveEGLContextOnPause(true);
            renderer = new SplitScreenRenderer(this, revisionFishEyeDevice);
            glSurfaceView.setRenderer(renderer);
            revisionFishEyeDevice.startCollectFrame();
        } else {
            Toast.makeText(this, "this device does not support OpenGL ES 2.0",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        RelativeLayout.LayoutParams glLayoutParams = new RelativeLayout.LayoutParams(width,width);
        glSurfaceView.setLayoutParams(glLayoutParams);
        glSurfaceView.setVisibility(View.VISIBLE);
        glSurfaceView.setOnTouchListener(new GLViewTouchListener());
        //**********************************************************************
        root_layout.addView(glSurfaceView);
        rendererSet = true;
    }

    private boolean checkGLEnvironment() {
        ActivityManager activityManager =
                (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        ConfigurationInfo deviceConfigurationInfo =
                activityManager.getDeviceConfigurationInfo();
        int reqGlEsVersion = deviceConfigurationInfo.reqGlEsVersion;
        final boolean supportsEs2 =
                reqGlEsVersion >= 0x20000
                        || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1
                        && (Build.FINGERPRINT.startsWith("generic")
                        || Build.FINGERPRINT.startsWith("unknown")
                        || Build.MODEL.contains("google_sdk")
                        || Build.MODEL.contains("Emulator")
                        || Build.MODEL.contains("Android SDK built for x86")));
        return supportsEs2;
    }

    private class GLViewTouchListener implements View.OnTouchListener{
        private float oldDist;
        private int mode = 0;
        private VelocityTracker mVelocityTracker = null;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            // -------------判断多少个触碰点---------------------------------
            switch (event.getAction() & MotionEvent.ACTION_MASK){
                case MotionEvent.ACTION_DOWN:
                    mode = 1;
                    break;
                case MotionEvent.ACTION_UP:
                    mode = 0;
                    break;
                case MotionEvent.ACTION_POINTER_UP:
                    mode = 0;
                    break;
                case MotionEvent.ACTION_POINTER_DOWN:
                    oldDist = spacing(event);
                    mode = 2;
                    break;
            }
            if(event.getAction() == MotionEvent.ACTION_DOWN){
                if (mode == 1) {
                    final float x = event.getX();
                    final float y = event.getY();
                    glSurfaceView.queueEvent(new Runnable() {
                        @Override
                        public void run() {
                            renderer.handleTouchDown(x, y);
                        }
                    });

                    if (mVelocityTracker == null) {
                        mVelocityTracker = VelocityTracker.obtain();
                    } else {
                        mVelocityTracker.clear();
                    }
                    mVelocityTracker.addMovement(event);
                }
            }
            else if(event.getAction() == MotionEvent.ACTION_UP){
                final float x = event.getX();
                final float y = event.getY();
                final float xVelocity = mVelocityTracker.getXVelocity();
                final float yVelocity = mVelocityTracker.getYVelocity();
                glSurfaceView.queueEvent(new Runnable() {
                    @Override
                    public void run() {
                        renderer.handleTouchUp(x, y, xVelocity, yVelocity);
                    }
                });
            }
            else if(event.getAction() ==MotionEvent.ACTION_MOVE){
                if(mode == 1){
                    final float x = event.getX();
                    final float y = event.getY();
                    glSurfaceView.queueEvent(new Runnable() {
                        @Override
                        public void run() {
                            renderer.handleTouchMove(x,y);
                        }
                    });

                    mVelocityTracker.addMovement(event);
                    mVelocityTracker.computeCurrentVelocity(1000);
                }
            }
            return true;
        }
    }

    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (rendererSet){
            glSurfaceView.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(rendererSet){
            revisionFishEyeDevice.stopCollectFrame();
            glSurfaceView.onPause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(ssDeviceStatusListener);
    }
}
