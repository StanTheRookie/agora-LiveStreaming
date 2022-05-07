package com.example.agora_full_sdk_broadcast;

import static io.agora.rtc.Constants.QUALITY_BAD;
import static io.agora.rtc.Constants.QUALITY_DETECTING;
import static io.agora.rtc.Constants.QUALITY_DOWN;
import static io.agora.rtc.Constants.QUALITY_EXCELLENT;
import static io.agora.rtc.Constants.QUALITY_GOOD;
import static io.agora.rtc.Constants.QUALITY_POOR;
import static io.agora.rtc.Constants.QUALITY_UNKNOWN;
import static io.agora.rtc.Constants.QUALITY_VBAD;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Switch;
import android.widget.Toast;

import io.agora.rtc.Constants;
import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.RtcEngine;
import io.agora.rtc.internal.LastmileProbeConfig;
import io.agora.rtc.video.VideoCanvas;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "Stan";
    private String appId = "71f3985334a34eac9983cbf692cc9fa9";
    private String channelName = "StanTestAndroid";
    private String token = "00671f3985334a34eac9983cbf692cc9fa9IAClAVOEjEDzt/hDXqCeXBqqMrylQfJ2tEVaCvSXkG8E8xhcAncAAAAAEAA/6Ep2ynl3YgEAAQDJeXdi";
    private int local_uid = 7209;

    private RtcEngine mRtcEngine;

    private final IRtcEngineEventHandler mRtcEvenHandler = new IRtcEngineEventHandler() {
        @Override
        public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
            super.onJoinChannelSuccess(channel, uid, elapsed);
        }

        @Override
        public void onUserJoined(int uid, int elapsed) {
            //super.onUserJoined(uid, elapsed);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setupRemoteVideo(uid);
                }
            });
        }

        @Override
        public void onLastmileQuality(int quality) {
            Log.e(TAG, "onLastmileQuality: "+"onLastmileQuality" );
            switch(quality){
                case QUALITY_UNKNOWN:
                    Toast.makeText(MainActivity.this, "网络状态未知", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "onLastmileQuality: " +"网络状态未知");
                    break;
                case QUALITY_EXCELLENT:
                    Log.e(TAG, "onLastmileQuality: " +"网络质量极好");
                    Toast.makeText(MainActivity.this, "网络质量极好", Toast.LENGTH_SHORT).show();
                    break;
                case QUALITY_GOOD:
                    Log.e(TAG, "onLastmileQuality: "+ "网络质量不错" );
                    Toast.makeText(MainActivity.this, "网络质量不错", Toast.LENGTH_SHORT).show();
                    break;
                case QUALITY_POOR:
                    Log.e(TAG, "onLastmileQuality: " +"网络质量一般");
                    Toast.makeText(MainActivity.this, "网络质量一般", Toast.LENGTH_SHORT).show();
                    break;
                case QUALITY_BAD:
                    Log.e(TAG, "onLastmileQuality: " +"网络质量较差");
                    Toast.makeText(MainActivity.this, "网络质量较差", Toast.LENGTH_SHORT).show();
                    break;
                case QUALITY_VBAD:
                    Log.e(TAG, "onLastmileQuality: " + "网络质量很差" );
                    Toast.makeText(MainActivity.this, "网络质量很差", Toast.LENGTH_SHORT).show();
                    break;
                case QUALITY_DOWN:
                    Log.e(TAG, "onLastmileQuality: " + "网络不适合沟通" );
                    Toast.makeText(MainActivity.this, "网络不适合沟通", Toast.LENGTH_SHORT).show();
                    break;
                case QUALITY_DETECTING:
                    Log.e(TAG, "onLastmileQuality: " +"SDK 探测中" );
                    Toast.makeText(MainActivity.this, "SDK 探测中", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    Log.e(TAG, "onLastmileQuality: " + "请重新探测" );
                    Toast.makeText(MainActivity.this, "请重新探测", Toast.LENGTH_SHORT).show();

            }
        }

        @Override
        public void onLastmileProbeResult(LastmileProbeResult result) {
            Log.e(TAG, "onLastmileProbeResult: "+"onLastmileProbeResult" );

            short resultState = result.state;
            int resultRtt = result.rtt;

            Log.e(TAG, "onLastmileProbeResult: \n" + "State "+ result.state +"\n" +"Rtt " + result.rtt);


            mRtcEngine.stopLastmileProbeTest();
            mRtcEngine.joinChannel(token,channelName,"",local_uid);
        }
    };


    //渲染远端视频
    private void setupRemoteVideo(int uid) {
        FrameLayout container = findViewById(R.id.remote_video_view_container);
        SurfaceView surfaceView = mRtcEngine.CreateRendererView(getBaseContext());
        surfaceView.setZOrderMediaOverlay(true);
        container.addView(surfaceView);
        mRtcEngine.setupRemoteVideo(new VideoCanvas(surfaceView,VideoCanvas.RENDER_MODE_FILL,uid));
    }


    //获取Android授权
    private static final int PERMISSION_REQ_ID = 22;  //安卓5.1以后才需要动态获取授权
    private static final String[] REQUESTED_PERMISSIONS = {  //明确动态获取的权限类型
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA
    };
    private boolean checkSelfPermission(String permission, int requestCode){
        if(ContextCompat.checkSelfPermission(this,permission) !=
                PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,REQUESTED_PERMISSIONS,requestCode);
            return false;
        }
        return true;
    }


    private void initializeAndJoinChannel(){

        try {
            mRtcEngine= RtcEngine.create(getBaseContext(),appId,mRtcEvenHandler);
        } catch (Exception e) {
            e.printStackTrace();
        }

        mRtcEngine.setChannelProfile(Constants.CHANNEL_PROFILE_LIVE_BROADCASTING);
        mRtcEngine.setClientRole(Constants.CLIENT_ROLE_BROADCASTER);

        mRtcEngine.enableVideo();//本地视频默认被警用，需要调用enableVideo方法开始视频流。

        //渲染本地视频
        FrameLayout container = findViewById(R.id.local_video_view_container);
        SurfaceView surfaceView = RtcEngine.CreateRendererView(getBaseContext());
        container.addView(surfaceView);
        mRtcEngine.setupLocalVideo(new VideoCanvas(surfaceView,VideoCanvas.RENDER_MODE_FILL,local_uid));

        //mRtcEngine.joinChannel(token,channelName,"",local_uid);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (checkSelfPermission(REQUESTED_PERMISSIONS[0], PERMISSION_REQ_ID) &&
                checkSelfPermission(REQUESTED_PERMISSIONS[1], PERMISSION_REQ_ID)) {
            initializeAndJoinChannel();
        }

    }

    protected void onDestroy() {
        super.onDestroy();

        mRtcEngine.leaveChannel();
        mRtcEngine.destroy();
    }

    public void button1Onclick(View view) {

        //加入频道前网络质量探测
        LastmileProbeConfig config = new LastmileProbeConfig(){};
        config.probeDownlink = true;
        config.probeUplink = true;
        config.expectedDownlinkBitrate = 300000;
        config.expectedUplinkBitrate=100000;

        if(mRtcEngine.startLastmileProbeTest(config) == 0) {
            Log.e(TAG, "button1Onclick: " + "detecting last mile" );
        } else{
            Log.e(TAG, "button1Onclick: " + "Start LastmileProbeTest Fialed");
        }

    }
}