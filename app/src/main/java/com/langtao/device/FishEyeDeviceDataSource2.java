package com.langtao.device;

import android.content.Context;
import android.widget.Toast;

import java.util.concurrent.LinkedBlockingQueue;

import glnk.client.GlnkClient;
import glnk.media.AViewRenderer;
import glnk.media.GlnkDataSource;
import glnk.media.GlnkDataSourceListener;
import glnk.media.GlnkPlayer;
import glnk.rt.MyRuntime;

/**
 * Created by zzr on 2017/8/3.
 */

public class FishEyeDeviceDataSource2 implements GlnkDataSourceListener {

    private static final String TAG = "FisheyeDeviceDataSource";
    private LinkedBlockingQueue<YUVFrame> queue = new LinkedBlockingQueue(100);
    private Context context;

    private boolean isInitedFishDevice = false;
    private boolean isInitingFishDevice = false;
    private boolean isCollectYUV = false;

    public volatile static YUVFrame current_yuv_frame;

    public FishEyeDeviceDataSource2(Context context){
        if(this.context == null){
            this.context = context.getApplicationContext();
        }
        if(!MyRuntime.supported()){
            Toast.makeText(context, "暂不支持的手机", Toast.LENGTH_SHORT).show();
            return;
        }
    }

    private AViewRenderer renderer;
    private GlnkPlayer player;
    private GlnkDataSource source;

    public void connect(String gid, String username, String passwd,
                        int channelNo,int streamType,int dataType){
        if(isInitedFishDevice){
            //已经初始化过了，证明已正常连接，不要再连接了
            return;
        }
        if(isInitingFishDevice){
            //正在连接，别急
            return;
        }
        isInitingFishDevice = true;
        renderer = new AViewRenderer(context, null);
        renderer.setValidateYUVCallback(new AViewRenderer.ValidateYUVCallback() {
            @Override
            public void yuv_Callback(int width,int height,byte[] byYdata, int nYLen,byte[] byUdata, int nULen,byte[] byVdata, int nVLen) {

                YUVFrame frame = new YUVFrame();
                frame.setWidth(width);
                frame.setHeight(height);
                frame.setYDataBuffer(nYLen, byYdata);
                frame.setUDataBuffer(nULen, byUdata);
                frame.setVDataBuffer(nVLen, byVdata);
                if(yuvCallback!=null){
                    yuvCallback.yuv_callback(width,height,frame);
                }

                if(isCollectYUV/*filter % 5 == 0*/){
                    try {
                        if(queue.size() == 100){
                            YUVFrame obj = queue.poll();
                            obj.release();
                            obj=null;
                        }
                        queue.offer(frame);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        source = new GlnkDataSource(GlnkClient.getInstance());
        source.setGlnkDataSourceListener(this);
        source.setMetaData(gid, username, passwd, channelNo, streamType, dataType);

        player = new GlnkPlayer();
        player.prepare();
        player.setDataSource(source);
        player.setDisplay(renderer);
        player.start();

        new ConsumeThread().start();
    }

    public class ConsumeThread extends Thread{
        @Override
        public void run() {
            super.run();

            while(true){
                try {
                    if(queue!=null && !queue.isEmpty()){
                        YUVFrame poll = queue.poll();
                        if(poll!=null){
                            current_yuv_frame = poll;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }




    public YUVFrame getYUVFrame(){
        //if(queue!=null && !queue.isEmpty()){
        //    return queue.poll();
        //}
        return current_yuv_frame;
    }

    public void startCollectFrame(){
        isCollectYUV = true;
    }

    public void stopCollectFrame(){
        isCollectYUV = false;
    }

    public boolean isInitedFishDevice(){
        return isInitedFishDevice;
    }














    @Override
    public void onTalkingResp(int i) {

    }

    @Override
    public void onIOCtrl(int i, byte[] bytes) {

    }

    @Override
    public void onIOCtrlByManu(byte[] bytes) {

    }

    @Override
    public void onRemoteFileResp(int i, int i1, int i2) {

    }

    @Override
    public void onRemoteFileEOF() {

    }

    @Override
    public void onConnecting() {

    }

    @Override
    public void onConnected(int mode, String ip, int port) {
        isInitingFishDevice = false;
        isInitedFishDevice = true;
    }

    @Override
    public void onAuthorized(int i) {

    }

    @Override
    public void onPermision(int i) {

    }

    @Override
    public void onModeChanged(int i, String s, int i1) {

    }

    @Override
    public void onDisconnected(int i) {
        isInitedFishDevice = false;
    }

    @Override
    public void onDataRate(int i) {

    }

    @Override
    public void onReConnecting() {

    }

    @Override
    public void onEndOfFileCtrl(int i) {

    }

    @Override
    public void onLocalFileOpenResp(int i, int i1) {

    }

    @Override
    public void onLocalFilePlayingStamp(int i) {

    }

    @Override
    public void onLocalFileEOF() {

    }

    @Override
    public void onOpenVideoProcess(int i) {

    }

    @Override
    public void onVideoFrameRate(int i) {

    }

    @Override
    public void onAppVideoFrameRate(int i) {

    }


    public interface YuvCallback{
        void yuv_callback(int width, int height, YUVFrame frame);
    }
    private YuvCallback yuvCallback;

    public void setYuvCallback(YuvCallback callback) {
        this.yuvCallback = callback;
    }
}
