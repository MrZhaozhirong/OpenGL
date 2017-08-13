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
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.langtao.device.DeviceStatusManager;
import com.langtao.device.FishEyeDeviceDataSource2;
import com.langtao.device.SDKinitUtil;
import com.langtao.device.YUVFrame;
import com.pixel.opengl.R;

import java.lang.ref.WeakReference;

import static com.langtao.device.DeviceStatusManager.DEV_ID;

/**
 * Created by zzr on 2017/8/10.
 */

public class SplitScreenActivity extends Activity implements View.OnTouchListener {


    private SafeHandler safeHandler = new SafeHandler(this);
    static class SafeHandler extends Handler {
        WeakReference<SplitScreenActivity> mActivity;

        public SafeHandler(SplitScreenActivity activity){
            mActivity = new WeakReference<SplitScreenActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 100:
                    SplitScreenActivity activity = mActivity.get();

                    if(activity!=null){
                        activity.initOpenGLs(msg.arg1,  msg.arg2, (YUVFrame) msg.obj);
                    }
                    break;
                default:
                    break;
            }
        }
    }

    private FishEyeDeviceDataSource2 revisionFishEyeDevice;
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

        revisionFishEyeDevice = new FishEyeDeviceDataSource2(this);
        revisionFishEyeDevice.setYuvCallback(new FishEyeDeviceDataSource2.YuvCallback() {
            @Override
            public void yuv_callback(int width, int height, YUVFrame frame) {
                if(!rendererSet){
                    Message obtain = new Message();
                    obtain.what = 100;
                    obtain.arg1 = width;
                    obtain.arg2 = height;
                    obtain.obj = frame;
                    safeHandler.sendMessage(obtain);//initOpenGL();
                }
            }
        });
    }

    private LinearLayout root_layout;
    private RelativeLayout layout1;
    private RelativeLayout layout2;
    private RelativeLayout layout3;
    private RelativeLayout layout4;
    private GLSurfaceView glSurfaceView1;
    private SplitScreenRenderer renderer1;
    private GLSurfaceView glSurfaceView2;
    private SplitScreenRenderer renderer2;
    private void initView() {
        root_layout = (LinearLayout) this.findViewById(R.id.root_layout);
        layout1 = (RelativeLayout) this.findViewById(R.id.layout1);
        layout2 = (RelativeLayout) this.findViewById(R.id.layout2);
        layout3 = (RelativeLayout) this.findViewById(R.id.layout3);
        layout4 = (RelativeLayout) this.findViewById(R.id.layout4);
    }


    private boolean rendererSet = false;
    private void initOpenGLs(int frameWidth,int frameHeight,YUVFrame frame) {
        if(rendererSet) return;
        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        int width = wm.getDefaultDisplay().getWidth();
        int height = wm.getDefaultDisplay().getHeight();
        //**********************************************************************
        glSurfaceView1 = new GLSurfaceView(this);
        if(checkGLEnvironment()){
            glSurfaceView1.setEGLContextClientVersion(2);
            glSurfaceView1.setPreserveEGLContextOnPause(true);
            renderer1 = new SplitScreenRenderer(this, revisionFishEyeDevice, frameWidth,frameHeight,frame);
            renderer1.resume();
            glSurfaceView1.setRenderer(renderer1);
        } else {
            Toast.makeText(this, "this device does not support OpenGL ES 2.0",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        RelativeLayout.LayoutParams glLayoutParams = new RelativeLayout.LayoutParams(width,width);
        glSurfaceView1.setLayoutParams(glLayoutParams);
        glSurfaceView1.setVisibility(View.VISIBLE);
        //**********************************************************************
        root_layout.addView(glSurfaceView1);
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


    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {

        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (rendererSet){
            glSurfaceView1.onResume();
            //renderer2.resume();
            //glSurfaceView2.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(rendererSet){
            renderer1.pause();
            glSurfaceView1.onPause();
            //renderer2.pause();
            //glSurfaceView2.onPause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(ssDeviceStatusListener);
    }
}
