package com.langtao.device;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Created by zzr on 2017/8/3.
 */

public class YUVFrame {

    private int width;
    private int height;
    private ByteBuffer YDatabuffer;
    private int nYLen;
    private ByteBuffer UDatabuffer;
    private int nULen;
    private ByteBuffer VDatabuffer;
    private int nVLen;

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public ByteBuffer getYDatabuffer() {
        return YDatabuffer;
    }

    public ByteBuffer getUDatabuffer() {
        return UDatabuffer;
    }

    public ByteBuffer getVDatabuffer() {
        return VDatabuffer;
    }

    public void release(){
        this.width=0;
        this.height=0;

        this.YDatabuffer=null;
        this.nYLen=0;
        this.UDatabuffer=null;
        this.nULen=0;
        this.VDatabuffer=null;
        this.nVLen=0;
    }

    public int available() {
        return this.nYLen + this.nULen + this.nVLen;
    }

    public byte[] getYuvbyte(){
        byte[] ret = new byte[available()];
        System.arraycopy(YDatabuffer.array(),0, ret,0,nYLen);
        System.arraycopy(UDatabuffer.array(),0, ret,nYLen,nULen);
        System.arraycopy(VDatabuffer.array(),0, ret,nYLen+nULen,nVLen);
        return ret;
    }

    public void setYDataBuffer(int nYLen, byte[] data){
        if(YDatabuffer==null){
            YDatabuffer = ByteBuffer.allocate(nYLen).order(ByteOrder.nativeOrder());
        }
        YDatabuffer.clear();
        YDatabuffer.put(data);
        YDatabuffer.position(0);
        this.nYLen = nYLen;
    }

    public void setUDataBuffer(int nULen, byte[] data){
        if(UDatabuffer==null){
            UDatabuffer = ByteBuffer.allocate(nULen).order(ByteOrder.nativeOrder());
        }
        UDatabuffer.clear();
        UDatabuffer.put(data);
        UDatabuffer.position(0);
        this.nULen = nULen;
    }

    public void setVDataBuffer(int nVLen, byte[] data){
        if(VDatabuffer==null){
            VDatabuffer = ByteBuffer.allocate(nVLen).order(ByteOrder.nativeOrder());
        }
        VDatabuffer.clear();
        VDatabuffer.put(data);
        VDatabuffer.position(0);
        this.nVLen = nVLen;
    }

}
