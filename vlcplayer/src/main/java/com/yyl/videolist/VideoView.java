package com.yyl.videolist;


import android.content.Context;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.util.AttributeSet;
import android.util.Log;
import android.view.TextureView;

import com.yyl.videolist.listeners.MediaListenerEvent;
import com.yyl.videolist.listeners.MediaPlayerControl;
import com.yyl.videolist.listeners.VideoSizeChange;
import com.yyl.videolist.utils.LogUtils;

import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;

/**
 * Created by Administrator on 2016/10/12/012.
 */

public class VideoView extends TextureView implements MediaPlayerControl, TextureView.SurfaceTextureListener, VideoSizeChange {
    private VideoPlayer videoMediaLogic;
    private final String tag = "VideoView";

    public VideoView(Context context) {
        this(context, null);
    }

    public VideoView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (isInEditMode()) {
            return;
        }
        init(context);
    }

    public void setMediaListenerEvent(MediaListenerEvent mediaListenerEvent) {
        videoMediaLogic.setMediaListenerEvent(mediaListenerEvent);
    }

    public void onStop() {
        videoMediaLogic.onStop();
    }

    public void onDestory() {
        if (videoMediaLogic != null)
            videoMediaLogic.setVideoSizeChange(null);
        LogUtils.i(tag, "onDestory");
    }

    private void init(Context context) {
        videoMediaLogic = new VideoPlayer(context);
        videoMediaLogic.setVideoSizeChange(this);
        setSurfaceTextureListener(this);
    }

    public void setMediaPlayer(LibVLC libVLC) {
        videoMediaLogic.setMediaPlayer(libVLC);
    }

    public void setMedia(Media media){
        videoMediaLogic.setMedia(media);
    }
    @Override
    public boolean isPrepare() {
        return videoMediaLogic.isPrepare();
    }


    @Override
    public void startPlay(String path) {
        videoMediaLogic.startPlay(path);
    }

    public void saveState() {
        videoMediaLogic.saveState();
    }

    @Override
    public void start() {
        videoMediaLogic.start();
    }

    @Override
    public void pause() {
        videoMediaLogic.pause();
    }

    @Override
    public long getDuration() {
        return videoMediaLogic.getDuration();
    }

    @Override
    public long getCurrentPosition() {
        return videoMediaLogic.getCurrentPosition();
    }

    @Override
    public void seekTo(long pos) {
        videoMediaLogic.seekTo(pos);
    }

    @Override
    public boolean isPlaying() {
        return videoMediaLogic.isPlaying();
    }


    @Override
    public void setMirror(boolean mirror) {
        this.mirror = mirror;
        if (mirror) {
            setScaleX(-1f);
        } else {
            setScaleX(1f);
        }
    }

    private boolean mirror = false;

    @Override
    public boolean getMirror() {
        return mirror;
    }


    @Override
    public int getBufferPercentage() {
        return videoMediaLogic.getBufferPercentage();
    }

    @Override
    public boolean setPlaybackSpeedMedia(float speed) {
        return videoMediaLogic.setPlaybackSpeedMedia(speed);
    }

    @Override
    public float getPlaybackSpeed() {
        return videoMediaLogic.getPlaybackSpeed();
    }


    @Override
    public void setLoop(boolean isLoop) {
        videoMediaLogic.setLoop(isLoop);
    }

    @Override
    public boolean isLoop() {
        return videoMediaLogic.isLoop();
    }


    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        LogUtils.i(tag, "onSurfaceTextureAvailable");
        videoMediaLogic.setSurface(surface);
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        LogUtils.i(tag, "onSurfaceTextureSizeChanged");
        post(new Runnable() {
            @Override
            public void run() {
                adjustAspectRatio(mVideoWidth, mVideoHeight);
            }
        });
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        LogUtils.i(tag, "onSurfaceTextureDestroyed");
        videoMediaLogic.onSurfaceTextureDestroyed();
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    //根据播放状态 打开关闭旋转动画

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        LogUtils.i(tag, "onAttachedToWindow");
        if (isInEditMode()) {
            return;
        }
        setKeepScreenOn(true);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        LogUtils.i(tag, "onDetachedFromWindow");
        if (isInEditMode()) {
            return;
        }
        setKeepScreenOn(false);
    }


    private void adjustAspectRatio(int videoWidth, int videoHeight) {
        if (videoWidth * videoHeight == 0) return;

        int viewWidth = getWidth();
        int viewHeight = getHeight();
        double aspectRatio = (double) videoHeight / (double) videoWidth;

        int newWidth, newHeight;
        if (viewHeight > (int) (viewWidth * aspectRatio)) {
            newWidth = viewWidth;
            newHeight = (int) (viewWidth * aspectRatio);
        } else {
            newWidth = (int) (viewHeight / aspectRatio);
            newHeight = viewHeight;
        }
        int xoff = (viewWidth - newWidth) / 2;
        int yoff = (viewHeight - newHeight) / 2;
        LogUtils.i(tag, "video=" + videoWidth + "x" + videoHeight + " view="
                + viewWidth + "x" + viewHeight + " newView=" + newWidth + "x"
                + newHeight + " off=" + xoff + "," + yoff);

        Matrix txform = new Matrix();
        getTransform(txform);
        txform.setScale((float) newWidth / viewWidth, (float) newHeight
                / viewHeight);
        // txform.postRotate(10); // just for fun
        txform.postTranslate(xoff, yoff);
        setTransform(txform);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (changed) {
            adjustAspectRatio(mVideoWidth, mVideoHeight);
        }
    }

    private int mVideoWidth, mVideoHeight;

    @Override
    public void onVideoSizeChanged(int width, int height, boolean rotation) {
        if (width * height == 0) return;
        if (rotation) {
            this.mVideoWidth = height;
            this.mVideoHeight = width;
        } else {
            this.mVideoWidth = width;
            this.mVideoHeight = height;
        }
        post(new Runnable() {
            @Override
            public void run() {
                adjustAspectRatio(mVideoWidth, mVideoHeight);
            }
        });
    }
}