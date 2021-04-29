package com.example.mymap;

import android.content.res.Configuration;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.hengyi.fastvideoplayer.library.FastVideoPlayer;
import com.hengyi.fastvideoplayer.library.listener.FastVideoPlayerScreenListener;

/**
 * @author 6D
 */
public class RMTPPlayerActivity extends AppCompatActivity {
    private FastVideoPlayer videoPlayer;
    private Button play;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rtmp_player);
        videoPlayer = findViewById(R.id.fastvideo_player);
        play = findViewById(R.id.play);
        videoPlayer.setLive(true);//是直播还是点播  false为点播
        videoPlayer.setScaleType(FastVideoPlayer.SCALETYPE_16_9);
        videoPlayer.setTitle("叫车演示");//设置标题
        videoPlayer.setUrl("rtmp://150.158.176.170:1935/live/fushion");


        //封面图加载
//        Glide.with(this).load("https://bot.tmall.com/guide/img/guide1-bg760.png").into(videoPlayer.getCoverImage());

        play.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                videoPlayer.play();
            }
        });

    }

    /**
     * 下面的这几个Activity的生命状态很重要
     */
    @Override
    protected void onPause() {
        super.onPause();
        if (videoPlayer != null) {
            videoPlayer.onPause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (videoPlayer != null) {
            videoPlayer.onResume();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (videoPlayer != null) {
            videoPlayer.onDestroy();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (videoPlayer != null) {
            videoPlayer.onConfigurationChanged(newConfig);
        }
        if(this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
            Log.i("LANDSCAPE = ", String.valueOf(Configuration.ORIENTATION_LANDSCAPE));
            if (getSupportActionBar() != null){
                getSupportActionBar().hide();
            }
        }
        else if(this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
            Log.i("LANDSCAPE = ", String.valueOf(Configuration.ORIENTATION_PORTRAIT));
            if (!getSupportActionBar().isShowing()){
                getSupportActionBar().show();
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (videoPlayer != null && videoPlayer.onBackPressed()) {
            return;
        }
        super.onBackPressed();
    }
}
