package com.langtao.device;

import android.content.Context;
import android.util.Log;
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

public class FisheyeDeviceDataSource implements GlnkDataSourceListener {

    private static final String TAG = "FisheyeDeviceDataSource";
    private LinkedBlockingQueue<YUVFrame> queue = new LinkedBlockingQueue(100);
    private Context context;

    private boolean isInitedFishDevice = false;
    private boolean isCollectYUV = false;
    public FisheyeDeviceDataSource(Context context){
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

        renderer = new AViewRenderer(context, null);
        renderer.setValidateYUVCallback(new AViewRenderer.ValidateYUVCallback() {
            @Override
            public void yuv_Callback(int width,int height,byte[] byYdata, int nYLen,byte[] byUdata, int nULen,byte[] byVdata, int nVLen) {

                if(isCollectYUV/*filter % 5 == 0*/){
                    Log.e(TAG, "queue.size : "+queue.size());
                    YUVFrame frame = new YUVFrame();
                    frame.setWidth(width);
                    frame.setHeight(height);
                    frame.setYDataBuffer(nYLen, byYdata);
                    frame.setUDataBuffer(nULen, byUdata);
                    frame.setVDataBuffer(nVLen, byVdata);
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
    }

    public YUVFrame getYUVFrame(){
        if(queue!=null && !queue.isEmpty()){
            return queue.poll();
        }
        return null;
    }

    public void startCollectFrame(){
        isCollectYUV = true;
        if(player!=null){
            player.start();
        }
    }
    public void stopCollectFrame(){
        isCollectYUV = false;
        if(player!=null){
            player.stop();
            player.release();
            player = null;
        }
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

}
