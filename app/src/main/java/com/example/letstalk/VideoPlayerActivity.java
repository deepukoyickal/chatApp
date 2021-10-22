package com.example.letstalk;

import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.MediaController;
import android.widget.VideoView;

public class VideoPlayerActivity extends AppCompatActivity {


    VideoView vid;
    String videoUrl;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);
        vid = (VideoView)findViewById(R.id.videoView);
        videoUrl = getIntent().getStringExtra("url");
    }

    public void playVideo(View v) {
        MediaController m = new MediaController(this);
        vid.setMediaController(m);

        //String path = "android.resource://com.aasemjs.videoplaydemo/"+R.raw.trial;

        Uri u = Uri.parse(videoUrl);

        vid.setVideoURI(u);

        vid.start();

    }
}
